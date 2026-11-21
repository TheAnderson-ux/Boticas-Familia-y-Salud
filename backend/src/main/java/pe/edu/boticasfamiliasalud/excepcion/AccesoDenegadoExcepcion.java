package pe.edu.boticasfamiliasalud.excepcion;

/**
 * Excepcion de acceso denegado (por ejemplo, un usuario que intenta cambiar
 * la contrasena de otro). Se traduce a HTTP 403.
 */
public class AccesoDenegadoExcepcion extends RuntimeException {

    public AccesoDenegadoExcepcion(String mensaje) {
        super(mensaje);
    }
}