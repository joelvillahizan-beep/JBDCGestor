import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ComprobarRecetas {
    private static final String NOMBRE_PRUEBA = "Receta de prueba JDBC";

    private static int consultarNumero(String sql) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement sentencia = con.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            if (!resultado.next()) {
                throw new SQLException("No se ha obtenido el dato esperado.");
            }
            return resultado.getInt(1);
        }
    }

    private static void comprobarTotales(int esperado) throws SQLException {
        int recetas = consultarNumero("SELECT COUNT(*) FROM recetas");
        int estadisticas = consultarNumero(
                "SELECT total_recetas FROM estadisticas WHERE id = 1");
        System.out.println("Recetas en la tabla: " + recetas
                + " | Total en estadisticas: " + estadisticas);
        if (recetas != esperado || estadisticas != esperado) {
            throw new IllegalStateException("Los totales no coinciden.");
        }
    }

    private static int buscarIdPrueba() throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement sentencia = con.prepareStatement(
                     "SELECT id FROM recetas WHERE nombre = ?")) {
            sentencia.setString(1, NOMBRE_PRUEBA);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? resultado.getInt("id") : -1;
            }
        }
    }

    private static void consultas() {
        GestionRecetas.main(new String[0]);
        System.out.println("\nBUSCAR UN IDENTIFICADOR QUE NO EXISTE");
        GestionRecetas.buscarPorId(-1);
    }

    private static void cambios() throws SQLException {
        int totalInicial = consultarNumero("SELECT COUNT(*) FROM recetas");
        comprobarTotales(totalInicial);
        if (buscarIdPrueba() != -1) {
            throw new IllegalStateException("Ya existe una receta con el nombre de prueba.");
        }

        int idPrueba = -1;
        try {
            System.out.println("\nAÑADIR RECETA Y CONFIRMAR LA TRANSACCION");
            GestionRecetas.anadirReceta(new Receta(
                    NOMBRE_PRUEBA, "Prueba", 20, "Fácil", 2));
            idPrueba = buscarIdPrueba();
            if (idPrueba == -1) throw new IllegalStateException("No se ha añadido la receta.");
            GestionRecetas.buscarPorId(idPrueba);
            comprobarTotales(totalInicial + 1);

            System.out.println("\nMODIFICAR A 45 MINUTOS Y 6 RACIONES");
            GestionRecetas.modificarReceta(idPrueba, 45, 6);
            GestionRecetas.buscarPorId(idPrueba);
            try (Connection con = ConexionBD.getConexion();
                 PreparedStatement sentencia = con.prepareStatement(
                         "SELECT tiempo_preparacion, raciones FROM recetas WHERE id = ?")) {
                sentencia.setInt(1, idPrueba);
                try (ResultSet resultado = sentencia.executeQuery()) {
                    if (!resultado.next() || resultado.getInt(1) != 45 || resultado.getInt(2) != 6) {
                        throw new IllegalStateException("La modificación no se ha guardado.");
                    }
                }
            }
        } finally {
            if (idPrueba != -1) {
                System.out.println("\nELIMINAR SOLO LA RECETA DE PRUEBA");
                GestionRecetas.eliminarReceta(idPrueba);
                GestionRecetas.buscarPorId(idPrueba);
            }
        }
        comprobarTotales(totalInicial);
        System.out.println("\nComprobación correcta. Las recetas originales se conservan.");
    }

    private static void errores() throws SQLException {
        int totalInicial = consultarNumero("SELECT COUNT(*) FROM recetas");
        System.out.println("ANTES DEL ERROR");
        comprobarTotales(totalInicial);
        System.out.println("\nINTENTO AÑADIR UNA RECETA SIN NOMBRE");
        GestionRecetas.anadirReceta(new Receta(null, "Prueba", 10, "Fácil", 2));
        System.out.println("\nDESPUES DEL ROLLBACK");
        comprobarTotales(totalInicial);
        System.out.println("\nComprobación correcta. No se ha añadido la receta sin nombre.");
    }

    public static void main(String[] args) throws SQLException {
        System.out.println("Usuario de Windows: " + System.getProperty("user.name"));
        System.out.println("Fecha y hora: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        try (Connection con = ConexionBD.getConexion()) {
            System.out.println("Base de datos: " + con.getCatalog()
                    + " | Usuario MySQL: " + con.getMetaData().getUserName());
        }
        System.out.println();
        String prueba = args.length == 0 ? "consultas" : args[0];
        switch (prueba) {
            case "consultas": consultas(); break;
            case "cambios": cambios(); break;
            case "errores": errores(); break;
            default: throw new IllegalArgumentException("Prueba no reconocida.");
        }
    }
}
