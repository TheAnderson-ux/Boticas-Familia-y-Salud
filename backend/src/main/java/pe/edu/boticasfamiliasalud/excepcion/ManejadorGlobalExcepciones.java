package pe.edu.boticasfamiliasalud.excepcion;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones: responde errores en JSON, en espanol,
 * sin exponer detalles internos sensibles (ni contraseñas, ni conexiones).
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final Logger registro = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    @ExceptionHandler(RecursoNoEncontradoExcepcion.class)
    public ResponseEntity<RespuestaError> manejarRecursoNoEncontrado(
            RecursoNoEncontradoExcepcion excepcion, HttpServletRequest peticion) {
        return construirRespuesta(HttpStatus.NOT_FOUND, excepcion.getMessage(), peticion);
    }

    @ExceptionHandler(ReglaNegocioExcepcion.class)
    public ResponseEntity<RespuestaError> manejarReglaNegocio(
            ReglaNegocioExcepcion excepcion, HttpServletRequest peticion) {
        return construirRespuesta(HttpStatus.CONFLICT, excepcion.getMessage(), peticion);
    }

    @ExceptionHandler(CredencialesInvalidasExcepcion.class)
    public ResponseEntity<RespuestaError> manejarCredencialesInvalidas(
            CredencialesInvalidasExcepcion excepcion, HttpServletRequest peticion) {
        return construirRespuesta(HttpStatus.UNAUTHORIZED, excepcion.getMessage(), peticion);
    }

    @ExceptionHandler(AccesoDenegadoExcepcion.class)
    public ResponseEntity<RespuestaError> manejarAccesoDenegado(
            AccesoDenegadoExcepcion excepcion, HttpServletRequest peticion) {
        return construirRespuesta(HttpStatus.FORBIDDEN, excepcion.getMessage(), peticion);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarValidacion(
            MethodArgumentNotValidException excepcion, HttpServletRequest peticion) {
        String detalle = excepcion.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        String mensaje = detalle.isBlank() ? "La solicitud no es valida" : detalle;
        return construirRespuesta(HttpStatus.BAD_REQUEST, mensaje, peticion);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespuestaError> manejarTipoArgumentoInvalido(
            MethodArgumentTypeMismatchException excepcion, HttpServletRequest peticion) {
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "Valor de parametro invalido: " + excepcion.getName(), peticion);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> manejarSolicitudInvalida(
            HttpMessageNotReadableException excepcion, HttpServletRequest peticion) {
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "La solicitud esta mal formada o contiene valores invalidos", peticion);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaError> manejarViolacionIntegridad(
            DataIntegrityViolationException excepcion, HttpServletRequest peticion) {
        return construirRespuesta(HttpStatus.CONFLICT,
                "Los datos enviados entran en conflicto con informacion existente", peticion);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarErrorInesperado(
            Exception excepcion, HttpServletRequest peticion) {
        registro.error("Error no controlado en {}", peticion.getRequestURI(), excepcion);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error inesperado en el servidor", peticion);
    }

    private ResponseEntity<RespuestaError> construirRespuesta(
            HttpStatus estadoHttp, String mensaje, HttpServletRequest peticion) {
        RespuestaError respuesta = new RespuestaError();
        respuesta.setFechaHora(LocalDateTime.now());
        respuesta.setEstado(estadoHttp.value());
        respuesta.setError(estadoHttp.getReasonPhrase());
        respuesta.setMensaje(mensaje);
        respuesta.setRuta(peticion.getRequestURI());
        return ResponseEntity.status(estadoHttp).body(respuesta);
    }
}