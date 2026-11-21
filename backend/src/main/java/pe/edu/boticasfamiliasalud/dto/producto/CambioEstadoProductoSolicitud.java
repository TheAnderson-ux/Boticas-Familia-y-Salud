package pe.edu.boticasfamiliasalud.dto.producto;

import jakarta.validation.constraints.NotNull;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoProducto;

/**
 * Solicitud para activar o inactivar un producto.
 */
public class CambioEstadoProductoSolicitud {

    @NotNull(message = "El estado es obligatorio")
    private EstadoProducto estado;

    public CambioEstadoProductoSolicitud() {
    }

    public EstadoProducto getEstado() {
        return estado;
    }

    public void setEstado(EstadoProducto estado) {
        this.estado = estado;
    }
}