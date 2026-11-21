package pe.edu.boticasfamiliasalud.servicio;

import pe.edu.boticasfamiliasalud.dto.autenticacion.LoginRespuesta;
import pe.edu.boticasfamiliasalud.dto.autenticacion.LoginSolicitud;

/**
 * Servicio de autenticacion del sistema.
 *
 * NOTA: en esta etapa la contrasena se compara en texto plano (seguridad
 * pendiente). Al implementar Spring Security, este servicio debera usar
 * BCrypt.
 */
public interface AutenticacionServicio {

    LoginRespuesta iniciarSesion(LoginSolicitud solicitud);
}