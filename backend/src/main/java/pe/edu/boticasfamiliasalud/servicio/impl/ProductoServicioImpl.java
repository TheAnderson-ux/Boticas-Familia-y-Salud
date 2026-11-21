package pe.edu.boticasfamiliasalud.servicio.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.boticasfamiliasalud.dto.producto.AjusteStockSolicitud;
import pe.edu.boticasfamiliasalud.dto.producto.CambioEstadoProductoSolicitud;
import pe.edu.boticasfamiliasalud.dto.producto.ProductoRespuesta;
import pe.edu.boticasfamiliasalud.dto.producto.ProductoSolicitud;
import pe.edu.boticasfamiliasalud.entidad.Producto;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoProducto;
import pe.edu.boticasfamiliasalud.excepcion.RecursoNoEncontradoExcepcion;
import pe.edu.boticasfamiliasalud.excepcion.ReglaNegocioExcepcion;
import pe.edu.boticasfamiliasalud.repositorio.ProductoRepositorio;
import pe.edu.boticasfamiliasalud.servicio.ProductoServicio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementacion del servicio de productos.
 */
@Service
public class ProductoServicioImpl implements ProductoServicio {

    private static final Logger registro = LoggerFactory.getLogger(ProductoServicioImpl.class);

    private final ProductoRepositorio productoRepositorio;

    public ProductoServicioImpl(ProductoRepositorio productoRepositorio) {
        this.productoRepositorio = productoRepositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoRespuesta> listarProductos() {
        return convertirLista(productoRepositorio.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoRespuesta> buscarProductos(String termino, EstadoProducto estado) {
        String busqueda = termino == null ? null : termino.trim();

        List<Producto> coincidencias = new ArrayList<>();
        if (busqueda == null || busqueda.isEmpty()) {
            if (estado != null) {
                coincidencias.addAll(productoRepositorio.findByEstado(estado));
            } else {
                coincidencias.addAll(productoRepositorio.findAll());
            }
        } else {
            coincidencias.addAll(productoRepositorio.findByNombreContainingIgnoreCase(busqueda));
            coincidencias.addAll(productoRepositorio.findByCategoriaContainingIgnoreCase(busqueda));
            productoRepositorio.findByCodigoIgnoreCase(busqueda).ifPresent(coincidencias::add);
        }

        Map<Long, Producto> unicos = new LinkedHashMap<>();
        for (Producto producto : coincidencias) {
            unicos.putIfAbsent(producto.getIdProducto(), producto);
        }

        List<Producto> resultado = new ArrayList<>(unicos.values());
        if (estado != null) {
            resultado = resultado.stream()
                    .filter(producto -> producto.getEstado() == estado)
                    .collect(Collectors.toList());
        }

        resultado.sort(Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER));
        return convertirLista(resultado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoRespuesta obtenerProductoPorId(Long idProducto) {
        return convertir(buscarProducto(idProducto));
    }

    @Override
    @Transactional
    public ProductoRespuesta crearProducto(ProductoSolicitud solicitud) {
        String codigo = normalizarCodigo(solicitud.getCodigo());

        if (productoRepositorio.existsByCodigoIgnoreCase(codigo)) {
            throw new ReglaNegocioExcepcion("Ya existe un producto con ese codigo.");
        }

        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(limpiarEspacios(solicitud.getNombre()));
        producto.setCategoria(limpiarEspacios(solicitud.getCategoria()));
        producto.setPrecio(solicitud.getPrecio());
        producto.setStock(solicitud.getStock());
        producto.setStockMinimo(solicitud.getStockMinimo());
        producto.setEstado(solicitud.getEstado() != null ? solicitud.getEstado() : EstadoProducto.ACTIVO);
        producto.setFechaRegistro(java.time.LocalDateTime.now());

        return convertir(productoRepositorio.save(producto));
    }

    @Override
    @Transactional
    public ProductoRespuesta actualizarProducto(Long idProducto, ProductoSolicitud solicitud) {
        Producto producto = buscarProducto(idProducto);

        String codigo = normalizarCodigo(solicitud.getCodigo());
        if (productoRepositorio.existsByCodigoIgnoreCaseAndIdProductoNot(codigo, idProducto)) {
            throw new ReglaNegocioExcepcion("Ya existe otro producto con ese codigo.");
        }

        producto.setCodigo(codigo);
        producto.setNombre(limpiarEspacios(solicitud.getNombre()));
        producto.setCategoria(limpiarEspacios(solicitud.getCategoria()));
        producto.setPrecio(solicitud.getPrecio());
        producto.setStock(solicitud.getStock());
        producto.setStockMinimo(solicitud.getStockMinimo());
        if (solicitud.getEstado() != null) {
            producto.setEstado(solicitud.getEstado());
        }

        return convertir(productoRepositorio.save(producto));
    }

    @Override
    @Transactional
    public ProductoRespuesta cambiarEstadoProducto(Long idProducto, CambioEstadoProductoSolicitud solicitud) {
        Producto producto = buscarProducto(idProducto);
        producto.setEstado(solicitud.getEstado());
        return convertir(productoRepositorio.save(producto));
    }

    @Override
    @Transactional
    public ProductoRespuesta ajustarStock(Long idProducto, AjusteStockSolicitud solicitud) {
        Producto producto = buscarProducto(idProducto);

        int nuevoStock = producto.getStock() + solicitud.getCantidad();
        if (nuevoStock < 0) {
            throw new ReglaNegocioExcepcion("El ajuste no puede dejar el stock en un valor negativo.");
        }

        producto.setStock(nuevoStock);
        ProductoRespuesta resultado = convertir(productoRepositorio.save(producto));

        registro.info("Ajuste de stock temporal | producto={} | codigo={} | cantidad={} | nuevoStock={} | motivo={}",
                producto.getIdProducto(), producto.getCodigo(), solicitud.getCantidad(),
                producto.getStock(), solicitud.getMotivo());

        return resultado;
    }

    private Producto buscarProducto(Long idProducto) {
        return productoRepositorio.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        "No se encontro el producto con id " + idProducto + "."));
    }

    private String normalizarCodigo(String codigo) {
        return limpiarEspacios(codigo).toUpperCase(Locale.ROOT);
    }

    private String limpiarEspacios(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private List<ProductoRespuesta> convertirLista(List<Producto> productos) {
        return productos.stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    private ProductoRespuesta convertir(Producto producto) {
        ProductoRespuesta respuesta = new ProductoRespuesta();
        respuesta.setIdProducto(producto.getIdProducto());
        respuesta.setCodigo(producto.getCodigo());
        respuesta.setNombre(producto.getNombre());
        respuesta.setCategoria(producto.getCategoria());
        respuesta.setPrecio(producto.getPrecio());
        respuesta.setStock(producto.getStock());
        respuesta.setStockMinimo(producto.getStockMinimo());
        respuesta.setEstado(producto.getEstado());
        respuesta.setFechaRegistro(producto.getFechaRegistro());

        int stock = producto.getStock();
        respuesta.setStockBajo(stock > 0 && stock <= producto.getStockMinimo());
        respuesta.setSinStock(stock == 0);
        return respuesta;
    }
}