public class Receta {
    private final int id;
    private final String nombre;
    private final String categoria;
    private final int tiempoPreparacion;
    private final String dificultad;
    private final int raciones;

    public Receta(int id, String nombre, String categoria,
                  int tiempoPreparacion, String dificultad, int raciones) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.tiempoPreparacion = tiempoPreparacion;
        this.dificultad = dificultad;
        this.raciones = raciones;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public int getTiempoPreparacion() { return tiempoPreparacion; }
    public String getDificultad() { return dificultad; }
    public int getRaciones() { return raciones; }

    @Override
    public String toString() {
        return id + " | " + nombre + " | " + categoria + " | "
                + tiempoPreparacion + " min | " + dificultad + " | " + raciones;
    }
}
