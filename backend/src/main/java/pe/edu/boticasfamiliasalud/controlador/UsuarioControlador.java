package pe.edu.boticasfamiliasalud.controlador;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.boticasfamiliasalud.dto.usuario.CambioContrasenaSolicitud;
import pe.edu.boticasfamiliasalud.dto.usuario.CambioEstadoUsuarioSolicitud;
import pe.edu.boticasfamiliasalud.dto.usuario.UsuarioRespuesta;
import pe.edu.boticasfamiliasalud.dto.usuario.UsuarioSolicitud;
import pe.edu.boticasfamiliasalud.seguridad.JwtPrincipal;
import pe.edu.boticasfamiliasalud.servicio.UsuarioServicio;

import java.util.List;

/**
 * Controlador de usuarios bajo la ruta /api/usuarios.
 * Las respuestas nunca incluyen contrasena.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    public List<UsuarioRespuesta> listarUsuarios() {
        return usuarioServicio.listarUsuarios();
    }

    @GetMapping("/{idUsuario}")
    public UsuarioRespuesta obtenerUsuarioPorId(@PathVariable Long idUsuario) {
        return usuarioServicio.obtenerUsuarioPorId(idUsuario);
    }

    @PostMapping
    public ResponseEntity<UsuarioRespuesta> crearUsuario(@Valid @RequestBody UsuarioSolicitud solicitud) {
        UsuarioRespuesta creado = usuarioServicio.crearUsuario(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{idUsuario}")
    public UsuarioRespuesta actualizarUsuario(
            @PathVariable Long idUsuario, @Valid @RequestBody UsuarioSolicitud solicitud) {
        return usuarioServicio.actualizarUsuario(idUsuario, solicitud);
    }

    @PatchMapping("/{idUsuario}/estado")
    public UsuarioRespuesta cambiarEstadoUsuario(
            @PathVariable Long idUsuario,
            @Valid @RequestBody CambioEstadoUsuarioSolicitud solicitud) {
        return usuarioServicio.cambiarEstadoUsuario(idUsuario, solicitud);
    }

    @PostMapping("/{idUsuario}/cambiar-contrasena")
    public UsuarioRespuesta cambiarContrasena(
            @PathVariable Long idUsuario,
            @Valid @RequestBody CambioContrasenaSolicitud solicitud,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return usuarioServicio.cambiarContrasena(idUsuario, solicitud, principal);
    }
}