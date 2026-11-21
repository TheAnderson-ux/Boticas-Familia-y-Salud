package pe.edu.boticasfamiliasalud.servicio;

import pe.edu.boticasfamiliasalud.dto.producto.AjusteStockSolicitud;
import pe.edu.boticasfamiliasalud.dto.producto.CambioEstadoProductoSolicitud;
import pe.edu.boticasfamiliasalud.dto.producto.ProductoRespuesta;
import pe.edu.boticasfamiliasalud.dto.producto.ProductoSolicitud;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoProducto;

import java.util.List;

/**
 * Servicio de productos.
 */
public interface ProductoServicio {

    List<ProductoRespuesta> listarProductos();

    List<ProductoRespuesta> buscarProductos(String termino, EstadoProducto estado);

    ProductoRespuesta obtenerProductoPorId(Long idProducto);

    ProductoRespuesta crearProducto(ProductoSolicitud solicitud);

    ProductoRespuesta actualizarProducto(Long idProducto, ProductoSolicitud solicitud);

    ProductoRespuesta cambiarEstadoProducto(Long idProducto, CambioEstadoProductoSolicitud solicitud);

    ProductoRespuesta ajustarStock(Long idProducto, AjusteStockSolicitud solicitud);
}