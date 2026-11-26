package fibranet.motor;

/**
 * Union-Find (conjuntos disjuntos) con las dos optimizaciones clásicas:
 *  1. Compresión de caminos en find.
 *  2. Unión por rango en union.
 *
 * Idea: cada grupo de sedes es un árbol y su raíz es el "representante".
 * Dos sedes están en el mismo grupo si tienen el mismo representante.
 *
 *     padre[]:  0  1  2  3  4          grupo {0,1,2} con raíz 0
 *               0  0  1  3  3          grupo {3,4}   con raíz 3
 *
 *         0          3
 *         |          |
 *         1          4
 *         |
 *         2
 *
 * Memoria: dos arreglos int[] de tamaño n, es decir O(n).
 * Costo: O(α(n)) amortizado por operación, donde α es la inversa de
 * Ackermann (α(n) <= 4 para cualquier n que quepa en una computadora).
 */
public class UnionFind {

    private final int[] padre;  // padre[i] = padre de i en su árbol; si padre[i] == i, i es raíz
    private final int[] rango;  // cota superior de la altura del árbol con raíz i
    private int grupos;         // cuántos grupos (árboles) hay ahora

    /** Al inicio cada sede está sola: n grupos de un elemento. */
    public UnionFind(int n) {
        padre = new int[n];
        rango = new int[n];
        grupos = n;
        for (int i = 0; i < n; i++) {
            padre[i] = i;       // cada uno es su propia raíz
            rango[i] = 0;       // un árbol de un nodo tiene altura 0
        }
    }

    /**
     * Devuelve el representante (raíz) del grupo de x.
     *
     * Compresión de caminos: al volver de la recursión, cada nodo del camino
     * apunta directo a la raíz. La próxima consulta será casi O(1).
     *
     *   antes:  2 -> 1 -> 0          después:  2 -> 0
     *                                          1 -> 0
     */
    public int find(int x) {
        if (padre[x] != x) {
            padre[x] = find(padre[x]);  // sube hasta la raíz y se cuelga de ella
        }
        return padre[x];
    }

    /**
     * Une los grupos de a y b. Devuelve false si ya estaban juntos
     * (en Kruskal eso significa que el tramo formaría un ciclo).
     *
     * Unión por rango: el árbol más bajo se cuelga del más alto, así
     * la altura solo crece cuando los dos tienen el mismo rango.
     * Esto garantiza altura O(log n) incluso sin compresión.
     */
    public boolean union(int a, int b) {
        int raizA = find(a);
        int raizB = find(b);
        if (raizA == raizB) {
            return false;                       // mismo grupo: no se une nada
        }
        if (rango[raizA] < rango[raizB]) {
            padre[raizA] = raizB;               // A es más bajo: va debajo de B
        } else if (rango[raizA] > rango[raizB]) {
            padre[raizB] = raizA;               // B es más bajo: va debajo de A
        } else {
            padre[raizB] = raizA;               // empate: uno arriba y su rango sube
            rango[raizA]++;
        }
        grupos--;                               // dos grupos se volvieron uno
        return true;
    }

    /** true si a y b están en el mismo grupo. */
    public boolean conectados(int a, int b) {
        return find(a) == find(b);
    }

    public int numeroGrupos() {
        return grupos;
    }
}
