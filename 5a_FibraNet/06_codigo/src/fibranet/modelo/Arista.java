package fibranet.modelo;

/**
 * Un tramo de fibra posible entre dos sedes.
 *
 * Las sedes se guardan como números (0, 1, 2, ...) y no como texto,
 * porque Union-Find y Prim trabajan con arreglos indexados por número.
 * El nombre de cada sede se busca en la clase Grafo.
 */
public class Arista implements Comparable<Arista> {

    public final int origen;    // índice de la sede de un extremo
    public final int destino;   // índice de la sede del otro extremo
    public final double costo;  // costo del tramo (miles de soles, o km en la Red Dorsal)

    public Arista(int origen, int destino, double costo) {
        this.origen = origen;
        this.destino = destino;
        this.costo = costo;
    }

    /**
     * Dada una sede del tramo, devuelve la del otro extremo.
     * Prim lo usa para saber a qué sede nueva llega un tramo.
     */
    public int otroExtremo(int sede) {
        return (sede == origen) ? destino : origen;
    }

    /**
     * Orden natural: el tramo más barato va primero.
     * Kruskal ordena la lista con esto y Prim lo usa en su cola de prioridad.
     */
    @Override
    public int compareTo(Arista otra) {
        return Double.compare(this.costo, otra.costo);
    }
}
