import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String SERVIDOR = "localhost";
    private static final String PUERTO = "3306";
    private static final String BASE_DATOS = "recetario";
    private static final String URL = "jdbc:mysql://" + SERVIDOR + ":" + PUERTO + "/" + BASE_DATOS;
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "";

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}
