package pe.edu.boticasfamiliasalud.servicio.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.boticasfamiliasalud.dto.venta.ItemVentaRespuesta;
import pe.edu.boticasfamiliasalud.dto.venta.ItemVentaSolicitud;
import pe.edu.boticasfamiliasalud.dto.venta.VentaRespuesta;
import pe.edu.boticasfamiliasalud.dto.venta.VentaSolicitud;
import pe.edu.boticasfamiliasalud.entidad.DetalleVenta;
import pe.edu.boticasfamiliasalud.entidad.Producto;
import pe.edu.boticasfamiliasalud.entidad.Usuario;
import pe.edu.boticasfamiliasalud.entidad.Venta;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoProducto;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoUsuario;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoVenta;
import pe.edu.boticasfamiliasalud.enumeracion.MetodoPago;
import pe.edu.boticasfamiliasalud.excepcion.RecursoNoEncontradoExcepcion;
import pe.edu.boticasfamiliasalud.excepcion.ReglaNegocioExcepcion;
import pe.edu.boticasfamiliasalud.repositorio.ProductoRepositorio;
import pe.edu.boticasfamiliasalud.repositorio.UsuarioRepositorio;
import pe.edu.boticasfamiliasalud.repositorio.VentaRepositorio;
import pe.edu.boticasfamiliasalud.servicio.VentaServicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementacion del servicio de ventas.
 *
 * Reglas de negocio:
 * - El numero de venta se genera en el servidor (correlativo V-%06d) y no puede duplicarse.
 * - Solo se pueden vender productos ACTIVOS con stock suficiente.
 * - Al registrar la venta se descuenta el stock vendido de forma automatica.
 * - Los montos (subtotal, IGV, total, vuelto) los calcula el servidor.
 * - Al anular una venta COMPLETADA se devuelve el stock.
 */
@Service
public class VentaServicioImpl implements VentaServicio {

    private static final Logger registro = LoggerFactory.getLogger(VentaServicioImpl.class);

    private final VentaRepositorio ventaRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public VentaServicioImpl(VentaRepositorio ventaRepositorio,
                             ProductoRepositorio productoRepositorio,
                             UsuarioRepositorio usuarioRepositorio) {
        this.ventaRepositorio = ventaRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaRespuesta> listarVentas(String termino, EstadoVenta estado, MetodoPago metodoPago,
                                             LocalDateTime desde, LocalDateTime hasta) {
        String busqueda = termino == null ? null : termino.trim();

        List<Venta> coincidencias = new ArrayList<>();
        if (busqueda == null || busqueda.isEmpty()) {
            coincidencias.addAll(ventaRepositorio.findAll());
        } else {
            ventaRepositorio.findByNumeroVenta(busqueda).ifPresent(coincidencias::add);
            coincidencias.addAll(ventaRepositorio.findByClienteContainingIgnoreCase(busqueda));
        }

        Map<Long, Venta> unicas = new LinkedHashMap<>();
        for (Venta venta : coincidencias) {
            unicas.putIfAbsent(venta.getIdVenta(), venta);
        }

        List<Venta> resultado = new ArrayList<>(unicas.values());
        if (estado != null) {
            resultado = resultado.stream()
                    .filter(venta -> venta.getEstado() == estado)
                    .collect(Collectors.toList());
        }
        if (metodoPago != null) {
            resultado = resultado.stream()
                    .filter(venta -> venta.getMetodoPago() == metodoPago)
                    .collect(Collectors.toList());
        }
        if (desde != null) {
            resultado = resultado.stream()
                    .filter(venta -> !venta.getFechaVenta().isBefore(desde))
                    .collect(Collectors.toList());
        }
        if (hasta != null) {
            resultado = resultado.stream()
                    .filter(venta -> !venta.getFechaVenta().isAfter(hasta))
                    .collect(Collectors.toList());
        }

        resultado.sort(Comparator.comparing(Venta::getFechaVenta).reversed()
                .thenComparing(Comparator.comparing(Venta::getIdVenta).reversed()));
        return aListaRespuesta(resultado);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaRespuesta obtenerVentaPorId(Long idVenta) {
        return aRespuestaCompleta(buscarVenta(idVenta));
    }

    @Override
    @Transactional
    public VentaRespuesta registrarVenta(VentaSolicitud solicitud) {
        Usuario vendedor = buscarUsuarioActivo(solicitud.getIdUsuario());

        Map<Long, ItemAgrupado> agrupados = agruparItems(solicitud.getItems());
        Map<Long, Producto> productos = validarProductosYStock(agrupados);

        BigDecimal subtotal = agrupados.values().stream()
                .map(ItemAgrupado::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal igv = calcularIgv(subtotal, solicitud);
        BigDecimal total = subtotal.add(igv);

        String numeroVenta = generarNumeroVenta();

        Venta venta = new Venta();
        venta.setNumeroVenta(numeroVenta);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setCliente(normalizarCliente(solicitud.getCliente()));
        venta.setVendedor(vendedor);
        venta.setMetodoPago(solicitud.getMetodoPago());
        venta.setSubtotal(subtotal);
        venta.setIgv(igv);
        venta.setTotal(total);
        aplicarPagoEfectivo(venta, solicitud);
        venta.setEstado(EstadoVenta.COMPLETADA);

        for (ItemAgrupado item : agrupados.values()) {
            Producto producto = productos.get(item.getIdProducto());
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCodigoProducto(producto.getCodigo());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(item.getSubtotal());
            venta.getDetalle().add(detalle);

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepositorio.save(producto);
        }

        VentaRespuesta respuesta = aRespuestaCompleta(ventaRepositorio.save(venta));

        registro.info("Venta registrada | numero={} | items={} | subtotal={} | igv={} | total={} | metodoPago={}",
                numeroVenta, agrupados.size(), subtotal, igv, total, solicitud.getMetodoPago());

        return respuesta;
    }

    @Override
    @Transactional
    public VentaRespuesta anularVenta(Long idVenta) {
        Venta venta = buscarVenta(idVenta);

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new ReglaNegocioExcepcion(
                    "La venta " + venta.getNumeroVenta() + " ya se encuentra anulada.");
        }

        for (DetalleVenta detalle : venta.getDetalle()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepositorio.save(producto);
        }

        venta.setEstado(EstadoVenta.ANULADA);
        VentaRespuesta respuesta = aRespuestaCompleta(ventaRepositorio.save(venta));

        registro.info("Venta anulada | numero={} | stock restaurado de {} producto(s)",
                venta.getNumeroVenta(), venta.getDetalle().size());

        return respuesta;
    }

    /* ------------------------------------------------------------------
       METODOS PRIVADOS
       ------------------------------------------------------------------ */

    private Venta buscarVenta(Long idVenta) {
        return ventaRepositorio.findById(idVenta)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        "No se encontro la venta con id " + idVenta + "."));
    }

