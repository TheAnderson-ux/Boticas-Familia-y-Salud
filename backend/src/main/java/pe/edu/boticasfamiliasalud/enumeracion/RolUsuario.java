package pe.edu.boticasfamiliasalud.enumeracion;

/**
 * Roles del sistema de la botica.
 * Se almacenan en base de datos como texto (EnumType.STRING), nunca como ordinal.
 */
public enum RolUsuario {
    ADMINISTRADOR,
    VENDEDOR, ALMACENERO
}