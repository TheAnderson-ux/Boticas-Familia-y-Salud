package pe.edu.boticasfamiliasalud.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuracion temporal de CORS para desarrollo local.
 *
 * Los origenes se restringiran en produccion. No se usa el comodin "*";
 * se admite unicamente el patron limitado de tuneles temporales de ngrok.
 */
@Configuration
public class CorsConfiguracion implements WebMvcConfigurer {

    private final List<String> origenesPermitidos;

    public CorsConfiguracion(
            @Value("${app.seguridad.origenes-permitidos:http://localhost:8080,http://127.0.0.1:8080,http://localhost:5500,http://127.0.0.1:5500,https://serpent-childhood-stainless.ngrok-free.dev,https://*.ngrok-free.dev}") List<String> origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    /**
     * Permite solicitudes desde los origenes de desarrollo local y del
     * dominio temporal de ngrok (patron https://*.ngrok-free.dev).
     */
    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/**")
                .allowedOriginPatterns(origenesPermitidos.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization");
    }
}