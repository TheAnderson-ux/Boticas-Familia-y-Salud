package pe.edu.boticasfamiliasalud.enumeracion;

/**
 * Estados de una venta del sistema.
 * Se almacenan en base de datos como texto (EnumType.STRING), nunca como ordinal.
 */
public enum EstadoVenta {
    COMPLETADA,
    PENDIENTE,
    ANULADA
}