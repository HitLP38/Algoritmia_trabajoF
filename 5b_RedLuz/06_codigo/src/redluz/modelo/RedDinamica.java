package redluz.modelo;

import java.util.List;

/**
 * Lo que debe saber hacer una red eléctrica dinámica de RedLuz.
 *
 * Hay dos implementaciones con el mismo contrato:
 *  - RedElectrica: usa el Link-Cut Tree, O(log n) amortizado por operación.
 *  - RedIngenua:   usa listas de adyacencia y BFS, O(n) por consulta.
 * Las pruebas les dan las mismas operaciones y exigen las mismas respuestas;
 * el benchmark compara sus tiempos.
 */
public interface RedDinamica {

    /** Resultado de una operación de la red. */
    enum Resultado { OK, ANILLO, YA_EXISTE, NO_EXISTE, NODO_DESCONOCIDO, DATO_INVALIDO }

    /** Registra un nodo. tipo: SUBESTACION, POSTE o CLIENTE. */
    Resultado agregarNodo(String id, String tipo);

    /** Conecta un tramo. Devuelve ANILLO si los dos nodos ya estaban conectados. */
    Resultado conectar(String a, String b, double km);

    /** Corta el tramo a - b. Devuelve NO_EXISTE si no había tramo. */
    Resultado cortar(String a, String b);

    /** true si el nodo está conectado a alguna subestación. */
    boolean tieneLuz(String id);

    /** true si los dos nodos están en el mismo árbol (zona). */
    boolean mismaZona(String a, String b);

    /** Ruta de alimentación hasta la subestación, o null si no tiene luz. */
    Ruta ruta(String id);

    boolean existeNodo(String id);

    int numeroNodos();

    /**
     * Clientes que hoy no tienen luz (por ejemplo, después de un corte
     * programado). Sirve para saber a cuántos clientes afecta un corte.
     */
    List<String> clientesSinLuz();

    /** Tramos activos como filas {origen, destino, km} (sin el enlace virtual a FUENTE). */
    List<String[]> tramos();
}
