package pe.edu.boticasfamiliasalud.controlador;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlador de salud: verifica que el backend este en funcionamiento.
 * No consulta ni conecta a la base de datos.
 */
@RestController
@RequestMapping("/api")
public class SaludControlador {

    /**
     * Endpoint GET /api/salud usado para probar que la aplicacion responde.
     */
    @GetMapping("/salud")
    public Map<String, String> obtenerEstado() {
        Map<String, String> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "El backend de BOTICAS FAMILIA & SALUD esta funcionando correctamente.");
        respuesta.put("estado", "ACTIVO");
        return respuesta;
    }
}