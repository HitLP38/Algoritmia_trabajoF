package redluz.modelo;

/** Ruta de alimentación de un nodo: de qué subestación recibe energía y a qué distancia. */
public class Ruta {

    public final String subestacion;
    public final double km;
    public final int tramos;

    public Ruta(String subestacion, double km, int tramos) {
        this.subestacion = subestacion;
        this.km = km;
        this.tramos = tramos;
    }
}
