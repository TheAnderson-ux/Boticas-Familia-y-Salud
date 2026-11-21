package pe.edu.boticasfamiliasalud.servicio;

import pe.edu.boticasfamiliasalud.dto.venta.VentaRespuesta;
import pe.edu.boticasfamiliasalud.dto.venta.VentaSolicitud;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoVenta;
import pe.edu.boticasfamiliasalud.enumeracion.MetodoPago;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de ventas.
 */
public interface VentaServicio {

    List<VentaRespuesta> listarVentas(String termino, EstadoVenta estado, MetodoPago metodoPago,
                                      LocalDateTime desde, LocalDateTime hasta);

    VentaRespuesta obtenerVentaPorId(Long idVenta);

    VentaRespuesta registrarVenta(VentaSolicitud solicitud);

    VentaRespuesta anularVenta(Long idVenta);
}