public class Receta {
    public String nombre;
    public String categoria;
    public int tiempoPreparacion;
    public String dificultad;
    public int raciones;

    public Receta(String nombre, String categoria, int tiempoPreparacion,
                  String dificultad, int raciones) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.tiempoPreparacion = tiempoPreparacion;
        this.dificultad = dificultad;
        this.raciones = raciones;
    }
}
