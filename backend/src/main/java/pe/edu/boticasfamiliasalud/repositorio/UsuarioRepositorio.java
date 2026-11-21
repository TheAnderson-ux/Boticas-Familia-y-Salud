package pe.edu.boticasfamiliasalud.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.boticasfamiliasalud.entidad.Usuario;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoUsuario;
import pe.edu.boticasfamiliasalud.enumeracion.RolUsuario;

import java.util.Optional;

/**
 * Repositorio de acceso a la tabla "usuarios".
 *
 * Los metodos "IgnoreCase" permiten validar unicidad sin importar
 * mayusculas ni minusculas.
 */
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNombreUsuarioIgnoreCase(String nombreUsuario);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByNombreUsuarioIgnoreCaseAndIdUsuarioNot(String nombreUsuario, Long idUsuario);

    boolean existsByCorreoIgnoreCaseAndIdUsuarioNot(String correo, Long idUsuario);

    long countByRolNombreAndEstado(RolUsuario nombreRol, EstadoUsuario estado);
}