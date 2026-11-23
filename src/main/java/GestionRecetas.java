import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GestionRecetas {

    private static void mostrarDatos(ResultSet rs) throws SQLException {
        System.out.println(
                rs.getInt("id") + " | " +
                rs.getString("nombre") + " | " +
                rs.getString("categoria") + " | " +
                rs.getInt("tiempo_preparacion") + " min | " +
                rs.getString("dificultad") + " | " +
                rs.getInt("raciones") + " raciones");
    }

    public static void mostrarRecetas() {
        String sql = "SELECT * FROM recetas";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                mostrarDatos(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error al mostrar recetas: " + e.getMessage());
        }
    }

    public static void buscarPorId(int id) {
        String sql = "SELECT * FROM recetas WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    mostrarDatos(rs);
                } else {
                    System.out.println("No se ha encontrado la receta.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar receta: " + e.getMessage());
        }
    }

    public static void anadirReceta(Receta receta) {
        String sql = "INSERT INTO recetas (nombre, categoria, tiempo_preparacion, dificultad, raciones) "
                + "VALUES (?, ?, ?, ?, ?)";
        String sqlTotal = "UPDATE estadisticas SET total_recetas = total_recetas + 1 WHERE id = 1";

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);

            try {
                int filasReceta;
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, receta.nombre);
                    pstmt.setString(2, receta.categoria);
                    pstmt.setInt(3, receta.tiempoPreparacion);
                    pstmt.setString(4, receta.dificultad);
                    pstmt.setInt(5, receta.raciones);
                    filasReceta = pstmt.executeUpdate();
                }

                int filasTotal = 0;
                if (filasReceta == 1) {
                    try (PreparedStatement pstmt = con.prepareStatement(sqlTotal)) {
                        filasTotal = pstmt.executeUpdate();
                    }
                }

                if (filasReceta == 1 && filasTotal == 1) {
                    con.commit();
                    System.out.println("Receta añadida. Transacción confirmada.");
                } else {
                    con.rollback();
                    System.out.println("Transacción cancelada.");
                }
            } catch (SQLException e) {
                con.rollback();
                System.out.println("Error: " + e.getMessage());
                System.out.println("Transacción cancelada.");
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión o transacción: " + e.getMessage());
        }
    }

    public static void modificarReceta(int id, int tiempo, int raciones) {
        String sql = "UPDATE recetas SET tiempo_preparacion = ?, raciones = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, tiempo);
            pstmt.setInt(2, raciones);
            pstmt.setInt(3, id);

            int filas = pstmt.executeUpdate();
            System.out.println("Recetas modificadas: " + filas);
        } catch (SQLException e) {
            System.out.println("Error al modificar receta: " + e.getMessage());
        }
    }

    public static void eliminarReceta(int id) {
        String sql = "DELETE FROM recetas WHERE id = ?";
        String sqlTotal = "UPDATE estadisticas SET total_recetas = total_recetas - 1 WHERE id = 1";

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);

            try {
                int filas;
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setInt(1, id);
                    filas = pstmt.executeUpdate();
                }

                int filasTotal = 1;
                if (filas == 1) {
                    try (PreparedStatement pstmt = con.prepareStatement(sqlTotal)) {
                        filasTotal = pstmt.executeUpdate();
                    }
                }

                if (filasTotal == 1) {
                    con.commit();
                    System.out.println("Recetas eliminadas: " + filas);
                } else {
                    con.rollback();
                    System.out.println("Transacción cancelada.");
                }
            } catch (SQLException e) {
                con.rollback();
                System.out.println("Error: " + e.getMessage());
                System.out.println("Transacción cancelada.");
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión o transacción: " + e.getMessage());
        }
    }

    public static void buscarPorNombre(String nombre) {
        String sql = "{CALL buscarReceta(?)}";

        try (Connection con = ConexionBD.getConexion();
             CallableStatement cstmt = con.prepareCall(sql)) {
            cstmt.setString(1, nombre);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    mostrarDatos(rs);
                } else {
                    System.out.println("No se ha encontrado la receta.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar por nombre: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("LISTADO DE RECETAS");
        mostrarRecetas();

        System.out.println("\nBUSCAR POR IDENTIFICADOR");
        buscarPorId(1);

        System.out.println("\nBUSCAR POR NOMBRE");
        buscarPorNombre("Tortilla de patatas");
    }
}
