package pe.edu.boticasfamiliasalud.controlador;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.boticasfamiliasalud.dto.venta.VentaRespuesta;
import pe.edu.boticasfamiliasalud.dto.venta.VentaSolicitud;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoVenta;
import pe.edu.boticasfamiliasalud.enumeracion.MetodoPago;
import pe.edu.boticasfamiliasalud.servicio.VentaServicio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controlador REST de ventas bajo la ruta /api/ventas.
 *
 * Los filtros de fecha (desde/hasta) se reciben como fechas ISO (YYYY-MM-DD)
 * y se convierten al rango completo del dia.
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaControlador {

    private final VentaServicio ventaServicio;

    public VentaControlador(VentaServicio ventaServicio) {
        this.ventaServicio = ventaServicio;
    }

    @GetMapping
    public List<VentaRespuesta> listarVentas(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) EstadoVenta estado,
            @RequestParam(required = false) MetodoPago metodoPago,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        return ventaServicio.listarVentas(
                termino,
                estado,
                metodoPago,
                desde != null ? desde.atStartOfDay() : null,
                hasta != null ? hasta.atTime(LocalTime.MAX) : null);
    }

    @GetMapping("/{idVenta}")
    public VentaRespuesta obtenerVentaPorId(@PathVariable Long idVenta) {
        return ventaServicio.obtenerVentaPorId(idVenta);
    }

    @PostMapping
    public ResponseEntity<VentaRespuesta> registrarVenta(
            @Valid @RequestBody VentaSolicitud solicitud) {
        VentaRespuesta respuesta = ventaServicio.registrarVenta(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PatchMapping("/{idVenta}/anular")
    public VentaRespuesta anularVenta(@PathVariable Long idVenta) {
        return ventaServicio.anularVenta(idVenta);
    }
}