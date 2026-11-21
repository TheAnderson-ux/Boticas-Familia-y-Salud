package pe.edu.boticasfamiliasalud.dto.autenticacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud de inicio de sesion.
 */
public class LoginSolicitud {

    @NotBlank(message = "El usuario es obligatorio")
    @Size(max = 60, message = "El usuario no puede superar 60 caracteres")
    private String nombreUsuario;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(max = 100, message = "La contrasena no puede superar 100 caracteres")
    private String contrasena;

    public LoginSolicitud() {
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}