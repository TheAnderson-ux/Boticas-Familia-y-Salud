package pe.edu.boticasfamiliasalud.dto.reporte;

import java.math.BigDecimal;

/**
 * Ventas agrupadas por metodo de pago en un periodo.
 */
public class VentasPorMetodoReporte {

    private String metodoPago;

    private String etiqueta;

    private Long cantidadVentas;

    private BigDecimal importe;

    public VentasPorMetodoReporte() {
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public Long getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(Long cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }
}