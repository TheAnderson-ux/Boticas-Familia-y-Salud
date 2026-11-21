package pe.edu.boticasfamiliasalud.dto.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Solicitud para ajustar el stock de un producto.
 *
 * Es un ajuste administrativo temporal. No crea movimientos de inventario
 * ni tabla de kardex. No permite que el stock final quede negativo.
 */
public class AjusteStockSolicitud {

    @NotNull(message = "La cantidad es obligatoria")
    private Integer cantidad;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 200, message = "El motivo no puede superar 200 caracteres")
    private String motivo;

    public AjusteStockSolicitud() {
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}