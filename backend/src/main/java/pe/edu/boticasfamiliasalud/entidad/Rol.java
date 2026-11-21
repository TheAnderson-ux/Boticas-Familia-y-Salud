package pe.edu.boticasfamiliasalud.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pe.edu.boticasfamiliasalud.enumeracion.RolUsuario;

/**
 * Entidad Rol. Representa la tabla "roles" de la base de datos.
 * Un rol puede estar asociado a muchos usuarios.
 */
@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre", nullable = false, unique = true, length = 30)
    private RolUsuario nombre;

    @Column(name = "descripcion", nullable = false, length = 120)
    private String descripcion;

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
    }

    public RolUsuario getNombre() {
        return nombre;
    }

    public void setNombre(RolUsuario nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}