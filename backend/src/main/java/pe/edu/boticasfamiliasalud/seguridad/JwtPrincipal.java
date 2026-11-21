package pe.edu.boticasfamiliasalud.seguridad;

/**
 * Datos del usuario autenticado extraidos del token JWT.
 */
public class JwtPrincipal {

    private final Long idUsuario;

    private final String nombreUsuario;

    private final String rol;

    public JwtPrincipal(Long idUsuario, String nombreUsuario, String rol) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getRol() {
        return rol;
    }
}