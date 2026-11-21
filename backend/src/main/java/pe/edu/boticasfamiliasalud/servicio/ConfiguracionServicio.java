package pe.edu.boticasfamiliasalud.servicio;

import pe.edu.boticasfamiliasalud.dto.configuracion.ConfiguracionRespuesta;
import pe.edu.boticasfamiliasalud.dto.configuracion.ConfiguracionSolicitud;

/**
 * Servicio de configuracion del sistema.
 */
public interface ConfiguracionServicio {

    ConfiguracionRespuesta obtenerConfiguracion();

    ConfiguracionRespuesta guardarConfiguracion(ConfiguracionSolicitud solicitud);
}