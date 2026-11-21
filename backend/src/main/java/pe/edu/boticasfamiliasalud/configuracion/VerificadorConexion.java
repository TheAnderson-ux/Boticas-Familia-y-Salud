package pe.edu.boticasfamiliasalud.configuracion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Prueba minima de conexion a SQL Server.
 *
 * Al iniciar la aplicacion toma una conexion del pool y ejecuta una consulta
 * trivial. Sirve para confirmar que la autenticacion integrada de Windows
 * y la configuracion del origen de datos son correctas. No crea tablas
 * ni usa entidades de negocio.
 */
@Component
public class VerificadorConexion implements CommandLineRunner {

    private static final Logger registro = LoggerFactory.getLogger(VerificadorConexion.class);

    private final DataSource origenDeDatos;

    public VerificadorConexion(DataSource origenDeDatos) {
        this.origenDeDatos = origenDeDatos;
    }

    @Override
    public void run(String... argumentos) throws Exception {
        try (Connection conexion = origenDeDatos.getConnection();
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("SELECT 1");
            registro.info("CONEXION A SQL SERVER EXITOSA: bd_boticas_familia_salud");
        }
    }
}