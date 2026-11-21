package pe.edu.boticasfamiliasalud.controlador;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.boticasfamiliasalud.dto.configuracion.ConfiguracionRespuesta;
import pe.edu.boticasfamiliasalud.dto.configuracion.ConfiguracionSolicitud;
import pe.edu.boticasfamiliasalud.servicio.ConfiguracionServicio;

/**
 * Controlador REST de la configuracion del sistema bajo /api/configuracion.
 */
@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionControlador {

    private final ConfiguracionServicio configuracionServicio;

    public ConfiguracionControlador(ConfiguracionServicio configuracionServicio) {
        this.configuracionServicio = configuracionServicio;
    }

    @GetMapping
    public ConfiguracionRespuesta obtenerConfiguracion() {
        return configuracionServicio.obtenerConfiguracion();
    }

    @PutMapping
    public ConfiguracionRespuesta guardarConfiguracion(
            @Valid @RequestBody ConfiguracionSolicitud solicitud) {
        return configuracionServicio.guardarConfiguracion(solicitud);
    }
}