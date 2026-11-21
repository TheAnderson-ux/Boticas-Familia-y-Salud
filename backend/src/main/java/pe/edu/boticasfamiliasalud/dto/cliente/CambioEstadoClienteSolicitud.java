package pe.edu.boticasfamiliasalud.dto.cliente;

import jakarta.validation.constraints.NotNull;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoCliente;

/**
 * Solicitud para cambiar el estado de un cliente.
 */
public class CambioEstadoClienteSolicitud {

    @NotNull(message = "El estado es obligatorio")
    private EstadoCliente estado;

    public CambioEstadoClienteSolicitud() {
    }

    public EstadoCliente getEstado() {
        return estado;
    }

    public void setEstado(EstadoCliente estado) {
        this.estado = estado;
    }
}