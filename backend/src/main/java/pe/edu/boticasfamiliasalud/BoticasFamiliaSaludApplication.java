package pe.edu.boticasfamiliasalud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del backend de Boticas Familia & Salud.

 * La auto-configuracion de JPA y del origen de datos ya esta activa:
 * Spring Boot se conecta a SQL Server mediante autenticacion integrada
 * de Windows (ver application.properties).
 */
@SpringBootApplication
public class BoticasFamiliaSaludApplication {

    /**
     * Punto de entrada de la aplicacion.
     */
    public static void main(String[] args) {
        SpringApplication.run(BoticasFamiliaSaludApplication.class, args);
    }
}