package pe.edu.boticasfamiliasalud.controlador;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.boticasfamiliasalud.dto.autenticacion.LoginRespuesta;
import pe.edu.boticasfamiliasalud.dto.autenticacion.LoginSolicitud;
import pe.edu.boticasfamiliasalud.servicio.AutenticacionServicio;

/**
 * Controlador REST de autenticacion bajo /api/autenticacion.
 */
@RestController
@RequestMapping("/api/autenticacion")
public class AutenticacionControlador {

    private final AutenticacionServicio autenticacionServicio;

    public AutenticacionControlador(AutenticacionServicio autenticacionServicio) {
        this.autenticacionServicio = autenticacionServicio;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRespuesta> iniciarSesion(
            @Valid @RequestBody LoginSolicitud solicitud) {
        LoginRespuesta respuesta = autenticacionServicio.iniciarSesion(solicitud);
        return ResponseEntity.status(HttpStatus.OK).body(respuesta);
    }
}