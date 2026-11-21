package pe.edu.boticasfamiliasalud.enumeracion;

/**
 * Estados de un producto del sistema.
 * Se almacenan en base de datos como texto (EnumType.STRING), nunca como ordinal.
 */
public enum EstadoProducto {
    ACTIVO,
    INACTIVO
}