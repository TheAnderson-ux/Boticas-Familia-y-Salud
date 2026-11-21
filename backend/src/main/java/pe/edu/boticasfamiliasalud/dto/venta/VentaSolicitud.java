package pe.edu.boticasfamiliasalud.dto.venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.boticasfamiliasalud.enumeracion.MetodoPago;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Solicitud para registrar una venta.
 *
 * Los montos (subtotal, igv, total, vuelto) NO se reciben del cliente:
 * el backend los calcula a partir del precio real de cada producto y de los
 * parametros de IGV indicados (aplicarIgv y porcentajeIgv).
 */
public class VentaSolicitud {

    @NotNull(message = "El id del vendedor es obligatorio")
    private Long idUsuario;

    @Size(max = 150, message = "El cliente no puede superar 150 caracteres")
    private String cliente;

    @NotNull(message = "El metodo de pago es obligatorio")
    private MetodoPago metodoPago;

    @DecimalMin(value = "0.0", inclusive = true, message = "El monto recibido no puede ser negativo")
    private BigDecimal montoRecibido;

    private Boolean aplicarIgv;

    @DecimalMin(value = "0.0", inclusive = true, message = "El porcentaje de IGV no puede ser negativo")
    private BigDecimal porcentajeIgv;

    @Valid
    @NotEmpty(message = "La venta debe incluir al menos un producto")
    private List<ItemVentaSolicitud> items = new ArrayList<>();

    public VentaSolicitud() {
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(BigDecimal montoRecibido) {
        this.montoRecibido = montoRecibido;
    }

    public Boolean getAplicarIgv() {
        return aplicarIgv;
    }

    public void setAplicarIgv(Boolean aplicarIgv) {
        this.aplicarIgv = aplicarIgv;
    }

    public BigDecimal getPorcentajeIgv() {
        return porcentajeIgv;
    }

    public void setPorcentajeIgv(BigDecimal porcentajeIgv) {
        this.porcentajeIgv = porcentajeIgv;
    }

    public List<ItemVentaSolicitud> getItems() {
        return items;
    }

    public void setItems(List<ItemVentaSolicitud> items) {
        this.items = items;
    }
}