package pe.edu.boticasfamiliasalud.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.boticasfamiliasalud.entidad.Producto;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoProducto;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a la tabla "productos".
 */
public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdProductoNot(String codigo, Long idProducto);

    List<Producto> findByEstado(EstadoProducto estado);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByCategoriaContainingIgnoreCase(String categoria);
}