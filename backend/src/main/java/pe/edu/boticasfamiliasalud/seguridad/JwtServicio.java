package pe.edu.boticasfamiliasalud.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.boticasfamiliasalud.entidad.Usuario;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Genera y valida tokens JWT (firma HS256).
 *
 * El token lleva como "subject" el id del usuario y los claims
 * "nombreUsuario" y "rol". La clave secreta se lee de
 * "app.seguridad.secreto" (Base64, al menos 32 bytes).
 */
@Component
public class JwtServicio {

    private static final String CLAIM_NOMBRE_USUARIO = "nombreUsuario";
    private static final String CLAIM_ROL = "rol";

    private final SecretKey clave;

    private final long expiracionMinutos;

    public JwtServicio(@Value("${app.seguridad.secreto}") String secreto,
                       @Value("${app.seguridad.expiracion-minutos}") long expiracionMinutos) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
        this.expiracionMinutos = expiracionMinutos;
    }

    /**
     * Crea un token para el usuario indicado con su tiempo de expiracion.
     */
    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMinutos * 60_000L);

        return Jwts.builder()
                .subject(String.valueOf(usuario.getIdUsuario()))
                .claim(CLAIM_NOMBRE_USUARIO, usuario.getNombreUsuario())
                .claim(CLAIM_ROL, usuario.getRol().getNombre().name())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(clave)
                .compact();
    }

    /**
     * Extrae los datos del token si es valido; en caso contrario devuelve null.
     */
    public JwtPrincipal obtenerPrincipal(String token) {
        try {
            Claims reclamaciones = Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long idUsuario = Long.valueOf(reclamaciones.getSubject());
            String nombreUsuario = reclamaciones.get(CLAIM_NOMBRE_USUARIO, String.class);
            String rol = reclamaciones.get(CLAIM_ROL, String.class);

            return new JwtPrincipal(idUsuario, nombreUsuario, rol);
        } catch (Exception ignorada) {
            return null;
        }
    }
}