    private Usuario buscarUsuarioActivo(Long idUsuario) {
        Usuario usuario = usuarioRepositorio.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        "No se encontro el usuario (vendedor) con id " + idUsuario + "."));
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new ReglaNegocioExcepcion(
                    "El usuario " + usuario.getNombreUsuario() + " esta INACTIVO y no puede registrar ventas.");
        }
        return usuario;
    }

    private Producto buscarProducto(Long idProducto) {
        return productoRepositorio.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        "No se encontro el producto con id " + idProducto + "."));
    }

    private Map<Long, ItemAgrupado> agruparItems(List<ItemVentaSolicitud> items) {
        Map<Long, ItemAgrupado> agrupados = new LinkedHashMap<>();
        for (ItemVentaSolicitud item : items) {
            ItemAgrupado agrupado = agrupados.get(item.getIdProducto());
            if (agrupado == null) {
                agrupados.put(item.getIdProducto(), new ItemAgrupado(item.getIdProducto(), item.getCantidad()));
            } else {
                agrupado.sumar(item.getCantidad());
            }
        }
        return agrupados;
    }

    private Map<Long, Producto> validarProductosYStock(Map<Long, ItemAgrupado> agrupados) {
        Map<Long, Producto> productos = new LinkedHashMap<>();
        for (ItemAgrupado item : agrupados.values()) {
            Producto producto = buscarProducto(item.getIdProducto());
            if (producto.getEstado() != EstadoProducto.ACTIVO) {
                throw new ReglaNegocioExcepcion(
                        "El producto " + producto.getNombre() + " esta INACTIVO y no puede venderse.");
            }
            if (producto.getStock() < item.getCantidad()) {
                throw new ReglaNegocioExcepcion(
                        "Stock insuficiente para el producto " + producto.getNombre()
                                + " (disponible: " + producto.getStock() + ", solicitado: "
                                + item.getCantidad() + ").");
            }
            item.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())));
            productos.put(item.getIdProducto(), producto);
        }
        return productos;
    }

    private BigDecimal calcularIgv(BigDecimal subtotal, VentaSolicitud solicitud) {
        boolean aplicar = Boolean.TRUE.equals(solicitud.getAplicarIgv());
        if (!aplicar || solicitud.getPorcentajeIgv() == null) {
            return BigDecimal.ZERO;
        }
        return subtotal.multiply(solicitud.getPorcentajeIgv())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private void aplicarPagoEfectivo(Venta venta, VentaSolicitud solicitud) {
        if (solicitud.getMetodoPago() != MetodoPago.EFECTIVO) {
            venta.setMontoRecibido(null);
            venta.setVuelto(null);
            return;
        }
        BigDecimal recibido = solicitud.getMontoRecibido();
        venta.setMontoRecibido(recibido);
        if (recibido != null) {
            venta.setVuelto(recibido.subtract(venta.getTotal()).max(BigDecimal.ZERO));
        } else {
            venta.setVuelto(null);
        }
    }

    private String normalizarCliente(String cliente) {
        if (cliente == null || cliente.trim().isEmpty()) {
            return "Cliente generico";
        }
        return cliente.trim();
    }

    private String generarNumeroVenta() {
        int siguiente = 1;
        Venta ultima = ventaRepositorio.findTopByOrderByIdVentaDesc().orElse(null);
        if (ultima != null) {
            String numero = ultima.getNumeroVenta();
            int indice = numero.lastIndexOf('-');
            if (indice >= 0 && indice + 1 < numero.length()) {
                try {
                    siguiente = Integer.parseInt(numero.substring(indice + 1)) + 1;
                } catch (NumberFormatException ignorada) {
                    siguiente = 1;
                }
            }
        }
        return "V-" + String.format("%06d", siguiente);
    }

    private List<VentaRespuesta> aListaRespuesta(List<Venta> ventas) {
        return ventas.stream()
                .map(this::aRespuestaBasica)
                .collect(Collectors.toList());
    }

    private VentaRespuesta aRespuestaBasica(Venta venta) {
        VentaRespuesta respuesta = aRespuestaCompleta(venta);
        respuesta.setItems(new ArrayList<>());
        return respuesta;
    }

    private VentaRespuesta aRespuestaCompleta(Venta venta) {
        VentaRespuesta respuesta = new VentaRespuesta();
        respuesta.setIdVenta(venta.getIdVenta());
        respuesta.setNumeroVenta(venta.getNumeroVenta());
        respuesta.setFechaVenta(venta.getFechaVenta());
        respuesta.setCliente(venta.getCliente());
        respuesta.setIdVendedor(venta.getVendedor().getIdUsuario());
        respuesta.setVendedor(venta.getVendedor().getNombreCompleto());
        respuesta.setMetodoPago(venta.getMetodoPago());
        respuesta.setSubtotal(venta.getSubtotal());
        respuesta.setIgv(venta.getIgv());
        respuesta.setTotal(venta.getTotal());
        respuesta.setMontoRecibido(venta.getMontoRecibido());
        respuesta.setVuelto(venta.getVuelto());
        respuesta.setEstado(venta.getEstado());
        respuesta.setItems(venta.getDetalle().stream()
                .map(this::aItemRespuesta)
                .collect(Collectors.toList()));
        return respuesta;
    }

    private ItemVentaRespuesta aItemRespuesta(DetalleVenta detalle) {
        ItemVentaRespuesta item = new ItemVentaRespuesta();
        item.setIdProducto(detalle.getProducto().getIdProducto());
        item.setCodigo(detalle.getCodigoProducto());
        item.setNombre(detalle.getNombreProducto());
        item.setCantidad(detalle.getCantidad());
        item.setPrecioUnitario(detalle.getPrecioUnitario());
        item.setSubtotal(detalle.getSubtotal());
        return item;
    }

    /**
     * Estructura interna que agrupa las cantidades pedidas por producto.
     * Permite mezclar varios items del mismo producto en una sola linea.
     */
    private static class ItemAgrupado {

        private final Long idProducto;
        private Integer cantidad;
        private BigDecimal subtotal;

        ItemAgrupado(Long idProducto, Integer cantidad) {
            this.idProducto = idProducto;
            this.cantidad = cantidad;
        }

        void sumar(Integer adicional) {
            this.cantidad += adicional;
        }

        Long getIdProducto() {
            return idProducto;
        }

        Integer getCantidad() {
            return cantidad;
        }

        BigDecimal getSubtotal() {
            return subtotal;
        }

        void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }
    }
}