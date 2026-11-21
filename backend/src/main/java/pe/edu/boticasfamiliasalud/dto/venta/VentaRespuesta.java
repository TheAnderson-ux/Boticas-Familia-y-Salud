package pe.edu.boticasfamiliasalud.dto.venta;

import pe.edu.boticasfamiliasalud.enumeracion.EstadoVenta;
import pe.edu.boticasfamiliasalud.enumeracion.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta de una venta.
 *
 * Campo vendedor: nombre completo del usuario que registro la venta.
 * Los montos se calculan y almacenan en el servidor.
 */
public class VentaRespuesta {

    private Long idVenta;

    private String numeroVenta;

    private LocalDateTime fechaVenta;

    private String cliente;

    private Long idVendedor;

    private String vendedor;

    private MetodoPago metodoPago;

    private BigDecimal subtotal;

    private BigDecimal igv;

    private BigDecimal total;

    private BigDecimal montoRecibido;

    private BigDecimal vuelto;

    private EstadoVenta estado;

    private List<ItemVentaRespuesta> items = new ArrayList<>();

    public VentaRespuesta() {
    }

    public Long getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Long idVenta) {
        this.idVenta = idVenta;
    }

    public String getNumeroVenta() {
        return numeroVenta;
    }

    public void setNumeroVenta(String numeroVenta) {
        this.numeroVenta = numeroVenta;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public Long getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(Long idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIgv() {
        return igv;
    }

    public void setIgv(BigDecimal igv) {
        this.igv = igv;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(BigDecimal montoRecibido) {
        this.montoRecibido = montoRecibido;
    }

    public BigDecimal getVuelto() {
        return vuelto;
    }

    public void setVuelto(BigDecimal vuelto) {
        this.vuelto = vuelto;
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoVenta estado) {
        this.estado = estado;
    }

    public List<ItemVentaRespuesta> getItems() {
        return items;
    }

    public void setItems(List<ItemVentaRespuesta> items) {
        this.items = items;
    }
}