package redluz.motor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Link-Cut Tree (Sleator y Tarjan, 1983): mantiene un BOSQUE que cambia
 * (se agregan y quitan aristas) y responde consultas de conectividad y de
 * caminos en O(log n) amortizado por operación.
 *
 * Idea central: el árbol real se parte en "caminos preferidos". Cada camino
 * se guarda en un árbol splay ordenado por profundidad (izquierda = arriba).
 *
 *   Árbol real           Caminos preferidos         Splay de cada camino
 *       F                F - S1 - P1 - C1            ordenados de arriba
 *       |                     \                      hacia abajo
 *       S1                     P2 - C2  (cuelga de S1 con un path-parent)
 *      /  \
 *    P1    P2
 *    |      |
 *    C1    C2
 *
 * Operación clave: access(x) hace que el camino preferido vaya de la raíz
 * del árbol real hasta x, y deja a x en la raíz de su splay. Después de un
 * access, el subárbol splay de x ES el camino raíz -> x, y sus agregados
 * (suma de km, número de tramos) son la respuesta de la consulta.
 *
 * Esta clase no sabe nada de electricidad: solo trabaja con Nodos.
 */
public class LinkCutTree {

    /** Si está activo, se anota cada rotación y access (para la exposición). */
    private boolean trazar = false;
    private final List<String> traza = new ArrayList<>();

    public void activarTraza(boolean activo) {
        trazar = activo;
        traza.clear();
    }

    public List<String> traza() {
        return traza;
    }

    // ------------------------------------------------------------------
    // 1. Rotación y splay
    // ------------------------------------------------------------------

    /**
     * Rota x sobre su padre p, conservando el orden izquierda-derecha.
     *
     *   rotación cuando x es hijo izquierdo:
     *
     *         p                x
     *        / \              / \
     *       x   c    ==>     a   p
     *      / \                  / \
     *     a   b                b   c
     *
     * El abuelo g pasa a apuntar a x. Si p era raíz del splay, x hereda el
     * path-parent de p (g sigue siendo padre de x pero no lo tiene como hijo).
     */
    private void rotar(Nodo x) {
        Nodo p = x.padre;
        Nodo g = p.padre;
        boolean pEraRaiz = p.esRaizSplay();

        if (p.izq == x) {
            p.izq = x.der;                       // b pasa a ser hijo izquierdo de p
            if (x.der != null) {
                x.der.padre = p;
            }
            x.der = p;                           // p baja a la derecha de x
        } else {
            p.der = x.izq;                       // caso simétrico
            if (x.izq != null) {
                x.izq.padre = p;
            }
            x.izq = p;
        }
        p.padre = x;
        x.padre = g;
        if (!pEraRaiz) {                         // g tenía a p como hijo: ahora tiene a x
            if (g.izq == p) {
                g.izq = x;
            } else {
                g.der = x;
            }
        }
        p.actualizar();                          // primero p, que ahora está debajo
        x.actualizar();
        if (trazar) {
            traza.add("rotar " + x.etiqueta + " sobre " + p.etiqueta);
        }
    }

    /**
     * Sube x hasta la raíz de su árbol splay con rotaciones dobles.
     *
     *  - zig-zig (x y su padre son hijos del mismo lado): rotar el padre y luego x.
     *  - zig-zag (lados distintos): rotar x dos veces.
     *  - zig (el padre es la raíz): rotar x una vez.
     *
     * Antes de rotar hay que aplicar las inversiones pendientes de arriba
     * hacia abajo; por eso se guardan los ancestros en una pila.
     */
    private void splay(Nodo x) {
        Deque<Nodo> pila = new ArrayDeque<>();
        Nodo actual = x;
        pila.push(actual);
        while (!actual.esRaizSplay()) {
            actual = actual.padre;
            pila.push(actual);
        }
        while (!pila.isEmpty()) {
            pila.pop().empujar();                // de la raíz del splay hacia x
        }

        while (!x.esRaizSplay()) {
            Nodo p = x.padre;
            if (!p.esRaizSplay()) {
                Nodo g = p.padre;
                boolean zigZig = (g.izq == p) == (p.izq == x);
                rotar(zigZig ? p : x);           // primera rotación del par
            }
            rotar(x);                            // segunda rotación (o la única)
        }
    }

