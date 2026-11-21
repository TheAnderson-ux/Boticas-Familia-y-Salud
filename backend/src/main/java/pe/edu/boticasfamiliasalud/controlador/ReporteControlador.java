package pe.edu.boticasfamiliasalud.controlador;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.boticasfamiliasalud.dto.reporte.ProductosMasVendidosReporte;
import pe.edu.boticasfamiliasalud.dto.reporte.ResumenReporte;
import pe.edu.boticasfamiliasalud.dto.reporte.VentasPorDiaReporte;
import pe.edu.boticasfamiliasalud.dto.reporte.VentasPorMetodoReporte;
import pe.edu.boticasfamiliasalud.servicio.ReporteServicio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controlador REST de reportes bajo /api/reportes.
 *
 * Los filtros de fecha (desde/hasta) son fechas ISO (YYYY-MM-DD). Para
 * "ventas-por-dia" se puede indicar "dias" (por defecto 7) cuando no se
 * envian fechas.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteControlador {

    private final ReporteServicio reporteServicio;

    public ReporteControlador(ReporteServicio reporteServicio) {
        this.reporteServicio = reporteServicio;
    }

    @GetMapping("/resumen")
    public ResumenReporte resumen(
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        return reporteServicio.resumen(inicioDe(desde), finDe(hasta));
    }

    @GetMapping("/ventas-por-dia")
    public List<VentasPorDiaReporte> ventasPorDia(
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta,
            @RequestParam(required = false) Integer dias) {
        return reporteServicio.ventasPorDia(desde, hasta, dias);
    }

    @GetMapping("/productos-mas-vendidos")
    public List<ProductosMasVendidosReporte> productosMasVendidos(
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        return reporteServicio.productosMasVendidos(inicioDe(desde), finDe(hasta));
    }

    @GetMapping("/metodos-pago")
    public List<VentasPorMetodoReporte> ventasPorMetodoPago(
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        return reporteServicio.ventasPorMetodoPago(inicioDe(desde), finDe(hasta));
    }

    private java.time.LocalDateTime inicioDe(LocalDate fecha) {
        return fecha != null ? fecha.atStartOfDay() : null;
    }

    private java.time.LocalDateTime finDe(LocalDate fecha) {
        return fecha != null ? fecha.atTime(LocalTime.MAX) : null;
    }
}