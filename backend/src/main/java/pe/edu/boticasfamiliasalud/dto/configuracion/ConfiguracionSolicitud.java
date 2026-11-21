package pe.edu.boticasfamiliasalud.dto.configuracion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Solicitud para guardar la configuracion del sistema.
 * Solo se procesan los campos no nulos; los demas conservan su valor.
 */
public class ConfiguracionSolicitud {

    @NotBlank(message = "El nombre comercial es obligatorio")
    @Size(max = 120, message = "El nombre comercial no puede superar 120 caracteres")
    private String nombreComercial;

    @NotBlank(message = "La razon social es obligatoria")
    @Size(max = 150, message = "La razon social no puede superar 150 caracteres")
    private String razonSocial;

    @NotBlank(message = "El RUC es obligatorio")
    @Size(max = 20, message = "El RUC no puede superar 20 caracteres")
    private String ruc;

    @NotBlank(message = "La direccion es obligatoria")
    @Size(max = 200, message = "La direccion no puede superar 200 caracteres")
    private String direccion;

    @NotBlank(message = "El telefono es obligatorio")
    @Size(max = 40, message = "El telefono no puede superar 40 caracteres")
    private String telefono;

    @NotBlank(message = "El correo es obligatorio")
    @Size(max = 120, message = "El correo no puede superar 120 caracteres")
    private String correo;

    @NotBlank(message = "El horario es obligatorio")
    @Size(max = 120, message = "El horario no puede superar 120 caracteres")
    private String horario;

    private String logo;

    @NotBlank(message = "El tema es obligatorio")
    @Size(max = 20, message = "El tema no puede superar 20 caracteres")
    private String tema;

    @NotBlank(message = "El formato de fecha es obligatorio")
    @Size(max = 20, message = "El formato de fecha no puede superar 20 caracteres")
    private String formatoFecha;

    @NotBlank(message = "El formato de hora es obligatorio")
    @Size(max = 10, message = "El formato de hora no puede superar 10 caracteres")
    private String formatoHora;

    private Boolean notificacionesActivas;

    private Boolean confirmarCancelacionVenta;

    private Boolean aplicarIgv;

    @DecimalMin(value = "0.0", inclusive = true, message = "El porcentaje de IGV no puede ser negativo")
    private BigDecimal porcentajeIgv;

    @NotBlank(message = "El metodo de pago predeterminado es obligatorio")
    @Size(max = 20, message = "El metodo de pago no puede superar 20 caracteres")
    private String metodoPagoPredeterminado;

    private Boolean clienteGenerico;

    private Boolean mostrarComprobanteAuto;

    @NotNull(message = "El stock minimo es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El stock minimo no puede ser negativo")
    private Integer stockMinimo;

    public ConfiguracionSolicitud() {
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getFormatoFecha() {
        return formatoFecha;
    }

    public void setFormatoFecha(String formatoFecha) {
        this.formatoFecha = formatoFecha;
    }

    public String getFormatoHora() {
        return formatoHora;
    }

    public void setFormatoHora(String formatoHora) {
        this.formatoHora = formatoHora;
    }

    public Boolean getNotificacionesActivas() {
        return notificacionesActivas;
    }

    public void setNotificacionesActivas(Boolean notificacionesActivas) {
        this.notificacionesActivas = notificacionesActivas;
    }

    public Boolean getConfirmarCancelacionVenta() {
        return confirmarCancelacionVenta;
    }

    public void setConfirmarCancelacionVenta(Boolean confirmarCancelacionVenta) {
        this.confirmarCancelacionVenta = confirmarCancelacionVenta;
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

    public String getMetodoPagoPredeterminado() {
        return metodoPagoPredeterminado;
    }

    public void setMetodoPagoPredeterminado(String metodoPagoPredeterminado) {
        this.metodoPagoPredeterminado = metodoPagoPredeterminado;
    }

    public Boolean getClienteGenerico() {
        return clienteGenerico;
    }

    public void setClienteGenerico(Boolean clienteGenerico) {
        this.clienteGenerico = clienteGenerico;
    }

    public Boolean getMostrarComprobanteAuto() {
        return mostrarComprobanteAuto;
    }

    public void setMostrarComprobanteAuto(Boolean mostrarComprobanteAuto) {
        this.mostrarComprobanteAuto = mostrarComprobanteAuto;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
}