package pe.edu.boticasfamiliasalud.servicio;

import pe.edu.boticasfamiliasalud.dto.usuario.CambioContrasenaSolicitud;
import pe.edu.boticasfamiliasalud.dto.usuario.CambioEstadoUsuarioSolicitud;
import pe.edu.boticasfamiliasalud.dto.usuario.UsuarioRespuesta;
import pe.edu.boticasfamiliasalud.dto.usuario.UsuarioSolicitud;
import pe.edu.boticasfamiliasalud.seguridad.JwtPrincipal;

import java.util.List;

/**
 * Servicio de usuarios.
 */
public interface UsuarioServicio {

    List<UsuarioRespuesta> listarUsuarios();

    UsuarioRespuesta obtenerUsuarioPorId(Long idUsuario);

    UsuarioRespuesta crearUsuario(UsuarioSolicitud solicitud);

    UsuarioRespuesta actualizarUsuario(Long idUsuario, UsuarioSolicitud solicitud);

    UsuarioRespuesta cambiarEstadoUsuario(Long idUsuario, CambioEstadoUsuarioSolicitud solicitud);

    UsuarioRespuesta cambiarContrasena(Long idUsuario, CambioContrasenaSolicitud solicitud, JwtPrincipal principal);
}