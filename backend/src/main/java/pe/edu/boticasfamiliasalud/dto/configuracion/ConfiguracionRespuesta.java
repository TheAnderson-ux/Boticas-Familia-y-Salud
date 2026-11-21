package pe.edu.boticasfamiliasalud.dto.configuracion;

import java.math.BigDecimal;

/**
 * Respuesta de la configuracion del sistema (una unica fila).
 * Mismos campos que la entidad ConfiguracionBotica.
 */
public class ConfiguracionRespuesta {

    private Long idConfiguracion;

    private String nombreComercial;

    private String razonSocial;

    private String ruc;

    private String direccion;

    private String telefono;

    private String correo;

    private String horario;

    private String logo;

    private String tema;

    private String formatoFecha;

    private String formatoHora;

    private Boolean notificacionesActivas;

    private Boolean confirmarCancelacionVenta;

    private Boolean aplicarIgv;

    private BigDecimal porcentajeIgv;

    private String metodoPagoPredeterminado;

    private Boolean clienteGenerico;

    private Boolean mostrarComprobanteAuto;

    private Integer stockMinimo;

    public ConfiguracionRespuesta() {
    }

    public Long getIdConfiguracion() {
        return idConfiguracion;
    }

    public void setIdConfiguracion(Long idConfiguracion) {
        this.idConfiguracion = idConfiguracion;
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