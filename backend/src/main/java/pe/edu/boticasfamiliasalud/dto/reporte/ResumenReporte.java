package pe.edu.boticasfamiliasalud.dto.reporte;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resumen de ventas de un periodo.
 */
public class ResumenReporte {

    private LocalDateTime desde;

    private LocalDateTime hasta;

    private BigDecimal totalIngresos;

    private Long totalVentas;

    private BigDecimal ticketPromedio;

    private Integer productosVendidos;

    public ResumenReporte() {
    }

    public LocalDateTime getDesde() {
        return desde;
    }

    public void setDesde(LocalDateTime desde) {
        this.desde = desde;
    }

    public LocalDateTime getHasta() {
        return hasta;
    }

    public void setHasta(LocalDateTime hasta) {
        this.hasta = hasta;
    }

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public Long getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(Long totalVentas) {
        this.totalVentas = totalVentas;
    }

    public BigDecimal getTicketPromedio() {
        return ticketPromedio;
    }

    public void setTicketPromedio(BigDecimal ticketPromedio) {
        this.ticketPromedio = ticketPromedio;
    }

    public Integer getProductosVendidos() {
        return productosVendidos;
    }

    public void setProductosVendidos(Integer productosVendidos) {
        this.productosVendidos = productosVendidos;
    }
}