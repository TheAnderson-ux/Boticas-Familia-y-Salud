package pe.edu.boticasfamiliasalud.enumeracion;

/**
 * Estados de un cliente del sistema.
 * Se almacenan en base de datos como texto (EnumType.STRING), nunca como ordinal.
 */
public enum EstadoCliente {
    ACTIVO,
    INACTIVO
}