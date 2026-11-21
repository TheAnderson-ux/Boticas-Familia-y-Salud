package pe.edu.boticasfamiliasalud.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud para cambiar la contrasena de un usuario.
 */
public class CambioContrasenaSolicitud {

    @NotBlank(message = "La contrasena actual es obligatoria")
    private String contrasenaActual;

    @NotBlank(message = "La nueva contrasena es obligatoria")
    @Size(min = 4, message = "La nueva contrasena debe tener al menos 4 caracteres")
    private String nuevaContrasena;

    @NotBlank(message = "La confirmacion de contrasena es obligatoria")
    @Size(min = 4, message = "La confirmacion debe tener al menos 4 caracteres")
    private String confirmarContrasena;

    public CambioContrasenaSolicitud() {
    }

    public String getContrasenaActual() {
        return contrasenaActual;
    }

    public void setContrasenaActual(String contrasenaActual) {
        this.contrasenaActual = contrasenaActual;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }

    public String getConfirmarContrasena() {
        return confirmarContrasena;
    }

    public void setConfirmarContrasena(String confirmarContrasena) {
        this.confirmarContrasena = confirmarContrasena;
    }
}