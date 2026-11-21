package pe.edu.boticasfamiliasalud.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de autenticacion JWT.
 *
 * Lee el encabezado "Authorization: Bearer <token>", valida el token y
 * coloca la autenticacion en el contexto de Spring Security. Si el token
 * falta o no es valido, la peticion sigue anonima y el acceso a endpoints
 * protegidos sera rechazado con 401.
 */
@Component
public class JwtAutenticacionFiltro extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtServicio jwtServicio;

    public JwtAutenticacionFiltro(JwtServicio jwtServicio) {
        this.jwtServicio = jwtServicio;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain cadena) throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");

        if (cabecera != null && cabecera.startsWith(PREFIJO_BEARER)) {
            String token = cabecera.substring(PREFIJO_BEARER.length());
            JwtPrincipal principal = jwtServicio.obtenerPrincipal(token);

            if (principal != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken autenticacion =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + principal.getRol())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        }

        cadena.doFilter(request, response);
    }
}