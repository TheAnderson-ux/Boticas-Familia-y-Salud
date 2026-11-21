package pe.edu.boticasfamiliasalud.servicio;

import pe.edu.boticasfamiliasalud.dto.cliente.CambioEstadoClienteSolicitud;
import pe.edu.boticasfamiliasalud.dto.cliente.ClienteRespuesta;
import pe.edu.boticasfamiliasalud.dto.cliente.ClienteSolicitud;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoCliente;

import java.util.List;

/**
 * Servicio de operaciones de clientes.
 */
public interface ClienteServicio {

    List<ClienteRespuesta> listarClientes();

    List<ClienteRespuesta> buscarClientes(String termino, EstadoCliente estado);

    ClienteRespuesta obtenerClientePorId(Long idCliente);

    ClienteRespuesta crearCliente(ClienteSolicitud solicitud);

    ClienteRespuesta actualizarCliente(Long idCliente, ClienteSolicitud solicitud);

    ClienteRespuesta cambiarEstadoCliente(Long idCliente, CambioEstadoClienteSolicitud solicitud);
}