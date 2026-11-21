package pe.edu.boticasfamiliasalud.dto.venta;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/**
 * Item de la solicitud de registro de venta.
 * El precio y el subtotal se toman del producto real en la base de datos.
 */
public class ItemVentaSolicitud {

    @NotNull(message = "El id del producto es obligatorio")
    private Long idProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    public ItemVentaSolicitud() {
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}