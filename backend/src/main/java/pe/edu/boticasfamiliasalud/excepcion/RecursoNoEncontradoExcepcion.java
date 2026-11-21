package pe.edu.boticasfamiliasalud.excepcion;

/**
 * Excepcion cuando un recurso solicitado no existe.
 * Se traduce a HTTP 404.
 */
public class RecursoNoEncontradoExcepcion extends RuntimeException {

    public RecursoNoEncontradoExcepcion(String mensaje) {
        super(mensaje);
    }
}