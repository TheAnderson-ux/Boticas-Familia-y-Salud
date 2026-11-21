package pe.edu.boticasfamiliasalud.servicio.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.boticasfamiliasalud.dto.configuracion.ConfiguracionRespuesta;
import pe.edu.boticasfamiliasalud.dto.configuracion.ConfiguracionSolicitud;
import pe.edu.boticasfamiliasalud.entidad.ConfiguracionBotica;
import pe.edu.boticasfamiliasalud.repositorio.ConfiguracionBoticaRepositorio;
import pe.edu.boticasfamiliasalud.servicio.ConfiguracionServicio;

import java.math.BigDecimal;

/**
 * Implementacion del servicio de configuracion del sistema.
 *
 * La configuracion es una unica fila. Si no existe al consultar, se crea
 * con los valores por defecto. Al guardar solo se aplican los campos no nulos.
 */
@Service
public class ConfiguracionServicioImpl implements ConfiguracionServicio {

    private final ConfiguracionBoticaRepositorio configuracionRepositorio;

    public ConfiguracionServicioImpl(ConfiguracionBoticaRepositorio configuracionRepositorio) {
        this.configuracionRepositorio = configuracionRepositorio;
    }

    @Override
    @Transactional
    public ConfiguracionRespuesta obtenerConfiguracion() {
        ConfiguracionBotica configuracion = configuracionRepositorio
                .findTopByOrderByIdConfiguracionAsc()
                .orElseGet(this::crearPorDefecto);
        return aRespuesta(configuracion);
    }

    private ConfiguracionBotica crearPorDefecto() {
        ConfiguracionBotica configuracion = new ConfiguracionBotica();
        configuracion.setNombreComercial("BOTICAS FAMILIA & SALUD");
        configuracion.setRazonSocial("Boticas Familia y Salud S.A.C.");
        configuracion.setRuc("20123456789");
        configuracion.setDireccion("Av. Principal 123, Lima");
        configuracion.setTelefono("(01) 555-1234");
        configuracion.setCorreo("contacto@boticasfamiliasalud.pe");
        configuracion.setHorario("Lun - Dom: 8:00 a. m. - 10:00 p. m.");
        configuracion.setLogo(null);
        configuracion.setTema("claro");
        configuracion.setFormatoFecha("dd/mm/aaaa");
        configuracion.setFormatoHora("12");
        configuracion.setNotificacionesActivas(false);
        configuracion.setConfirmarCancelacionVenta(true);
        configuracion.setAplicarIgv(false);
        configuracion.setPorcentajeIgv(new BigDecimal("18"));
        configuracion.setMetodoPagoPredeterminado("efectivo");
        configuracion.setClienteGenerico(true);
        configuracion.setMostrarComprobanteAuto(true);
        configuracion.setStockMinimo(5);
        return configuracionRepositorio.save(configuracion);
    }

    @Override
    @Transactional
    public ConfiguracionRespuesta guardarConfiguracion(ConfiguracionSolicitud solicitud) {
        ConfiguracionBotica configuracion = configuracionRepositorio
                .findTopByOrderByIdConfiguracionAsc()
                .orElseGet(this::crearPorDefecto);

        aplicarSiNoNulo(solicitud.getNombreComercial(), configuracion::setNombreComercial);
        aplicarSiNoNulo(solicitud.getRazonSocial(), configuracion::setRazonSocial);
        aplicarSiNoNulo(solicitud.getRuc(), configuracion::setRuc);
        aplicarSiNoNulo(solicitud.getDireccion(), configuracion::setDireccion);
        aplicarSiNoNulo(solicitud.getTelefono(), configuracion::setTelefono);
        aplicarSiNoNulo(solicitud.getCorreo(), configuracion::setCorreo);
        aplicarSiNoNulo(solicitud.getHorario(), configuracion::setHorario);
        aplicarSiNoNulo(solicitud.getLogo(), configuracion::setLogo);
        aplicarSiNoNulo(solicitud.getTema(), configuracion::setTema);
        aplicarSiNoNulo(solicitud.getFormatoFecha(), configuracion::setFormatoFecha);
        aplicarSiNoNulo(solicitud.getFormatoHora(), configuracion::setFormatoHora);
        aplicarSiNoNulo(solicitud.getNotificacionesActivas(), configuracion::setNotificacionesActivas);
        aplicarSiNoNulo(solicitud.getConfirmarCancelacionVenta(), configuracion::setConfirmarCancelacionVenta);
        aplicarSiNoNulo(solicitud.getAplicarIgv(), configuracion::setAplicarIgv);
        aplicarSiNoNulo(solicitud.getPorcentajeIgv(), configuracion::setPorcentajeIgv);
        aplicarSiNoNulo(solicitud.getMetodoPagoPredeterminado(), configuracion::setMetodoPagoPredeterminado);
        aplicarSiNoNulo(solicitud.getClienteGenerico(), configuracion::setClienteGenerico);
        aplicarSiNoNulo(solicitud.getMostrarComprobanteAuto(), configuracion::setMostrarComprobanteAuto);
        aplicarSiNoNulo(solicitud.getStockMinimo(), configuracion::setStockMinimo);

        return aRespuesta(configuracionRepositorio.save(configuracion));
    }

    private <T> void aplicarSiNoNulo(T valor, java.util.function.Consumer<T> asignador) {
        if (valor != null) {
            asignador.accept(valor);
        }
    }

    private ConfiguracionRespuesta aRespuesta(ConfiguracionBotica configuracion) {
        ConfiguracionRespuesta respuesta = new ConfiguracionRespuesta();
        respuesta.setIdConfiguracion(configuracion.getIdConfiguracion());
        respuesta.setNombreComercial(configuracion.getNombreComercial());
        respuesta.setRazonSocial(configuracion.getRazonSocial());
        respuesta.setRuc(configuracion.getRuc());
        respuesta.setDireccion(configuracion.getDireccion());
        respuesta.setTelefono(configuracion.getTelefono());
        respuesta.setCorreo(configuracion.getCorreo());
        respuesta.setHorario(configuracion.getHorario());
        respuesta.setLogo(configuracion.getLogo());
        respuesta.setTema(configuracion.getTema());
        respuesta.setFormatoFecha(configuracion.getFormatoFecha());
        respuesta.setFormatoHora(configuracion.getFormatoHora());
        respuesta.setNotificacionesActivas(configuracion.getNotificacionesActivas());
        respuesta.setConfirmarCancelacionVenta(configuracion.getConfirmarCancelacionVenta());
        respuesta.setAplicarIgv(configuracion.getAplicarIgv());
        respuesta.setPorcentajeIgv(configuracion.getPorcentajeIgv());
        respuesta.setMetodoPagoPredeterminado(configuracion.getMetodoPagoPredeterminado());
        respuesta.setClienteGenerico(configuracion.getClienteGenerico());
        respuesta.setMostrarComprobanteAuto(configuracion.getMostrarComprobanteAuto());
        respuesta.setStockMinimo(configuracion.getStockMinimo());
        return respuesta;
    }
}