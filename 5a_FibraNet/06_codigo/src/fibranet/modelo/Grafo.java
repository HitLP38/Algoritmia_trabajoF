package fibranet.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * La red de FibraNet: sedes (vértices) y tramos posibles (aristas con costo).
 *
 * Guarda la red de dos formas, porque cada algoritmo la necesita distinta:
 *  - Lista de aristas: Kruskal la ordena por costo.
 *  - Lista de adyacencia: Prim pregunta "¿qué tramos salen de esta sede?".
 *
 * Memoria: O(V + E). Cada arista se guarda una vez en la lista y aparece
 * en la adyacencia de sus dos extremos (dos referencias al mismo objeto).
 */
public class Grafo {

    private final List<String> nombres = new ArrayList<>();          // índice -> nombre
    private final Map<String, Integer> indices = new HashMap<>();    // nombre -> índice
    private final List<Arista> aristas = new ArrayList<>();
    private final List<List<Arista>> adyacencia = new ArrayList<>();

    /**
     * Registra una sede si no existe y devuelve su índice.
     * Así el CSV no necesita una lista aparte de sedes.
     */
    public int agregarSede(String nombre) {
        Integer indice = indices.get(nombre);
        if (indice != null) {
            return indice;                       // ya existía
        }
        int nuevo = nombres.size();              // el siguiente número libre
        nombres.add(nombre);
        indices.put(nombre, nuevo);
        adyacencia.add(new ArrayList<>());       // aún sin tramos
        return nuevo;
    }

    /** Agrega un tramo entre dos sedes (las crea si hace falta). */
    public void agregarTramo(String origen, String destino, double costo) {
        int a = agregarSede(origen);
        int b = agregarSede(destino);
        Arista arista = new Arista(a, b, costo);
        aristas.add(arista);
        adyacencia.get(a).add(arista);           // el tramo sirve en los dos sentidos
        adyacencia.get(b).add(arista);
    }

    public int numeroSedes() {
        return nombres.size();
    }

    public int numeroTramos() {
        return aristas.size();
    }

    public String nombre(int indice) {
        return nombres.get(indice);
    }

    /** Devuelve el índice de la sede, o -1 si no existe. */
    public int indice(String nombre) {
        Integer indice = indices.get(nombre);
        return (indice == null) ? -1 : indice;
    }

    /** Vista de solo lectura: los cambios se hacen con agregarTramo. */
    public List<Arista> aristas() {
        return Collections.unmodifiableList(aristas);
    }

    public List<Arista> tramosDe(int sede) {
        return Collections.unmodifiableList(adyacencia.get(sede));
    }

    public double costoTotalDeTodosLosTramos() {
        double total = 0;
        for (Arista a : aristas) {
            total += a.costo;
        }
        return total;
    }

    public boolean estaVacio() {
        return nombres.isEmpty();
    }
}
