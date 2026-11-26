package redluz.modelo;

import redluz.motor.LinkCutTree;
import redluz.motor.Nodo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * La red de RedLuz sobre un Link-Cut Tree.
 *
 * Traduce el lenguaje de la red eléctrica (cliente, tramo, subestación) a
 * operaciones del motor (link, cut, connected, camino):
 *
 *   FUENTE (virtual)
 *     |  enlace virtual (0 km, no cuenta como tramo)
 *     S1 ---[P1-S1 0.8 km]--- P1 ---[P1-C1 0.1 km]--- C1
 *
 *  - FUENTE es un nodo virtual unido a todas las subestaciones.
 *    "C1 tiene luz" equivale a connected(C1, FUENTE).
 *  - Cada tramo es un NODO propio del Link-Cut Tree que guarda sus km.
 *    Así la suma de km de un camino es la suma de los nodos del camino.
 *
 * Costo: cada operación hace O(1) llamadas al motor: O(log n) amortizado.
 */
public class RedElectrica implements RedDinamica {

    private final LinkCutTree lct = new LinkCutTree();
    private final Nodo fuente = new Nodo("FUENTE", 0, 0, -1);

    private final Map<String, Nodo> nodos = new HashMap<>();             // id -> nodo del motor
    private final List<String> subestaciones = new ArrayList<>();         // marca -> id de subestación
    private final List<String> clientes = new ArrayList<>();              // ids de tipo CLIENTE, en orden de alta
    private final Map<String, Nodo> nodosTramo = new HashMap<>();        // "A|B" -> nodo-tramo
    private final Map<String, String[]> datosTramo = new LinkedHashMap<>(); // "A|B" -> {A, B, km}

    /** Da acceso al motor para activar la traza paso a paso. */
    public LinkCutTree motor() {
        return lct;
    }

    @Override
    public Resultado agregarNodo(String id, String tipo) {
        if (id == null || id.isEmpty() || !Tipos.esValido(tipo)) {
            return Resultado.DATO_INVALIDO;
        }
        if (nodos.containsKey(id)) {
            return Resultado.YA_EXISTE;
        }
        boolean esSubestacion = tipo.equals(Tipos.SUBESTACION);
        int marca = esSubestacion ? subestaciones.size() : -1;
        Nodo nodo = new Nodo(id, 0, 0, marca);
        nodos.put(id, nodo);
        if (tipo.equals(Tipos.CLIENTE)) {
            clientes.add(id);
        }
        if (esSubestacion) {
            subestaciones.add(id);
            Nodo enlace = new Nodo("FUENTE-" + id, 0, 0, -1);   // 0 km y no cuenta como tramo
            lct.link(enlace, fuente);
            lct.link(nodo, enlace);
        }
        return Resultado.OK;
    }

    @Override
    public Resultado conectar(String a, String b, double km) {
        Nodo na = nodos.get(a);
        Nodo nb = nodos.get(b);
        if (na == null || nb == null) {
            return Resultado.NODO_DESCONOCIDO;
        }
        if (a.equals(b) || !(km > 0)) {
            return Resultado.DATO_INVALIDO;
        }
        String clave = clave(a, b);
        if (nodosTramo.containsKey(clave)) {
            return Resultado.YA_EXISTE;
        }
        if (lct.connected(na, nb)) {
            return Resultado.ANILLO;            // ya hay un camino: el tramo cerraría un anillo
        }
        Nodo tramo = new Nodo(a + "-" + b, km, 1, -1);
        lct.link(na, tramo);                    // a - tramo
        lct.link(tramo, nb);                    // tramo - b
        nodosTramo.put(clave, tramo);
        datosTramo.put(clave, new String[] {a, b, String.valueOf(km)});
        return Resultado.OK;
    }

    @Override
    public Resultado cortar(String a, String b) {
        Nodo na = nodos.get(a);
        Nodo nb = nodos.get(b);
        if (na == null || nb == null) {
            return Resultado.NODO_DESCONOCIDO;
        }
        String clave = clave(a, b);
        Nodo tramo = nodosTramo.remove(clave);
        if (tramo == null) {
            return Resultado.NO_EXISTE;
        }
        datosTramo.remove(clave);
        lct.cut(na, tramo);                     // se quitan las dos aristas del nodo-tramo
        lct.cut(tramo, nb);
        return Resultado.OK;
    }

    @Override
    public boolean tieneLuz(String id) {
        Nodo nodo = nodos.get(id);
        return nodo != null && lct.connected(nodo, fuente);
    }

    /**
     * Dos nodos con luz están en la misma zona si los alimenta la misma
     * subestación (todas las zonas se tocan en FUENTE, que es virtual).
     * Si alguno no tiene luz, basta con preguntar si están conectados.
     */
    @Override
    public boolean mismaZona(String a, String b) {
        Nodo na = nodos.get(a);
        Nodo nb = nodos.get(b);
        if (na == null || nb == null) {
            return false;
        }
        if (tieneLuz(a) && tieneLuz(b)) {
            return ruta(a).subestacion.equals(ruta(b).subestacion);
        }
        return lct.connected(na, nb);
    }

    /**
     * makeRoot(FUENTE) + access(nodo) deja en un solo splay el camino
     * FUENTE -> ... -> subestación -> ... -> nodo. Sus agregados dan:
     *  - sumaKm: km totales;  - sumaTramos: tramos reales;
     *  - maxMarca: la única subestación del camino.
     */
    @Override
    public Ruta ruta(String id) {
        if (!tieneLuz(id)) {
            return null;
        }
        Nodo camino = lct.camino(fuente, nodos.get(id));
        String subestacion = subestaciones.get(lct.maxMarca(camino));
        return new Ruta(subestacion, lct.sumaKm(camino), lct.sumaTramos(camino));
    }

    /** Una consulta connected por cliente: O(c log n) amortizado. */
    @Override
    public List<String> clientesSinLuz() {
        List<String> sinLuz = new ArrayList<>();
        for (String id : clientes) {
            if (!tieneLuz(id)) {
                sinLuz.add(id);
            }
        }
        Collections.sort(sinLuz);               // mismo orden que RedIngenua
        return sinLuz;
    }

    @Override
    public boolean existeNodo(String id) {
        return nodos.containsKey(id);
    }

    @Override
    public int numeroNodos() {
        return nodos.size();
    }

    @Override
    public List<String[]> tramos() {
        return new ArrayList<>(datosTramo.values());
    }

    /** Clave única del tramo sin importar el orden: "A|B" == "B|A". */
    static String clave(String a, String b) {
        return (a.compareTo(b) < 0) ? a + "|" + b : b + "|" + a;
    }
}
