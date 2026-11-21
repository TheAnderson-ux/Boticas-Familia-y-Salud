package pe.edu.boticasfamiliasalud.excepcion;

import java.time.LocalDateTime;

/**
 * Estructura consistente de respuesta de error en JSON.
 * Nunca incluye contraseñas, conexiones ni detalles internos.
 */
public class RespuestaError {

    private LocalDateTime fechaHora;

    private int estado;

    private String error;

    private String mensaje;

    private String ruta;

    public RespuestaError() {
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
}