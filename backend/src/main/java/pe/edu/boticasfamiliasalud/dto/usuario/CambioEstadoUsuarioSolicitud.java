package pe.edu.boticasfamiliasalud.dto.usuario;

import jakarta.validation.constraints.NotNull;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoUsuario;

/**
 * Solicitud para activar o desactivar un usuario.
 */
public class CambioEstadoUsuarioSolicitud {

    @NotNull(message = "El estado es obligatorio")
    private EstadoUsuario estado;

    public CambioEstadoUsuarioSolicitud() {
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }
}