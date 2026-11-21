package pe.edu.boticasfamiliasalud.servicio.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.boticasfamiliasalud.dto.reporte.ProductosMasVendidosReporte;
import pe.edu.boticasfamiliasalud.dto.reporte.ResumenReporte;
import pe.edu.boticasfamiliasalud.dto.reporte.VentasPorDiaReporte;
import pe.edu.boticasfamiliasalud.dto.reporte.VentasPorMetodoReporte;
import pe.edu.boticasfamiliasalud.entidad.DetalleVenta;
import pe.edu.boticasfamiliasalud.entidad.Venta;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoVenta;
import pe.edu.boticasfamiliasalud.repositorio.VentaRepositorio;
import pe.edu.boticasfamiliasalud.servicio.ReporteServicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementacion del servicio de reportes de ventas.
 *
 * Los reportes consideran unicamente ventas con estado COMPLETADA.
 * La agregacion se realiza en memoria sobre las ventas del periodo, porque
 * en esta etapa el volumen de datos es pequeno.
 */
@Service
public class ReporteServicioImpl implements ReporteServicio {

    private static final Map<String, String> ETIQUETAS_METODO = Map.of(
            "EFECTIVO", "Efectivo",
            "TARJETA", "Tarjeta",
            "YAPE", "Yape/Plin",
            "TRANSFERENCIA", "Transferencia");

    private static final int LIMITE_PRODUCTOS = 10;

    private final VentaRepositorio ventaRepositorio;

    public ReporteServicioImpl(VentaRepositorio ventaRepositorio) {
        this.ventaRepositorio = ventaRepositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenReporte resumen(LocalDateTime desde, LocalDateTime hasta) {
        List<Venta> ventas = obtenerVentasCompletadas(desde, hasta);

        ResumenReporte resumen = new ResumenReporte();
        resumen.setDesde(desde != null ? desde : null);
        resumen.setHasta(hasta != null ? hasta : null);

        BigDecimal totalIngresos = ventas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long cantidadVentas = ventas.size();
        int productosVendidos = 0;
        for (Venta venta : ventas) {
            for (DetalleVenta detalle : venta.getDetalle()) {
                productosVendidos += detalle.getCantidad();
            }
        }

        BigDecimal ticketPromedio = cantidadVentas == 0 ? BigDecimal.ZERO
                : totalIngresos.divide(BigDecimal.valueOf(cantidadVentas), 2, RoundingMode.HALF_UP);

        resumen.setTotalIngresos(totalIngresos.setScale(2, RoundingMode.HALF_UP));
        resumen.setTotalVentas(cantidadVentas);
        resumen.setTicketPromedio(ticketPromedio);
        resumen.setProductosVendidos(productosVendidos);
        return resumen;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentasPorDiaReporte> ventasPorDia(LocalDate desde, LocalDate hasta, Integer dias) {
        int cantidadDias = dias != null && dias > 0 ? dias : 7;
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusDays(cantidadDias - 1L);

        List<Venta> ventas = obtenerVentasCompletadas(inicio.atStartOfDay(), fin.atTime(LocalTime.MAX));

        Map<LocalDate, VentasPorDiaReporte> porDia = new LinkedHashMap<>();
        LocalDate cursor = inicio;
        while (!cursor.isAfter(fin)) {
            VentasPorDiaReporte item = new VentasPorDiaReporte();
            item.setFecha(cursor);
            item.setCantidadVentas(0L);
            item.setTotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            porDia.put(cursor, item);
            cursor = cursor.plusDays(1);
        }

        for (Venta venta : ventas) {
            VentasPorDiaReporte item = porDia.get(venta.getFechaVenta().toLocalDate());
            if (item == null) {
                item = new VentasPorDiaReporte();
                item.setFecha(venta.getFechaVenta().toLocalDate());
                item.setCantidadVentas(0L);
                item.setTotal(BigDecimal.ZERO);
                porDia.put(item.getFecha(), item);
            }
            item.setCantidadVentas(item.getCantidadVentas() + 1);
            item.setTotal(item.getTotal().add(venta.getTotal()).setScale(2, RoundingMode.HALF_UP));
        }

        return new ArrayList<>(porDia.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductosMasVendidosReporte> productosMasVendidos(LocalDateTime desde, LocalDateTime hasta) {
        List<Venta> ventas = obtenerVentasCompletadas(desde, hasta);

        Map<Long, ProductosMasVendidosReporte> acumulados = new LinkedHashMap<>();
        for (Venta venta : ventas) {
            for (DetalleVenta detalle : venta.getDetalle()) {
                ProductosMasVendidosReporte item = acumulados.get(detalle.getProducto().getIdProducto());
                if (item == null) {
                    item = new ProductosMasVendidosReporte();
                    item.setIdProducto(detalle.getProducto().getIdProducto());
                    item.setCodigo(detalle.getCodigoProducto());
                    item.setNombre(detalle.getNombreProducto());
                    item.setCantidad(0);
                    item.setImporte(BigDecimal.ZERO);
                    acumulados.put(detalle.getProducto().getIdProducto(), item);
                }
                item.setCantidad(item.getCantidad() + detalle.getCantidad());
                item.setImporte(item.getImporte().add(detalle.getSubtotal()).setScale(2, RoundingMode.HALF_UP));
            }
        }

        return acumulados.values().stream()
                .sorted(Comparator.comparing(ProductosMasVendidosReporte::getCantidad).reversed()
                        .thenComparing(Comparator.comparing(ProductosMasVendidosReporte::getImporte).reversed()))
                .limit(LIMITE_PRODUCTOS)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentasPorMetodoReporte> ventasPorMetodoPago(LocalDateTime desde, LocalDateTime hasta) {
        List<Venta> ventas = obtenerVentasCompletadas(desde, hasta);

        Map<String, VentasPorMetodoReporte> acumulados = new LinkedHashMap<>();
        for (Venta venta : ventas) {
            String clave = venta.getMetodoPago().name();
            VentasPorMetodoReporte item = acumulados.get(clave);
            if (item == null) {
                item = new VentasPorMetodoReporte();
                item.setMetodoPago(clave);
                item.setEtiqueta(ETIQUETAS_METODO.getOrDefault(clave, clave));
                item.setCantidadVentas(0L);
                item.setImporte(BigDecimal.ZERO);
                acumulados.put(clave, item);
            }
            item.setCantidadVentas(item.getCantidadVentas() + 1);
            item.setImporte(item.getImporte().add(venta.getTotal()).setScale(2, RoundingMode.HALF_UP));
        }

        return acumulados.values().stream()
                .sorted(Comparator.comparing(VentasPorMetodoReporte::getImporte).reversed())
                .collect(Collectors.toList());
    }

    private List<Venta> obtenerVentasCompletadas(LocalDateTime desde, LocalDateTime hasta) {
        List<Venta> ventas;
        if (desde != null && hasta != null) {
            ventas = ventaRepositorio.findByFechaVentaBetween(desde, hasta);
        } else if (hasta != null) {
            ventas = ventaRepositorio.findByFechaVentaLessThanEqual(hasta);
        } else if (desde != null) {
            ventas = ventaRepositorio.findByFechaVentaGreaterThanEqual(desde);
        } else {
            ventas = ventaRepositorio.findAll();
        }
        return ventas.stream()
                .filter(venta -> venta.getEstado() == EstadoVenta.COMPLETADA)
                .collect(Collectors.toList());
    }
}