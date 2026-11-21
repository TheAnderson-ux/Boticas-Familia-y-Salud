package pe.edu.boticasfamiliasalud.servicio.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.boticasfamiliasalud.dto.autenticacion.LoginRespuesta;
import pe.edu.boticasfamiliasalud.dto.autenticacion.LoginSolicitud;
import pe.edu.boticasfamiliasalud.entidad.Usuario;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoUsuario;
import pe.edu.boticasfamiliasalud.excepcion.CredencialesInvalidasExcepcion;
import pe.edu.boticasfamiliasalud.repositorio.UsuarioRepositorio;
import pe.edu.boticasfamiliasalud.seguridad.JwtServicio;
import pe.edu.boticasfamiliasalud.servicio.AutenticacionServicio;

/**
 * Implementacion del servicio de autenticacion.
 *
 * Valida el usuario (case-insensitive) y la contrasena cifrada con BCrypt.
 * Rechaza usuarios INACTIVOS. Al autenticarse emite un token JWT.
 * No se indica cual campo fue incorrecto para no facilitar la
 * enumeracion de cuentas.
 */
@Service
public class AutenticacionServicioImpl implements AutenticacionServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtServicio jwtServicio;

    public AutenticacionServicioImpl(UsuarioRepositorio usuarioRepositorio,
                                     PasswordEncoder passwordEncoder,
                                     JwtServicio jwtServicio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtServicio = jwtServicio;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginRespuesta iniciarSesion(LoginSolicitud solicitud) {
        Usuario usuario = usuarioRepositorio.findByNombreUsuarioIgnoreCase(
                        solicitud.getNombreUsuario().trim())
                .orElseThrow(() -> new CredencialesInvalidasExcepcion(
                        "Usuario o contrasena incorrectos."));

        if (usuario.getEstado() != EstadoUsuario.ACTIVO
                || !passwordEncoder.matches(solicitud.getContrasena(), usuario.getContrasena())) {
            throw new CredencialesInvalidasExcepcion("Usuario o contrasena incorrectos.");
        }

        LoginRespuesta respuesta = new LoginRespuesta();
        respuesta.setIdUsuario(usuario.getIdUsuario());
        respuesta.setNombreCompleto(usuario.getNombreCompleto());
        respuesta.setNombreUsuario(usuario.getNombreUsuario());
        respuesta.setCorreo(usuario.getCorreo());
        respuesta.setRol(usuario.getRol().getNombre().name());
        respuesta.setDescripcionRol(usuario.getRol().getDescripcion());
        respuesta.setFechaRegistro(usuario.getFechaRegistro());
        respuesta.setMensaje("Inicio de sesion correcto.");
        respuesta.setToken(jwtServicio.generarToken(usuario));
        return respuesta;
    }
}