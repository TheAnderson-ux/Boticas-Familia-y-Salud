package pe.edu.boticasfamiliasalud.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoCliente;

/**
 * Solicitud para crear o actualizar un cliente.
 * El estado es opcional: al crear se asume ACTIVO.
 */
public class ClienteSolicitud {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @Pattern(regexp = "^[A-Za-z0-9-]*$",
            message = "El documento solo admite letras, numeros y guiones")
    @Size(max = 20, message = "El documento no puede superar 20 caracteres")
    private String documento;

    @Size(max = 20, message = "El telefono no puede superar 20 caracteres")
    @Pattern(regexp = "^[0-9+ -]*$",
            message = "El telefono solo admite numeros, espacios, signos + y guiones")
    private String telefono;

    @Email(message = "El correo no es valido")
    @Size(max = 120, message = "El correo no puede superar 120 caracteres")
    private String correo;

    @Size(max = 200, message = "La direccion no puede superar 200 caracteres")
    private String direccion;

    private EstadoCliente estado;

    public ClienteSolicitud() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public EstadoCliente getEstado() {
        return estado;
    }

    public void setEstado(EstadoCliente estado) {
        this.estado = estado;
    }
}