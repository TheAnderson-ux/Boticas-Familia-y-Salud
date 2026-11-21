package pe.edu.boticasfamiliasalud.servicio;

import pe.edu.boticasfamiliasalud.dto.reporte.ProductosMasVendidosReporte;
import pe.edu.boticasfamiliasalud.dto.reporte.ResumenReporte;
import pe.edu.boticasfamiliasalud.dto.reporte.VentasPorDiaReporte;
import pe.edu.boticasfamiliasalud.dto.reporte.VentasPorMetodoReporte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de reportes de ventas.
 */
public interface ReporteServicio {

    ResumenReporte resumen(LocalDateTime desde, LocalDateTime hasta);

    List<VentasPorDiaReporte> ventasPorDia(LocalDate desde, LocalDate hasta, Integer dias);

    List<ProductosMasVendidosReporte> productosMasVendidos(LocalDateTime desde, LocalDateTime hasta);

    List<VentasPorMetodoReporte> ventasPorMetodoPago(LocalDateTime desde, LocalDateTime hasta);
}