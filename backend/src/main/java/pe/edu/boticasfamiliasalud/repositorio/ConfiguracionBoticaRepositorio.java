package pe.edu.boticasfamiliasalud.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.boticasfamiliasalud.entidad.ConfiguracionBotica;

import java.util.Optional;

/**
 * Repositorio de acceso a la tabla "configuracion_botica".
 */
public interface ConfiguracionBoticaRepositorio extends JpaRepository<ConfiguracionBotica, Long> {

    Optional<ConfiguracionBotica> findTopByOrderByIdConfiguracionAsc();
}