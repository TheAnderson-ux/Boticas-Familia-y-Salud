package pe.edu.boticasfamiliasalud.dto.reporte;

import java.math.BigDecimal;

/**
 * Producto mas vendido en un periodo: cantidad e importe acumulado.
 */
public class ProductosMasVendidosReporte {

    private Long idProducto;

    private String codigo;

    private String nombre;

    private Integer cantidad;

    private BigDecimal importe;

    public ProductosMasVendidosReporte() {
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

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }
}