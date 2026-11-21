package pe.edu.boticasfamiliasalud.servicio;

import pe.edu.boticasfamiliasalud.entidad.Rol;
import pe.edu.boticasfamiliasalud.enumeracion.RolUsuario;

/**
 * Servicio de roles.
 */
public interface RolServicio {

    Rol obtenerRolPorNombre(RolUsuario nombreRol);

    void inicializarRoles();
}