package pe.edu.boticasfamiliasalud.enumeracion;

/**
 * Metodos de pago aceptados en las ventas del sistema.
 * Se almacenan en base de datos como texto (EnumType.STRING), nunca como ordinal.
 * Corresponden a los valores usados por el frontend (efectivo, tarjeta, yape, transferencia).
 */
public enum MetodoPago {
    EFECTIVO,
    TARJETA,
    YAPE,
    TRANSFERENCIA
}