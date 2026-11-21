package pe.edu.boticasfamiliasalud.servicio.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.boticasfamiliasalud.dto.usuario.CambioContrasenaSolicitud;
import pe.edu.boticasfamiliasalud.dto.usuario.CambioEstadoUsuarioSolicitud;
import pe.edu.boticasfamiliasalud.dto.usuario.UsuarioRespuesta;
import pe.edu.boticasfamiliasalud.dto.usuario.UsuarioSolicitud;
import pe.edu.boticasfamiliasalud.entidad.Rol;
import pe.edu.boticasfamiliasalud.entidad.Usuario;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoUsuario;
import pe.edu.boticasfamiliasalud.enumeracion.RolUsuario;
import pe.edu.boticasfamiliasalud.excepcion.CredencialesInvalidasExcepcion;
import pe.edu.boticasfamiliasalud.excepcion.RecursoNoEncontradoExcepcion;
import pe.edu.boticasfamiliasalud.excepcion.ReglaNegocioExcepcion;
import pe.edu.boticasfamiliasalud.excepcion.AccesoDenegadoExcepcion;
import pe.edu.boticasfamiliasalud.repositorio.UsuarioRepositorio;
import pe.edu.boticasfamiliasalud.seguridad.JwtPrincipal;
import pe.edu.boticasfamiliasalud.servicio.RolServicio;
import pe.edu.boticasfamiliasalud.servicio.UsuarioServicio;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementacion del servicio de usuarios.
 *
 * Reglas de negocio: unicidad case-insensitive de nombre de usuario y correo,
 * contrasenas cifradas con BCrypt (nunca en texto plano), conservacion de la
 * contrasena al editar, y proteccion del ultimo administrador activo.
 */
