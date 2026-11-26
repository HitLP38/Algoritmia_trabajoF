package fibranet.motor;

import fibranet.modelo.Arista;

import java.util.ArrayList;
import java.util.List;

/**
 * Lo que devuelven Kruskal y Prim: el plan de tendido.
 *
 * Se guarda también el paso a paso (texto) para mostrarlo en la
 * exposición, y el tiempo que tardó el algoritmo.
 */
public class ResultadoMST {

    public final String algoritmo;                         // "Kruskal" o "Prim"
    public final List<Arista> tramosElegidos = new ArrayList<>();
    public final List<String[]> pasos = new ArrayList<>();  // filas de la tabla de paso a paso
    public double costoTotal = 0;
    public int grupos = 1;          // 1 = red conexa; más de 1 = bosque
    public long nanosegundos = 0;   // tiempo de ejecución medido

    public ResultadoMST(String algoritmo) {
        this.algoritmo = algoritmo;
    }

    /** Agrega un tramo al plan y suma su costo. */
    void agregar(Arista arista) {
        tramosElegidos.add(arista);
        costoTotal += arista.costo;
    }

    public boolean esConexo() {
        return grupos == 1;
    }
}