    // ------------------------------------------------------------------
    // 2. Access: la operación central
    // ------------------------------------------------------------------

    /**
     * Hace que el camino preferido vaya de la raíz del árbol real hasta x.
     *
     * Recorre hacia arriba los path-parent. En cada paso:
     *   1. splay(z) deja a z en la raíz de su splay;
     *   2. z.der = y "pega" el camino que venía de abajo y suelta la parte
     *      de abajo que tenía antes (que queda colgando con un path-parent).
     * Al final x queda en la raíz de un splay que contiene exactamente el
     * camino raíz -> x. Costo: O(log n) amortizado.
     */
    public void access(Nodo x) {
        if (trazar) {
            traza.add("access(" + x.etiqueta + ")");
        }
        Nodo y = null;
        for (Nodo z = x; z != null; z = z.padre) {
            splay(z);
            z.der = y;                           // lo de abajo en el camino pasa a ser y
            z.actualizar();
            y = z;
        }
        splay(x);
    }

    /**
     * Convierte a x en la raíz de su árbol real.
     * access(x) deja el camino raíz -> x; invertirlo pone a x arriba.
     * La inversión es perezosa (solo se marca), por eso cuesta O(1) extra.
     */
    public void makeRoot(Nodo x) {
        access(x);
        x.invertido = !x.invertido;
        if (trazar) {
            traza.add("makeRoot(" + x.etiqueta + "): se invierte el camino");
        }
    }

    /**
     * Devuelve la raíz del árbol real que contiene a x: el nodo más a la
     * izquierda (el de menor profundidad) del splay tras access(x).
     */
    public Nodo findRoot(Nodo x) {
        access(x);
        Nodo actual = x;
        actual.empujar();
        while (actual.izq != null) {
            actual = actual.izq;
            actual.empujar();
        }
        splay(actual);                           // mantiene la complejidad amortizada
        return actual;
    }

    // ------------------------------------------------------------------
    // 3. Operaciones públicas
    // ------------------------------------------------------------------

    /** true si x e y están en el mismo árbol del bosque. */
    public boolean connected(Nodo x, Nodo y) {
        return x == y || findRoot(x) == findRoot(y);
    }

    /**
     * Agrega la arista x - y. Devuelve false (y no cambia nada) si ya
     * estaban en el mismo árbol, porque se formaría un ciclo.
     */
    public boolean link(Nodo x, Nodo y) {
        makeRoot(x);                             // x queda como raíz de su árbol
        if (findRoot(y) == x) {
            return false;                        // y ya está en el árbol de x
        }
        x.padre = y;                             // x cuelga de y con un path-parent
        if (trazar) {
            traza.add("link: " + x.etiqueta + " cuelga de " + y.etiqueta);
        }
        return true;
    }

    /**
     * Quita la arista x - y. Devuelve false si no existía.
     *
     * makeRoot(x) y access(y) dejan el camino x -> y en un splay con y en la
     * raíz. Si la arista existe, ese camino tiene solo dos nodos: x es el
     * hijo izquierdo de y y no tiene hijo derecho. Basta con separarlos.
     */
    public boolean cut(Nodo x, Nodo y) {
        makeRoot(x);
        access(y);
        x.empujar();
        if (y.izq != x || x.der != null) {
            return false;                        // no son vecinos directos
        }
        y.izq = null;
        x.padre = null;
        y.actualizar();
        if (trazar) {
            traza.add("cut: se separan " + x.etiqueta + " y " + y.etiqueta);
        }
        return true;
    }

    /**
     * Deja el camino x -> y listo para leer sus agregados y devuelve el
     * nodo cuyo subárbol splay es exactamente ese camino.
     * Requiere que x e y estén conectados.
     */
    public Nodo camino(Nodo x, Nodo y) {
        makeRoot(x);
        access(y);
        return y;
    }

    // Lectura de agregados (los campos son de paquete; esto los expone).

    public double sumaKm(Nodo raizDeCamino) {
        return raizDeCamino.sumaKm;
    }

    public int sumaTramos(Nodo raizDeCamino) {
        return raizDeCamino.sumaTramos;
    }

    public int maxMarca(Nodo raizDeCamino) {
        return raizDeCamino.maxMarca;
    }
}