@Service
public class UsuarioServicioImpl implements UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final RolServicio rolServicio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServicioImpl(UsuarioRepositorio usuarioRepositorio,
                               RolServicio rolServicio,
                               PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolServicio = rolServicio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRespuesta> listarUsuarios() {
        return usuarioRepositorio.findAll().stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioRespuesta obtenerUsuarioPorId(Long idUsuario) {
        return aRespuesta(obtenerEntidad(idUsuario));
    }

    @Override
    @Transactional
    public UsuarioRespuesta crearUsuario(UsuarioSolicitud solicitud) {
        validarContrasena(solicitud.getContrasena(), true);
        validarUnicidad(solicitud.getNombreUsuario(), solicitud.getCorreo(), null);

        Rol rol = rolServicio.obtenerRolPorNombre(solicitud.getRol());
        EstadoUsuario estado = solicitud.getEstado() != null ? solicitud.getEstado() : EstadoUsuario.ACTIVO;

        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(solicitud.getNombreCompleto().trim());
        usuario.setNombreUsuario(solicitud.getNombreUsuario().trim());
        usuario.setCorreo(solicitud.getCorreo().trim());
        usuario.setContrasena(passwordEncoder.encode(solicitud.getContrasena()));
        usuario.setEstado(estado);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setRol(rol);

        return aRespuesta(usuarioRepositorio.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioRespuesta actualizarUsuario(Long idUsuario, UsuarioSolicitud solicitud) {
        Usuario usuario = obtenerEntidad(idUsuario);
        validarUnicidad(solicitud.getNombreUsuario(), solicitud.getCorreo(), idUsuario);

        usuario.setNombreCompleto(solicitud.getNombreCompleto().trim());
        usuario.setNombreUsuario(solicitud.getNombreUsuario().trim());
        usuario.setCorreo(solicitud.getCorreo().trim());

        String contrasena = solicitud.getContrasena();
        if (contrasena != null && !contrasena.isBlank()) {
            validarContrasena(contrasena, true);
            usuario.setContrasena(passwordEncoder.encode(contrasena));
        }

        usuario.setRol(rolServicio.obtenerRolPorNombre(solicitud.getRol()));
        if (solicitud.getEstado() != null) {
            usuario.setEstado(solicitud.getEstado());
        }

        return aRespuesta(usuarioRepositorio.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioRespuesta cambiarEstadoUsuario(Long idUsuario, CambioEstadoUsuarioSolicitud solicitud) {
        Usuario usuario = obtenerEntidad(idUsuario);

        if (solicitud.getEstado() == EstadoUsuario.INACTIVO && usuario.getEstado() == EstadoUsuario.ACTIVO) {
            if (usuario.getRol().getNombre() == RolUsuario.ADMINISTRADOR
                    && !existeOtroAdministradorActivo(idUsuario)) {
                throw new ReglaNegocioExcepcion(
                        "No se puede desactivar el ultimo usuario ADMINISTRADOR ACTIVO");
            }
        }

        usuario.setEstado(solicitud.getEstado());
        return aRespuesta(usuarioRepositorio.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioRespuesta cambiarContrasena(Long idUsuario, CambioContrasenaSolicitud solicitud, JwtPrincipal principal) {
        boolean esAdministrador = RolUsuario.ADMINISTRADOR.name().equals(principal.getRol());

        if (!esAdministrador && !principal.getIdUsuario().equals(idUsuario)) {
            throw new AccesoDenegadoExcepcion("No tiene permisos para cambiar la contrasena de otro usuario.");
        }

        Usuario usuario = obtenerEntidad(idUsuario);

        if (!passwordEncoder.matches(solicitud.getContrasenaActual(), usuario.getContrasena())) {
            throw new CredencialesInvalidasExcepcion("La contrasena actual no es correcta.");
        }

        if (!solicitud.getNuevaContrasena().equals(solicitud.getConfirmarContrasena())) {
            throw new ReglaNegocioExcepcion("La nueva contrasena y su confirmacion no coinciden.");
        }

        validarContrasena(solicitud.getNuevaContrasena(), true);
        usuario.setContrasena(passwordEncoder.encode(solicitud.getNuevaContrasena()));

        return aRespuesta(usuarioRepositorio.save(usuario));
    }

    private Usuario obtenerEntidad(Long idUsuario) {
        return usuarioRepositorio.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        "No existe el usuario con id " + idUsuario));
    }

    private boolean existeOtroAdministradorActivo(Long idUsuario) {
        long totalActivos = usuarioRepositorio
                .countByRolNombreAndEstado(RolUsuario.ADMINISTRADOR, EstadoUsuario.ACTIVO);
        return totalActivos > 1;
    }

    private void validarUnicidad(String nombreUsuario, String correo, Long idUsuario) {
        if (idUsuario == null) {
            if (usuarioRepositorio.existsByNombreUsuarioIgnoreCase(nombreUsuario.trim())) {
                throw new ReglaNegocioExcepcion("Ya existe un usuario con ese nombre de usuario");
            }
            if (usuarioRepositorio.existsByCorreoIgnoreCase(correo.trim())) {
                throw new ReglaNegocioExcepcion("Ya existe un usuario con ese correo");
            }
        } else {
            if (usuarioRepositorio.existsByNombreUsuarioIgnoreCaseAndIdUsuarioNot(nombreUsuario.trim(), idUsuario)) {
                throw new ReglaNegocioExcepcion("Ya existe un usuario con ese nombre de usuario");
            }
            if (usuarioRepositorio.existsByCorreoIgnoreCaseAndIdUsuarioNot(correo.trim(), idUsuario)) {
                throw new ReglaNegocioExcepcion("Ya existe un usuario con ese correo");
            }
        }
    }

    private void validarContrasena(String contrasena, boolean obligatoria) {
        if (obligatoria && (contrasena == null || contrasena.isBlank())) {
            throw new ReglaNegocioExcepcion("La contrasena es obligatoria");
        }
        if (contrasena != null && !contrasena.isBlank() && contrasena.length() < 4) {
            throw new ReglaNegocioExcepcion("La contrasena debe tener al menos 4 caracteres");
        }
    }

    private UsuarioRespuesta aRespuesta(Usuario usuario) {
        UsuarioRespuesta respuesta = new UsuarioRespuesta();
        respuesta.setIdUsuario(usuario.getIdUsuario());
        respuesta.setNombreCompleto(usuario.getNombreCompleto());
        respuesta.setNombreUsuario(usuario.getNombreUsuario());
        respuesta.setCorreo(usuario.getCorreo());
        respuesta.setRol(usuario.getRol().getNombre());
        respuesta.setDescripcionRol(usuario.getRol().getDescripcion());
        respuesta.setEstado(usuario.getEstado());
        respuesta.setFechaRegistro(usuario.getFechaRegistro());
        return respuesta;
    }
}