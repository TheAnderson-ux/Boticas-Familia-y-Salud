package pe.edu.boticasfamiliasalud.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.boticasfamiliasalud.entidad.Rol;
import pe.edu.boticasfamiliasalud.enumeracion.RolUsuario;

import java.util.Optional;

/**
 * Repositorio de acceso a la tabla "roles".
 */
public interface RolRepositorio extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(RolUsuario nombre);

    boolean existsByNombre(RolUsuario nombre);
}