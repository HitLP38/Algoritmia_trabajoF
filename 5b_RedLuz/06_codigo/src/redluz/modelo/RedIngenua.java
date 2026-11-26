package redluz.modelo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * La misma red, resuelta "a mano" con listas de adyacencia y BFS.
 *
 * Es correcta pero lenta: cada consulta recorre la zona entera, O(n).
 * Sirve como referencia en las pruebas (sus respuestas son fáciles de
 * creer) y como línea base en el benchmark.
 */
public class RedIngenua implements RedDinamica {

    private final Map<String, Map<String, Double>> vecinos = new HashMap<>();  // id -> (vecino -> km)
    private final Map<String, String> tipos = new HashMap<>();
    private final Map<String, String[]> datosTramo = new LinkedHashMap<>();

    @Override
    public Resultado agregarNodo(String id, String tipo) {
        if (id == null || id.isEmpty() || !Tipos.esValido(tipo)) {
            return Resultado.DATO_INVALIDO;
        }
        if (tipos.containsKey(id)) {
            return Resultado.YA_EXISTE;
        }
        tipos.put(id, tipo);
        vecinos.put(id, new HashMap<>());
        return Resultado.OK;
    }

    @Override
    public Resultado conectar(String a, String b, double km) {
        if (!tipos.containsKey(a) || !tipos.containsKey(b)) {
            return Resultado.NODO_DESCONOCIDO;
        }
        if (a.equals(b) || !(km > 0)) {
            return Resultado.DATO_INVALIDO;
        }
        if (vecinos.get(a).containsKey(b)) {
            return Resultado.YA_EXISTE;
        }
        // Anillo si ya hay camino entre a y b, o si las dos zonas ya tienen
        // subestación (en el modelo con FUENTE eso también cierra un anillo).
        if (bfs(a).containsKey(b) || (subestacionDe(a) != null && subestacionDe(b) != null)) {
            return Resultado.ANILLO;
        }
        vecinos.get(a).put(b, km);
        vecinos.get(b).put(a, km);
        datosTramo.put(RedElectrica.clave(a, b), new String[] {a, b, String.valueOf(km)});
        return Resultado.OK;
    }

    @Override
    public Resultado cortar(String a, String b) {
        if (!tipos.containsKey(a) || !tipos.containsKey(b)) {
            return Resultado.NODO_DESCONOCIDO;
        }
        if (vecinos.get(a).remove(b) == null) {
            return Resultado.NO_EXISTE;
        }
        vecinos.get(b).remove(a);
        datosTramo.remove(RedElectrica.clave(a, b));
        return Resultado.OK;
    }

    @Override
    public boolean tieneLuz(String id) {
        return tipos.containsKey(id) && subestacionDe(id) != null;
    }

    @Override
    public boolean mismaZona(String a, String b) {
        return tipos.containsKey(a) && tipos.containsKey(b) && bfs(a).containsKey(b);
    }

    @Override
    public Ruta ruta(String id) {
        if (!tipos.containsKey(id)) {
            return null;
        }
        Map<String, String> padreDe = bfs(id);
        String sub = null;
        for (String nodo : padreDe.keySet()) {
            if (Tipos.SUBESTACION.equals(tipos.get(nodo))) {
                sub = nodo;
                break;
            }
        }
        if (sub == null) {
            return null;
        }
        // Recorre el árbol BFS desde la subestación de vuelta hasta id sumando km.
        double km = 0;
        int tramos = 0;
        for (String actual = sub; !actual.equals(id); actual = padreDe.get(actual)) {
            km += vecinos.get(actual).get(padreDe.get(actual));
            tramos++;
        }
        return new Ruta(sub, km, tramos);
    }

    /** BFS desde origen. Devuelve, para cada nodo alcanzado, desde qué nodo se llegó. O(n). */
    private Map<String, String> bfs(String origen) {
        Map<String, String> padreDe = new LinkedHashMap<>();
        ArrayDeque<String> cola = new ArrayDeque<>();
        padreDe.put(origen, origen);
        cola.add(origen);
        while (!cola.isEmpty()) {
            String actual = cola.poll();
            for (String vecino : vecinos.get(actual).keySet()) {
                if (!padreDe.containsKey(vecino)) {
                    padreDe.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }
        return padreDe;
    }

    private String subestacionDe(String id) {
        for (String nodo : bfs(id).keySet()) {
            if (Tipos.SUBESTACION.equals(tipos.get(nodo))) {
                return nodo;
            }
        }
        return null;
    }

    @Override
    public boolean existeNodo(String id) {
        return tipos.containsKey(id);
    }

    @Override
    public int numeroNodos() {
        return tipos.size();
    }

    @Override
    public List<String[]> tramos() {
        return new ArrayList<>(datosTramo.values());
    }
}
