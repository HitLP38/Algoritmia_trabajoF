package fibranet.motor;

/**
 * Union-Find SIN optimizaciones. Solo existe para el benchmark:
 * muestra cuánto ganan la compresión de caminos y la unión por rango.
 *
 * find sube padre por padre sin acortar el camino y union cuelga siempre
 * la raíz de a debajo de la raíz de b. En el peor caso el árbol se vuelve
 * una lista y cada find cuesta O(n).
 */
public class UnionFindIngenuo {

    private final int[] padre;

    public UnionFindIngenuo(int n) {
        padre = new int[n];
        for (int i = 0; i < n; i++) {
            padre[i] = i;
        }
    }

    /** Sube hasta la raíz sin modificar nada. Peor caso O(n). */
    public int find(int x) {
        while (padre[x] != x) {
            x = padre[x];
        }
        return x;
    }

    /** Une sin mirar alturas. */
    public boolean union(int a, int b) {
        int raizA = find(a);
        int raizB = find(b);
        if (raizA == raizB) {
            return false;
        }
        padre[raizA] = raizB;
        return true;
    }
}
