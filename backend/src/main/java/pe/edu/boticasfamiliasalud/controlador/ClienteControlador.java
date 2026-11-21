package pe.edu.boticasfamiliasalud.controlador;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.boticasfamiliasalud.dto.cliente.CambioEstadoClienteSolicitud;
import pe.edu.boticasfamiliasalud.dto.cliente.ClienteRespuesta;
import pe.edu.boticasfamiliasalud.dto.cliente.ClienteSolicitud;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoCliente;
import pe.edu.boticasfamiliasalud.servicio.ClienteServicio;

import java.util.List;

/**
 * Controlador REST de clientes.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteControlador {

    private final ClienteServicio clienteServicio;

    public ClienteControlador(ClienteServicio clienteServicio) {
        this.clienteServicio = clienteServicio;
    }

    @GetMapping
    public List<ClienteRespuesta> listarClientes(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) EstadoCliente estado) {
        return clienteServicio.buscarClientes(termino, estado);
    }

    @GetMapping("/{idCliente}")
    public ClienteRespuesta obtenerClientePorId(@PathVariable Long idCliente) {
        return clienteServicio.obtenerClientePorId(idCliente);
    }

    @PostMapping
    public ResponseEntity<ClienteRespuesta> crearCliente(
            @Valid @RequestBody ClienteSolicitud solicitud) {
        ClienteRespuesta respuesta = clienteServicio.crearCliente(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{idCliente}")
    public ClienteRespuesta actualizarCliente(
            @PathVariable Long idCliente,
            @Valid @RequestBody ClienteSolicitud solicitud) {
        return clienteServicio.actualizarCliente(idCliente, solicitud);
    }

    @PatchMapping("/{idCliente}/estado")
    public ClienteRespuesta cambiarEstadoCliente(
            @PathVariable Long idCliente,
            @Valid @RequestBody CambioEstadoClienteSolicitud solicitud) {
        return clienteServicio.cambiarEstadoCliente(idCliente, solicitud);
    }
}