package pe.edu.boticasfamiliasalud.dto.reporte;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Venta por dia: cantidad de ventas e importe para una fecha.
 */
public class VentasPorDiaReporte {

    private LocalDate fecha;

    private Long cantidadVentas;

    private BigDecimal total;

    public VentasPorDiaReporte() {
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Long getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(Long cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}