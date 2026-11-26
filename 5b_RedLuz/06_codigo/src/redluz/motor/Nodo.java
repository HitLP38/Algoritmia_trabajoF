package redluz.motor;

/**
 * Un nodo del Link-Cut Tree.
 *
 * Cada nodo vive dentro de un árbol splay (el que guarda un "camino
 * preferido"). Sus tres referencias son los "punteros" de la estructura:
 *
 *            padre
 *              |
 *            [este]
 *            /    \
 *          izq    der
 *
 *  - izq: nodos que están MÁS ARRIBA en el camino (más cerca de la raíz del árbol real).
 *  - der: nodos que están MÁS ABAJO en el camino.
 *  - padre: si este nodo es hijo izq o der de su padre, es el padre en el splay.
 *           Si no es hijo de nadie, el padre es un "path-parent": el nodo del
 *           árbol real donde cuelga este camino entero.
 *
 * Además guarda datos de la red eléctrica y sus agregados sobre todo el
 * subárbol splay, para responder consultas de camino en O(1) tras un access.
 */
public final class Nodo {

    // --- referencias del árbol splay ---
    Nodo izq;
    Nodo der;
    Nodo padre;
    boolean invertido;   // marca perezosa: "hay que intercambiar izq y der en este subárbol"

    // --- datos propios ---
    public final String etiqueta;  // nombre para mostrar (S1, P3, C7, o "P3-C7" si es un tramo)
    final double km;               // longitud: solo los nodos-tramo tienen km > 0
    final int cuentaTramo;         // 1 si es un tramo real; 0 si es un vértice o el enlace virtual
    final int marcaSubestacion;    // número de subestación (>= 0) o -1 si no es subestación

    // --- agregados del subárbol splay (este nodo + sus descendientes en el splay) ---
    double sumaKm;
    int sumaTramos;
    int maxMarca;

    public Nodo(String etiqueta, double km, int cuentaTramo, int marcaSubestacion) {
        this.etiqueta = etiqueta;
        this.km = km;
        this.cuentaTramo = cuentaTramo;
        this.marcaSubestacion = marcaSubestacion;
        actualizar();
    }

    /**
     * Recalcula los agregados a partir de los hijos. Se llama cada vez
     * que cambian los hijos (después de rotar o de cambiar der en access).
     */
    void actualizar() {
        sumaKm = km;
        sumaTramos = cuentaTramo;
        maxMarca = marcaSubestacion;
        if (izq != null) {
            sumaKm += izq.sumaKm;
            sumaTramos += izq.sumaTramos;
            maxMarca = Math.max(maxMarca, izq.maxMarca);
        }
        if (der != null) {
            sumaKm += der.sumaKm;
            sumaTramos += der.sumaTramos;
            maxMarca = Math.max(maxMarca, der.maxMarca);
        }
    }

    /**
     * Aplica la marca de inversión pendiente: intercambia los hijos y
     * pasa la marca a ellos. Invertir un camino (arriba <-> abajo) es lo que
     * hace makeRoot; hacerlo de forma perezosa cuesta O(1).
     */
    void empujar() {
        if (invertido) {
            Nodo temporal = izq;
            izq = der;
            der = temporal;
            if (izq != null) {
                izq.invertido = !izq.invertido;
            }
            if (der != null) {
                der.invertido = !der.invertido;
            }
            invertido = false;
        }
    }

    /** true si es la raíz de su árbol splay (su padre no lo tiene como hijo). */
    boolean esRaizSplay() {
        return padre == null || (padre.izq != this && padre.der != this);
    }
}
