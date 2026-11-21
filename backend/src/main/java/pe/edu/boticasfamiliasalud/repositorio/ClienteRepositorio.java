package pe.edu.boticasfamiliasalud.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.boticasfamiliasalud.entidad.Cliente;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoCliente;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a la tabla "clientes".
 */
public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByDocumentoIgnoreCase(String documento);

    boolean existsByDocumentoIgnoreCaseAndIdClienteNot(String documento, Long idCliente);

    Optional<Cliente> findByDocumentoIgnoreCase(String documento);

    List<Cliente> findByEstado(EstadoCliente estado);

    List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    List<Cliente> findByTelefonoContainingIgnoreCase(String telefono);
}