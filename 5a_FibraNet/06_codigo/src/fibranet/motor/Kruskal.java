package fibranet.motor;

import fibranet.modelo.Arista;
import fibranet.modelo.Grafo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Algoritmo de Kruskal: árbol (o bosque) de expansión mínima.
 *
 * Idea voraz: recorrer los tramos del más barato al más caro y quedarse
 * con cada tramo que una dos grupos distintos. Si el tramo une dos sedes
 * que ya están en el mismo grupo, formaría un ciclo y se descarta.
 * Union-Find responde "¿mismo grupo?" casi en O(1).
 *
 * Costo:
 *   ordenar los E tramos ............ O(E log E)
 *   E operaciones de Union-Find ..... O(E α(V))
 *   Total ........................... O(E log E) = O(E log V)
 * Mejor caso Ω(E log E) por el ordenamiento; Θ(E log E) en general.
 */
public final class Kruskal {

    private Kruskal() {
        // clase de utilidad: no se crean objetos
    }

    /**
     * @param grafo       la red de sedes y tramos
     * @param pasoAPaso   si es true, guarda cada decisión para mostrarla
     */
    public static ResultadoMST calcular(Grafo grafo, boolean pasoAPaso) {
        ResultadoMST resultado = new ResultadoMST("Kruskal");
        long inicio = System.nanoTime();

        // 1. Copiar y ordenar los tramos por costo (de menor a mayor).
        //    Se copia para no cambiar el orden original de la red.
        List<Arista> ordenadas = new ArrayList<>(grafo.aristas());
        Collections.sort(ordenadas);

        // 2. Al inicio cada sede es su propio grupo.
        int n = grafo.numeroSedes();
        UnionFind grupos = new UnionFind(n);

        // 3. Recorrer los tramos. Un árbol con n sedes tiene n-1 tramos,
        //    así que se puede parar antes si ya se completó.
        int paso = 0;
        for (Arista arista : ordenadas) {
            if (resultado.tramosElegidos.size() == n - 1) {
                break;                                        // árbol completo
            }
            paso++;
            boolean aceptado = grupos.union(arista.origen, arista.destino);
            if (aceptado) {
                resultado.agregar(arista);                    // une dos grupos: entra al plan
            }
            if (pasoAPaso) {
                resultado.pasos.add(new String[] {
                    String.valueOf(paso),
                    grafo.nombre(arista.origen) + " - " + grafo.nombre(arista.destino),
                    String.format(Locale.US, "%.2f", arista.costo),
                    aceptado ? "ACEPTADO" : "RECHAZADO (ciclo)"
                });
            }
        }

        // 4. Si quedó más de un grupo, la red no es conexa (es un bosque).
        resultado.grupos = grupos.numeroGrupos();
        resultado.nanosegundos = System.nanoTime() - inicio;
        return resultado;
    }

    /**
     * Agrupa las sedes según el plan: devuelve un Union-Find ya armado con
     * los tramos elegidos. Sirve para consultar si dos sedes quedan
     * conectadas y para listar los grupos de una red no conexa.
     */
    public static UnionFind gruposDelPlan(Grafo grafo, ResultadoMST plan) {
        UnionFind grupos = new UnionFind(grafo.numeroSedes());
        for (Arista arista : plan.tramosElegidos) {
            grupos.union(arista.origen, arista.destino);
        }
        return grupos;
    }
}
