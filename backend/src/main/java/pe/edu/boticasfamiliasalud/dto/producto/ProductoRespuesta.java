package pe.edu.boticasfamiliasalud.dto.producto;

import pe.edu.boticasfamiliasalud.enumeracion.EstadoProducto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Respuesta de producto.
 *
 * Campos calculados:
 * - stockBajo: true cuando stock es menor o igual a stockMinimo y mayor que 0.
 * - sinStock: true cuando stock es 0.
 */
public class ProductoRespuesta {

    private Long idProducto;

    private String codigo;

    private String nombre;

    private String categoria;

    private BigDecimal precio;

    private Integer stock;

    private Integer stockMinimo;

    private EstadoProducto estado;

    private LocalDateTime fechaRegistro;

    private boolean stockBajo;

    private boolean sinStock;

    public ProductoRespuesta() {
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public EstadoProducto getEstado() {
        return estado;
    }

    public void setEstado(EstadoProducto estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isStockBajo() {
        return stockBajo;
    }

    public void setStockBajo(boolean stockBajo) {
        this.stockBajo = stockBajo;
    }

    public boolean isSinStock() {
        return sinStock;
    }

    public void setSinStock(boolean sinStock) {
        this.sinStock = sinStock;
    }
}