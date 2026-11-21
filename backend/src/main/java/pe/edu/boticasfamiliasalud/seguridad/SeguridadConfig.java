
package pe.edu.boticasfamiliasalud.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    private final JwtAutenticacionFiltro jwtAutenticacionFiltro;
    private final ObjectMapper objectMapper;
    private final List<String> origenesPermitidos;

    public SeguridadConfig(
            JwtAutenticacionFiltro jwtAutenticacionFiltro,
            ObjectMapper objectMapper,
            @Value("${app.seguridad.origenes-permitidos:http://localhost:8080,http://127.0.0.1:8080,http://localhost:5500,http://127.0.0.1:5500,https://serpent-childhood-stainless.ngrok-free.dev,https://*.ngrok-free.dev}")
            List<String> origenesPermitidos) {

        this.jwtAutenticacionFiltro = jwtAutenticacionFiltro;
        this.objectMapper = objectMapper;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuracion = new CorsConfiguration();

        configuracion.setAllowedOriginPatterns(origenesPermitidos);
        configuracion.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        configuracion.setAllowedHeaders(
                List.of("Content-Type", "Authorization")
        );

        UrlBasedCorsConfigurationSource fuente =
                new UrlBasedCorsConfigurationSource();

        fuente.registerCorsConfiguration("/**", configuracion);

        return fuente;
    }

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(Customizer.withDefaults())

                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // =====================================
                        // RUTAS PUBLICAS
                        // =====================================

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/autenticacion/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/salud"
                        ).permitAll()

                        .requestMatchers(
                                "/", "/index.html", "/favicon.ico"
                        ).permitAll()

                        .requestMatchers(
                                "/css/**", "/js/**", "/assets/**"
                        ).permitAll()

                        .requestMatchers("/error").permitAll()


                        // =====================================
                        // CAMBIO DE CONTRASENA
                        // Usuario autenticado
                        // =====================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios/*/cambiar-contrasena"
                        ).authenticated()


                        // =====================================
                        // ADMINISTRADOR
                        // =====================================

                        .requestMatchers(
                                "/api/usuarios", "/api/usuarios/**"
                        ).hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                "/api/configuracion", "/api/configuracion/**"
                        ).hasRole("ADMINISTRADOR")

                        .requestMatchers(
                                "/api/reportes", "/api/reportes/**"
                        ).hasRole("ADMINISTRADOR")


                        // =====================================
                        // VENTAS
                        // ADMINISTRADOR Y VENDEDOR
                        // =====================================

                        .requestMatchers(
                                "/api/ventas", "/api/ventas/**"
                        ).hasAnyRole("ADMINISTRADOR", "VENDEDOR")


                        // =====================================
                        // CLIENTES
                        // ADMINISTRADOR Y VENDEDOR
                        // =====================================

                        .requestMatchers(
                                "/api/clientes", "/api/clientes/**"
                        ).hasAnyRole("ADMINISTRADOR", "VENDEDOR")


                        // =====================================
                        // PRODUCTOS
                        // CONSULTA: LOS TRES ROLES
                        // =====================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/productos", "/api/productos/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "VENDEDOR",
                                "ALMACENERO"
                        )


                        // =====================================
                        // PRODUCTOS
                        // MODIFICACION: ADMINISTRADOR Y ALMACENERO
                        // =====================================

                        .requestMatchers(
                                "/api/productos", "/api/productos/**"
                        ).hasAnyRole("ADMINISTRADOR", "ALMACENERO")


                        // =====================================
                        // CUALQUIER OTRA RUTA
                        // REQUIERE AUTENTICACION
                        // =====================================

                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entradaNoAutenticado())
                        .accessDeniedHandler(manejadorDenegado())
                )

                .addFilterBefore(
                        jwtAutenticacionFiltro,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // =====================================
    // ERROR 401: NO AUTENTICADO
    // =====================================

    private AuthenticationEntryPoint entradaNoAutenticado() {

        return (request, response, excepcion) -> {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            cuerpoError(
                                    request.getRequestURI(),
                                    401,
                                    "Unauthorized",
                                    "No autenticado. Inicie sesion para obtener un token."
                            )
                    )
            );
        };
    }

    // =====================================
    // ERROR 403: SIN PERMISOS
    // =====================================

    private AccessDeniedHandler manejadorDenegado() {

        return (request, response, excepcion) -> {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");

            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            cuerpoError(
                                    request.getRequestURI(),
                                    403,
                                    "Forbidden",
                                    "No tiene permisos para realizar esta operacion."
                            )
                    )
            );
        };
    }

    // =====================================
    // ESTRUCTURA DE RESPUESTA DE ERROR
    // =====================================

    private Map<String, Object> cuerpoError(
            String ruta,
            int estado,
            String error,
            String mensaje) {

        Map<String, Object> cuerpo = new LinkedHashMap<>();

        cuerpo.put("fechaHora", LocalDateTime.now().toString());
        cuerpo.put("estado", estado);
        cuerpo.put("error", error);
        cuerpo.put("mensaje", mensaje);
        cuerpo.put("ruta", ruta);

        return cuerpo;
    }
}
