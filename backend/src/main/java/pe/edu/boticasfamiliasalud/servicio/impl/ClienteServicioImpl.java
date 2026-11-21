package pe.edu.boticasfamiliasalud.servicio.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.boticasfamiliasalud.dto.cliente.CambioEstadoClienteSolicitud;
import pe.edu.boticasfamiliasalud.dto.cliente.ClienteRespuesta;
import pe.edu.boticasfamiliasalud.dto.cliente.ClienteSolicitud;
import pe.edu.boticasfamiliasalud.entidad.Cliente;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoCliente;
import pe.edu.boticasfamiliasalud.excepcion.RecursoNoEncontradoExcepcion;
import pe.edu.boticasfamiliasalud.excepcion.ReglaNegocioExcepcion;
import pe.edu.boticasfamiliasalud.repositorio.ClienteRepositorio;
import pe.edu.boticasfamiliasalud.servicio.ClienteServicio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementacion del servicio de clientes.
 */
@Service
public class ClienteServicioImpl implements ClienteServicio {

    private static final Logger registro = LoggerFactory.getLogger(ClienteServicioImpl.class);

    private final ClienteRepositorio clienteRepositorio;

    public ClienteServicioImpl(ClienteRepositorio clienteRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteRespuesta> listarClientes() {
        return convertirLista(clienteRepositorio.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteRespuesta> buscarClientes(String termino, EstadoCliente estado) {
        String busqueda = termino == null ? null : termino.trim();

        List<Cliente> coincidencias = new ArrayList<>();
        if (busqueda == null || busqueda.isEmpty()) {
            if (estado != null) {
                coincidencias.addAll(clienteRepositorio.findByEstado(estado));
            } else {
                coincidencias.addAll(clienteRepositorio.findAll());
            }
        } else {
            coincidencias.addAll(clienteRepositorio.findByNombreContainingIgnoreCase(busqueda));
            coincidencias.addAll(clienteRepositorio.findByTelefonoContainingIgnoreCase(busqueda));
            clienteRepositorio.findByDocumentoIgnoreCase(busqueda).ifPresent(coincidencias::add);
        }

        Map<Long, Cliente> unicos = new LinkedHashMap<>();
        for (Cliente cliente : coincidencias) {
            unicos.putIfAbsent(cliente.getIdCliente(), cliente);
        }

        List<Cliente> resultado = new ArrayList<>(unicos.values());
        if (estado != null) {
            resultado = resultado.stream()
                    .filter(cliente -> cliente.getEstado() == estado)
                    .collect(Collectors.toList());
        }

        resultado.sort(Comparator.comparing(Cliente::getNombre, String.CASE_INSENSITIVE_ORDER));
        return convertirLista(resultado);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteRespuesta obtenerClientePorId(Long idCliente) {
        return convertir(buscarCliente(idCliente));
    }

    @Override
    @Transactional
    public ClienteRespuesta crearCliente(ClienteSolicitud solicitud) {
        String nombre = limpiarEspacios(solicitud.getNombre());
        String documento = normalizarDocumento(solicitud.getDocumento());

        if (!documento.isEmpty() && clienteRepositorio.existsByDocumentoIgnoreCase(documento)) {
            throw new ReglaNegocioExcepcion("Ya existe un cliente con ese documento.");
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setDocumento(documento.isEmpty() ? null : documento);
        aplicarDatosOpcionales(cliente, solicitud);
        cliente.setEstado(solicitud.getEstado() != null ? solicitud.getEstado() : EstadoCliente.ACTIVO);
        cliente.setFechaRegistro(java.time.LocalDateTime.now());

        return convertir(clienteRepositorio.save(cliente));
    }

    @Override
    @Transactional
    public ClienteRespuesta actualizarCliente(Long idCliente, ClienteSolicitud solicitud) {
        Cliente cliente = buscarCliente(idCliente);

        String documento = normalizarDocumento(solicitud.getDocumento());
        if (!documento.isEmpty() &&
                clienteRepositorio.existsByDocumentoIgnoreCaseAndIdClienteNot(documento, idCliente)) {
            throw new ReglaNegocioExcepcion("Ya existe otro cliente con ese documento.");
        }

        cliente.setNombre(limpiarEspacios(solicitud.getNombre()));
        cliente.setDocumento(documento.isEmpty() ? null : documento);
        aplicarDatosOpcionales(cliente, solicitud);
        if (solicitud.getEstado() != null) {
            cliente.setEstado(solicitud.getEstado());
        }

        return convertir(clienteRepositorio.save(cliente));
    }

    @Override
    @Transactional
    public ClienteRespuesta cambiarEstadoCliente(Long idCliente, CambioEstadoClienteSolicitud solicitud) {
        Cliente cliente = buscarCliente(idCliente);
        cliente.setEstado(solicitud.getEstado());
        return convertir(clienteRepositorio.save(cliente));
    }

    private void aplicarDatosOpcionales(Cliente cliente, ClienteSolicitud solicitud) {
        cliente.setTelefono(limpiarEspacios(solicitud.getTelefono()));
        cliente.setCorreo(limpiarEspacios(solicitud.getCorreo()));
        cliente.setDireccion(limpiarEspacios(solicitud.getDireccion()));
    }

    private Cliente buscarCliente(Long idCliente) {
        return clienteRepositorio.findById(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        "No se encontro el cliente con id " + idCliente + "."));
    }

    private String normalizarDocumento(String documento) {
        return documento == null ? "" : documento.trim().toUpperCase(Locale.ROOT);
    }

    private String limpiarEspacios(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private List<ClienteRespuesta> convertirLista(List<Cliente> clientes) {
        return clientes.stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    private ClienteRespuesta convertir(Cliente cliente) {
        ClienteRespuesta respuesta = new ClienteRespuesta();
        respuesta.setIdCliente(cliente.getIdCliente());
        respuesta.setNombre(cliente.getNombre());
        respuesta.setDocumento(cliente.getDocumento());
        respuesta.setTelefono(cliente.getTelefono());
        respuesta.setCorreo(cliente.getCorreo());
        respuesta.setDireccion(cliente.getDireccion());
        respuesta.setEstado(cliente.getEstado());
        respuesta.setFechaRegistro(cliente.getFechaRegistro());
        return respuesta;
    }
}