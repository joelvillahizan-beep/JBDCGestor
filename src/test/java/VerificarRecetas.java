import java.sql.*;
import java.util.List;

public class VerificarRecetas {
    private static int comprobaciones = 0;

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new IllegalStateException("FALLO: " + mensaje);
        comprobaciones++;
        System.out.println("OK " + comprobaciones + ": " + mensaje);
    }

    private static void prepararTotal(int total) throws SQLException {
        try (Connection c = ConexionBD.conectar();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE estadisticas SET total_recetas = ? WHERE id = ?")) {
            ps.setInt(1, total);
            ps.setInt(2, 1);
            if (ps.executeUpdate() != 1) throw new SQLException("Falta estadisticas.id = 1");
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !"--base-de-pruebas".equals(args[0])) {
            throw new IllegalArgumentException(
                    "Usa --base-de-pruebas solo en una base de prácticas recién creada.");
        }
        System.out.println("VERIFICACIÓN REAL DE GESTORRECETAS");
        GestionRecetas.mostrarEntorno();
        List<Receta> iniciales = GestionRecetas.listar();
        int totalInicial = GestionRecetas.totalRecetas();
        comprobar(iniciales.size() == 8 && totalInicial == 8,
                "Estado inicial: 8 recetas y estadística 8");
        for (Receta receta : iniciales) System.out.println(receta);
        comprobar("Tortilla de patatas".equals(GestionRecetas.buscarPorId(1).getNombre()),
                "Buscar id 1 devuelve Tortilla de patatas");
        comprobar(GestionRecetas.buscarPorId(-1) == null, "Id inexistente sin resultado");

        System.out.println("\nALTA Y COMMIT");
        int id = GestionRecetas.anadir(new Receta(0, "Arroz de prueba UD02",
                "Arroz", 25, "Fácil", 2));
        try {
            System.out.println("Id de la receta añadida: " + id);
            comprobar(GestionRecetas.buscarPorId(id) != null, "Alta persistida en otra conexión");
            comprobar(GestionRecetas.totalRecetas() == 9 && GestionRecetas.listar().size() == 9,
                    "Commit: 9 recetas y estadística 9");
            comprobar(GestionRecetas.modificar(id, 35, 4), "Modificación realizada");
            Receta modificada = GestionRecetas.buscarPorId(id);
            System.out.println(modificada);
            comprobar(modificada.getTiempoPreparacion() == 35 && modificada.getRaciones() == 4,
                    "Consulta posterior: 35 minutos y 4 raciones");
            comprobar(!GestionRecetas.modificar(-1, 20, 2), "Modificar id inexistente devuelve false");

            System.out.println("\nPROCEDIMIENTO ALMACENADO");
            List<Receta> encontradas = GestionRecetas.buscarPorNombre("Tortilla de patatas");
            comprobar(encontradas.size() == 1 && encontradas.get(0).getId() == 1,
                    "CallableStatement devuelve la receta por nombre");
            System.out.println(encontradas.get(0));
            comprobar(GestionRecetas.buscarPorNombre("No existe UD02").isEmpty(),
                    "Nombre inexistente sin resultados");
            comprobar(GestionRecetas.buscarPorNombre("' OR '1'='1").isEmpty(),
                    "El texto con comillas se trata como dato");
            try {
                GestionRecetas.anadir(new Receta(0, "Inválida", "Prueba", 0, "Fácil", 2));
                throw new IllegalStateException("Se admitió un tiempo igual a cero");
            } catch (IllegalArgumentException e) {
                comprobar(true, "Se rechaza un tiempo igual a cero");
            }
        } finally {
            GestionRecetas.eliminar(id);
        }
        System.out.println("\nELIMINACIÓN");
        comprobar(GestionRecetas.buscarPorId(id) == null, "Receta eliminada");
        comprobar(GestionRecetas.totalRecetas() == 8 && GestionRecetas.listar().size() == 8,
                "Baja confirmada: 8 recetas y estadística 8");
        comprobar(!GestionRecetas.eliminar(-1) && GestionRecetas.totalRecetas() == 8,
                "Borrar un id inexistente no cambia el total");

        System.out.println("\nERROR CONTROLADO Y ROLLBACK");
        System.out.println("Solo en esta base de pruebas: total temporal = 2147483647");
        prepararTotal(Integer.MAX_VALUE);
        try {
            try {
                GestionRecetas.anadir(new Receta(0, "Receta rollback UD02",
                        "Prueba", 10, "Fácil", 1));
                throw new IllegalStateException("La segunda operación debía fallar");
            } catch (SQLException e) {
                System.out.println("Error SQL recibido: " + e.getMessage());
                comprobar(e.getErrorCode() == 1264,
                        "Falla el UPDATE por desbordamiento del contador INT");
            }
            comprobar(GestionRecetas.buscarPorNombre("Receta rollback UD02").isEmpty(),
                    "Rollback: la receta insertada no permanece en la base");
            comprobar(GestionRecetas.listar().size() == 8,
                    "Rollback: siguen existiendo las 8 recetas iniciales");
            comprobar(GestionRecetas.totalRecetas() == Integer.MAX_VALUE,
                    "El contador conserva su valor anterior a la transacción");
        } finally {
            prepararTotal(totalInicial);
        }
        comprobar(GestionRecetas.totalRecetas() == 8 && GestionRecetas.listar().size() == 8,
                "Restauración de la prueba: 8 recetas y estadística 8");
        System.out.println("\nRESULTADO: " + comprobaciones + " comprobaciones correctas.");
    }
}
