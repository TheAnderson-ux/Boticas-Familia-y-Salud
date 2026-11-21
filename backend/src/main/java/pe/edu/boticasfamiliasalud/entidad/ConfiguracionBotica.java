package pe.edu.boticasfamiliasalud.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Entidad ConfiguracionBotica. Representa la tabla "configuracion_botica".
 *
 * Guarda una unica fila con los datos generales de la botica, las
 * preferencias del sistema y la configuracion de ventas (IGV y metodo de
 * pago predeterminado).
 */
@Entity
@Table(name = "configuracion_botica")
public class ConfiguracionBotica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_configuracion")
    private Long idConfiguracion;

    // Datos generales de la botica
    @Column(name = "nombre_comercial", nullable = false, length = 120)
    private String nombreComercial;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "ruc", nullable = false, length = 20)
    private String ruc;

    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;

    @Column(name = "telefono", nullable = false, length = 40)
    private String telefono;

    @Column(name = "correo", nullable = false, length = 120)
    private String correo;

    @Column(name = "horario", nullable = false, length = 120)
    private String horario;

    @Lob
    @Column(name = "logo")
    private String logo;

    // Preferencias del sistema
    @Column(name = "tema", nullable = false, length = 20)
    private String tema = "claro";

    @Column(name = "formato_fecha", nullable = false, length = 20)
    private String formatoFecha = "dd/mm/aaaa";

    @Column(name = "formato_hora", nullable = false, length = 10)
    private String formatoHora = "12";

    @Column(name = "notificaciones_activas", nullable = false)
    private Boolean notificacionesActivas = false;

    @Column(name = "confirmar_cancelacion_venta", nullable = false)
    private Boolean confirmarCancelacionVenta = true;

    // Configuracion de ventas
    @Column(name = "aplicar_igv", nullable = false)
    private Boolean aplicarIgv = false;

    @Column(name = "porcentaje_igv", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeIgv = new BigDecimal("18");

    @Column(name = "metodo_pago_predeterminado", nullable = false, length = 20)
    private String metodoPagoPredeterminado = "efectivo";

    @Column(name = "cliente_generico", nullable = false)
    private Boolean clienteGenerico = true;

    @Column(name = "mostrar_comprobante_auto", nullable = false)
    private Boolean mostrarComprobanteAuto = true;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 5;

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