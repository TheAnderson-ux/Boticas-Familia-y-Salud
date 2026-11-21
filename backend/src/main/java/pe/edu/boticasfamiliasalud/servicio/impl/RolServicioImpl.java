package pe.edu.boticasfamiliasalud.servicio.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.boticasfamiliasalud.entidad.Rol;
import pe.edu.boticasfamiliasalud.enumeracion.RolUsuario;
import pe.edu.boticasfamiliasalud.excepcion.RecursoNoEncontradoExcepcion;
import pe.edu.boticasfamiliasalud.repositorio.RolRepositorio;
import pe.edu.boticasfamiliasalud.servicio.RolServicio;

/**
 * Implementacion del servicio de roles.
 */
@Service
public class RolServicioImpl implements RolServicio {

    private static final Logger registro = LoggerFactory.getLogger(RolServicioImpl.class);

    private final RolRepositorio rolRepositorio;

    public RolServicioImpl(RolRepositorio rolRepositorio) {
        this.rolRepositorio = rolRepositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public Rol obtenerRolPorNombre(RolUsuario nombreRol) {
        return rolRepositorio.findByNombre(nombreRol)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        "El rol " + nombreRol + " no existe"));
    }

    @Override
    @Transactional
    public void inicializarRoles() {
        crearRolSiNoExiste(
                RolUsuario.ADMINISTRADOR,
                "Acceso completo al sistema."
        );

        crearRolSiNoExiste(
                RolUsuario.VENDEDOR,
                "Registro y consulta de ventas."
        );

        crearRolSiNoExiste(
                RolUsuario.ALMACENERO,
                "Gestion de productos y control de inventario."
        );
    }

    private void crearRolSiNoExiste(RolUsuario nombre, String descripcion) {
        if (!rolRepositorio.existsByNombre(nombre)) {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            rol.setDescripcion(descripcion);
            rolRepositorio.save(rol);
            registro.info("Rol creado: {}", nombre);
        }
    }
}