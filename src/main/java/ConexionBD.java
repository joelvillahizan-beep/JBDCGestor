import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static String variable(String nombre, String defecto) {
        String valor = System.getenv(nombre);
        return valor == null ? defecto : valor;
    }

    public static Connection getConexion() throws SQLException {
        String url = variable("DB_URL",
                "jdbc:mysql://localhost:3306/recetario?connectionTimeZone=Europe/Madrid");
        String usuario = variable("DB_USER", "root");
        String password = variable("DB_PASSWORD", "");
        return DriverManager.getConnection(url, usuario, password);
    }
}
