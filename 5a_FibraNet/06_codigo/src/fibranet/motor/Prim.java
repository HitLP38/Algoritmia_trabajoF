package fibranet.motor;

import fibranet.modelo.Arista;
import fibranet.modelo.Grafo;

import java.util.Locale;
import java.util.PriorityQueue;

/**
 * Algoritmo de Prim: árbol (o bosque) de expansión mínima.
 *
 * Idea voraz: hacer crecer un solo árbol desde una sede inicial. En cada
 * paso se elige el tramo más barato que sale del árbol hacia una sede que
 * todavía no está en él. Una cola de prioridad (montículo binario) entrega
 * ese tramo en O(log E).
 *
 * Se usa la versión "perezosa": los tramos que ya no sirven (los dos
 * extremos dentro del árbol) se quedan en la cola y se descartan al salir.
 *
 * Costo:
 *   cada tramo entra y sale de la cola a lo sumo dos veces ... O(E log E)
 *   como E <= V², log E <= 2 log V, entonces ................. O(E log V)
 * Memoria extra: arreglo enArbol[] O(V) y la cola O(E).
 *
 * Si la red no es conexa, al vaciarse la cola se empieza otro árbol desde
 * una sede no visitada; así se obtiene el mismo bosque que con Kruskal.
 */
public final class Prim {

    private Prim() {
        // clase de utilidad: no se crean objetos
    }

    /**
     * @param grafo       la red de sedes y tramos
     * @param sedeInicial índice de la sede desde donde crece el árbol
     * @param pasoAPaso   si es true, guarda cada decisión para mostrarla
     */
    public static ResultadoMST calcular(Grafo grafo, int sedeInicial, boolean pasoAPaso) {
        ResultadoMST resultado = new ResultadoMST("Prim");
        long inicio = System.nanoTime();

        int n = grafo.numeroSedes();
        boolean[] enArbol = new boolean[n];               // true si la sede ya fue unida
        PriorityQueue<Arista> cola = new PriorityQueue<>(); // tramos candidatos, el más barato arriba
        int arboles = 0;
        int paso = 0;

        // Recorre las sedes empezando por la inicial. Cada sede que aún no
        // está en un árbol inicia uno nuevo (solo pasa si la red no es conexa).
        for (int k = 0; k < n; k++) {
            int raiz = (sedeInicial + k) % n;
            if (enArbol[raiz]) {
                continue;
            }
            arboles++;
            visitar(grafo, raiz, enArbol, cola);          // la raíz entra al árbol
            if (pasoAPaso) {
                resultado.pasos.add(new String[] {
                    String.valueOf(++paso), grafo.nombre(raiz), "-", "-", "INICIO de árbol"
                });
            }

            // Mientras haya tramos candidatos, tomar el más barato.
            while (!cola.isEmpty()) {
                Arista arista = cola.poll();
                boolean origenDentro = enArbol[arista.origen];
                boolean destinoDentro = enArbol[arista.destino];
                if (origenDentro && destinoDentro) {
                    continue;                              // ya no sirve: formaría un ciclo
                }
                int nueva = origenDentro ? arista.destino : arista.origen;
                resultado.agregar(arista);                 // el tramo entra al plan
                visitar(grafo, nueva, enArbol, cola);      // y la sede nueva al árbol
                if (pasoAPaso) {
                    resultado.pasos.add(new String[] {
                        String.valueOf(++paso),
                        grafo.nombre(nueva),
                        grafo.nombre(arista.otroExtremo(nueva)) + " - " + grafo.nombre(nueva),
                        String.format(Locale.US, "%.2f", arista.costo),
                        "SEDE AGREGADA"
                    });
                }
            }
        }

        resultado.grupos = arboles;
        resultado.nanosegundos = System.nanoTime() - inicio;
        return resultado;
    }

    /**
     * Marca la sede como parte del árbol y pone en la cola los tramos que
     * salen de ella hacia sedes que todavía están afuera.
     */
    private static void visitar(Grafo grafo, int sede, boolean[] enArbol, PriorityQueue<Arista> cola) {
        enArbol[sede] = true;
        for (Arista arista : grafo.tramosDe(sede)) {
            if (!enArbol[arista.otroExtremo(sede)]) {
                cola.add(arista);
            }
        }
    }
}
