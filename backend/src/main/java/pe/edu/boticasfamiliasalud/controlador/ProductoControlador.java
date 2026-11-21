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
import pe.edu.boticasfamiliasalud.dto.producto.AjusteStockSolicitud;
import pe.edu.boticasfamiliasalud.dto.producto.CambioEstadoProductoSolicitud;
import pe.edu.boticasfamiliasalud.dto.producto.ProductoRespuesta;
import pe.edu.boticasfamiliasalud.dto.producto.ProductoSolicitud;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoProducto;
import pe.edu.boticasfamiliasalud.servicio.ProductoServicio;

import java.util.List;

/**
 * Controlador REST de productos.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoControlador {

    private final ProductoServicio productoServicio;

    public ProductoControlador(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    @GetMapping
    public List<ProductoRespuesta> listarProductos(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) EstadoProducto estado) {
        return productoServicio.buscarProductos(termino, estado);
    }

    @GetMapping("/{idProducto}")
    public ProductoRespuesta obtenerProductoPorId(@PathVariable Long idProducto) {
        return productoServicio.obtenerProductoPorId(idProducto);
    }

    @PostMapping
    public ResponseEntity<ProductoRespuesta> crearProducto(
            @Valid @RequestBody ProductoSolicitud solicitud) {
        ProductoRespuesta respuesta = productoServicio.crearProducto(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/{idProducto}")
    public ProductoRespuesta actualizarProducto(
            @PathVariable Long idProducto,
            @Valid @RequestBody ProductoSolicitud solicitud) {
        return productoServicio.actualizarProducto(idProducto, solicitud);
    }

    @PatchMapping("/{idProducto}/estado")
    public ProductoRespuesta cambiarEstadoProducto(
            @PathVariable Long idProducto,
            @Valid @RequestBody CambioEstadoProductoSolicitud solicitud) {
        return productoServicio.cambiarEstadoProducto(idProducto, solicitud);
    }

    @PatchMapping("/{idProducto}/stock")
    public ProductoRespuesta ajustarStock(
            @PathVariable Long idProducto,
            @Valid @RequestBody AjusteStockSolicitud solicitud) {
        return productoServicio.ajustarStock(idProducto, solicitud);
    }
}