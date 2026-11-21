package pe.edu.boticasfamiliasalud.excepcion;

/**
 * Excepcion de regla de negocio (por ejemplo, desactivar el ultimo
 * administrador activo). Se traduce a HTTP 409.
 */
public class ReglaNegocioExcepcion extends RuntimeException {

    public ReglaNegocioExcepcion(String mensaje) {
        super(mensaje);
    }
}