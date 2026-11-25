import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {
    private static final String SERVIDOR = "127.0.0.1";
    private static final String PUERTO = "3306";
    private static final String BASE_DATOS = "recetario";
    private static final String URL = "jdbc:mysql://" + SERVIDOR + ":" + PUERTO + "/" + BASE_DATOS;
    private static final String USUARIO = "root";

    public static Connection getConexion() throws SQLException {
        String contrasena = System.getenv("MYSQL_PASSWORD");

        if (contrasena == null) {
            Properties configuracion = new Properties();
            Path fichero = Path.of("mysql.properties");

            try (Reader lector = Files.newBufferedReader(fichero, StandardCharsets.UTF_8)) {
                configuracion.load(lector);
                contrasena = configuracion.getProperty("contrasena");
            } catch (IOException e) {
                throw new SQLException("No se puede leer mysql.properties. "
                        + "Configura la contraseña de MySQL en la carpeta del proyecto.", e);
            }
        }

        if (contrasena == null) {
            throw new SQLException("Falta la propiedad contrasena en mysql.properties.");
        }

        return DriverManager.getConnection(URL, USUARIO, contrasena);
    }
}
