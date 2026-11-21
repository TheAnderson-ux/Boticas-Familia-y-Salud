package pe.edu.boticasfamiliasalud.excepcion;

/**
 * Excepcion cuando las credenciales de acceso son invalidas.
 * Se traduce a HTTP 401.
 */
public class CredencialesInvalidasExcepcion extends RuntimeException {

    public CredencialesInvalidasExcepcion(String mensaje) {
        super(mensaje);
    }
}