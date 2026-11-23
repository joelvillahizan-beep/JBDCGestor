import java.sql.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GestionRecetas {
    private static final Scanner sc = new Scanner(System.in);
    private static final String CAMPOS =
            "id, nombre, categoria, tiempo_preparacion, dificultad, raciones";

    private static Receta leerReceta(ResultSet rs) throws SQLException {
        return new Receta(rs.getInt("id"), rs.getString("nombre"),
                rs.getString("categoria"), rs.getInt("tiempo_preparacion"),
                rs.getString("dificultad"), rs.getInt("raciones"));
    }

    public static List<Receta> listar() throws SQLException {
        List<Receta> recetas = new ArrayList<>();
        String sql = "SELECT " + CAMPOS + " FROM recetas ORDER BY id";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) recetas.add(leerReceta(rs));
        }
        return recetas;
    }

    public static Receta buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + CAMPOS + " FROM recetas WHERE id = ?";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leerReceta(rs) : null;
            }
        }
    }

    private static void comprobarTexto(String valor, int maximo) {
        if (valor == null || valor.isBlank() || valor.length() > maximo)
            throw new IllegalArgumentException("Texto vacío o demasiado largo.");
    }

    private static void comprobarCantidades(int tiempo, int raciones) {
        if (tiempo <= 0 || raciones <= 0)
            throw new IllegalArgumentException("Tiempo y raciones deben ser positivos.");
    }

    public static int anadir(Receta receta) throws SQLException {
        comprobarTexto(receta.getNombre(), 100);
        comprobarTexto(receta.getCategoria(), 50);
        comprobarTexto(receta.getDificultad(), 20);
        comprobarCantidades(receta.getTiempoPreparacion(), receta.getRaciones());
        String sql = "INSERT INTO recetas "
                + "(nombre, categoria, tiempo_preparacion, dificultad, raciones) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ConexionBD.conectar()) {
            c.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = c.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, receta.getNombre());
                    ps.setString(2, receta.getCategoria());
                    ps.setInt(3, receta.getTiempoPreparacion());
                    ps.setString(4, receta.getDificultad());
                    ps.setInt(5, receta.getRaciones());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("No se obtuvo el id.");
                        id = rs.getInt(1);
                    }
                }
                cambiarTotal(c, 1);
                c.commit();
                return id;
            } catch (SQLException e) {
                deshacer(c, e);
                throw e;
            }
        }
    }

    private static void cambiarTotal(Connection c, int cambio) throws SQLException {
        String sql = "UPDATE estadisticas SET total_recetas = total_recetas + ? "
                + "WHERE id = ? AND total_recetas + ? >= 0";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cambio);
            ps.setInt(2, 1);
            ps.setInt(3, cambio);
            if (ps.executeUpdate() != 1)
                throw new SQLException("Falta la estadística o su total no es válido.");
        }
    }

    private static void deshacer(Connection c, SQLException error) {
        try {
            c.rollback();
        } catch (SQLException falloRollback) {
            error.addSuppressed(falloRollback);
        }
    }

    public static boolean modificar(int id, int tiempo, int raciones) throws SQLException {
        comprobarCantidades(tiempo, raciones);
        String sql = "UPDATE recetas SET tiempo_preparacion = ?, raciones = ? WHERE id = ?";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tiempo);
            ps.setInt(2, raciones);
            ps.setInt(3, id);
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean eliminar(int id) throws SQLException {
        try (Connection c = ConexionBD.conectar()) {
            c.setAutoCommit(false);
            try {
                int filas;
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM recetas WHERE id = ?")) {
                    ps.setInt(1, id);
                    filas = ps.executeUpdate();
                }
                if (filas == 1) cambiarTotal(c, -1);
                c.commit();
                return filas == 1;
            } catch (SQLException e) {
                deshacer(c, e);
                throw e;
            }
        }
    }

    public static List<Receta> buscarPorNombre(String nombre) throws SQLException {
        List<Receta> recetas = new ArrayList<>();
        try (Connection c = ConexionBD.conectar();
             CallableStatement cs = c.prepareCall("{call buscarReceta(?)}")) {
            cs.setString(1, nombre);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) recetas.add(leerReceta(rs));
            }
        }
        return recetas;
    }

    public static int totalRecetas() throws SQLException {
        String sql = "SELECT total_recetas FROM estadisticas WHERE id = ?";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, 1);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No existe la estadística 1.");
                return rs.getInt(1);
            }
        }
    }

    public static void mostrarEntorno() throws SQLException {
        System.out.println("Usuario del sistema: " + System.getProperty("user.name"));
        System.out.println("Fecha y hora: " + ZonedDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss VV XXX")));
        System.out.println("Java: " + System.getProperty("java.version"));
        try (Connection c = ConexionBD.conectar();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT CURRENT_USER(), DATABASE(), VERSION(), NOW()" );
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            System.out.println("Usuario MySQL: " + rs.getString(1));
            System.out.println("Base de datos: " + rs.getString(2));
            System.out.println("Servidor MySQL: " + rs.getString(3));
            System.out.println("Fecha y hora del servidor: " + rs.getString(4));
            System.out.println("Driver: " + c.getMetaData().getDriverVersion());
        }
    }

    private static String texto(String mensaje, int maximo) {
        System.out.print(mensaje);
        String valor = sc.nextLine().trim();
        comprobarTexto(valor, maximo);
        return valor;
    }

    private static int entero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número entero.");
            }
        }
    }

    private static void mostrar(List<Receta> recetas) {
        System.out.println("ID | Nombre | Categoría | Tiempo | Dificultad | Raciones");
        for (Receta receta : recetas) System.out.println(receta);
        if (recetas.isEmpty()) System.out.println("No se encontraron recetas.");
    }

    public static void main(String[] args) {
        try {
            mostrarEntorno();
        } catch (SQLException e) {
            System.out.println("No se pudo conectar: " + e.getMessage());
            return;
        }
        while (true) {
            System.out.println("\n1. Listar  2. Buscar por id  3. Añadir");
            System.out.println("4. Modificar  5. Eliminar  6. Buscar por nombre");
            System.out.println("7. Total de recetas  0. Salir");
            if (!sc.hasNextLine()) break;
            try {
                int opcion = entero("Opción: ");
                if (opcion == 0) break;
                switch (opcion) {
                    case 1:
                        mostrar(listar());
                        break;
                    case 2:
                        Receta receta = buscarPorId(entero("Id: "));
                        System.out.println(receta == null ? "No existe esa receta." : receta);
                        break;
                    case 3:
                        Receta nueva = new Receta(0, texto("Nombre: ", 100),
                                texto("Categoría: ", 50), entero("Tiempo (minutos): "),
                                texto("Dificultad: ", 20), entero("Raciones: "));
                        System.out.println("Receta añadida. Id: " + anadir(nueva));
                        break;
                    case 4:
                        boolean cambiada = modificar(entero("Id: "),
                                entero("Nuevo tiempo: "), entero("Nuevas raciones: "));
                        System.out.println(cambiada ? "Receta modificada." : "No existe esa receta.");
                        break;
                    case 5:
                        boolean borrada = eliminar(entero("Id: "));
                        System.out.println(borrada ? "Receta eliminada." : "No existe esa receta.");
                        break;
                    case 6:
                        mostrar(buscarPorNombre(texto("Nombre completo: ", 100)));
                        break;
                    case 7:
                        System.out.println("Total: " + totalRecetas());
                        break;
                    default:
                        System.out.println("Opción incorrecta.");
                }
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Datos incorrectos: " + e.getMessage());
            } catch (java.util.NoSuchElementException e) {
                break;
            }
        }
    }
}
