package pe.edu.boticasfamiliasalud.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoUsuario;
import pe.edu.boticasfamiliasalud.enumeracion.RolUsuario;

/**
 * Solicitud para crear o actualizar un usuario.
 *
 * La contrasena es obligatoria al crear y opcional al editar:
 * si viene vacia o nula al editar, se conserva la existente.
 * La validacion de la contrasena se realiza en el servicio.
 */
public class UsuarioSolicitud {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 150, message = "El nombre completo no puede superar 150 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$",
            message = "El nombre de usuario solo admite letras, numeros, punto, guion y guion bajo")
    @Size(min = 3, max = 60, message = "El nombre de usuario debe tener entre 3 y 60 caracteres")
    private String nombreUsuario;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 120, message = "El correo no puede superar 120 caracteres")
    private String correo;

    @Size(max = 100, message = "La contrasena no puede superar 100 caracteres")
    private String contrasena;

    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol;

    private EstadoUsuario estado;

    public UsuarioSolicitud() {
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }
}