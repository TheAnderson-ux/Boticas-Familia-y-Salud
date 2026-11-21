package pe.edu.boticasfamiliasalud.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.boticasfamiliasalud.entidad.Venta;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoVenta;
import pe.edu.boticasfamiliasalud.enumeracion.MetodoPago;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a la tabla "ventas".
 */
public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    Optional<Venta> findByNumeroVenta(String numeroVenta);

    boolean existsByNumeroVenta(String numeroVenta);

    Optional<Venta> findTopByOrderByIdVentaDesc();

    List<Venta> findByEstado(EstadoVenta estado);

    List<Venta> findByMetodoPago(MetodoPago metodoPago);

    List<Venta> findByClienteContainingIgnoreCase(String cliente);

    List<Venta> findByFechaVentaBetween(LocalDateTime desde, LocalDateTime hasta);

    List<Venta> findByFechaVentaLessThanEqual(LocalDateTime hasta);

    List<Venta> findByFechaVentaGreaterThanEqual(LocalDateTime desde);
}