package fibranet.io;

import fibranet.modelo.Geo;
import fibranet.modelo.Grafo;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Arma la Red Dorsal Nacional a partir de un CSV de sedes con coordenadas:
 *
 *     departamento,capital,latitud,longitud
 *     Lima,Lima,-12.0464,-77.0428
 *
 * Tramos candidatos: cada sede se une con sus k sedes más cercanas
 * (así no se proponen tramos absurdos como Tumbes - Tacna). El costo de
 * cada tramo es la distancia haversine en km, redondeada a 0,1 km.
 *
 * Costo: para n sedes se calculan n·(n-1)/2 distancias y se ordenan las
 * de cada sede: O(n² log n). Con 25 capitales es instantáneo.
 */
public final class LectorCoordenadas {

    private LectorCoordenadas() {
        // clase de utilidad
    }

    /** Una sede leída del CSV. */
    private static final class Punto {
        final String nombre;
        final double lat;
        final double lon;

        Punto(String nombre, double lat, double lon) {
            this.nombre = nombre;
            this.lat = lat;
            this.lon = lon;
        }
    }

    /**
     * @param ruta     CSV con encabezado departamento,capital,latitud,longitud
     * @param vecinos  k: cuántas sedes cercanas se proponen como tramo por cada sede
     * @param errores  aquí se anotan las filas descartadas con su número de línea
     * @return el grafo con las sedes y los tramos candidatos
     */
    public static Grafo leer(Path ruta, int vecinos, List<String> errores) throws IOException {
        List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        List<Punto> puntos = new ArrayList<>();
        Set<String> vistos = new HashSet<>();

        // 1. Leer y validar cada fila (la primera es el encabezado).
        for (int i = 1; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (linea.isEmpty()) {
                continue;
            }
            String[] c = linea.split(LectorCsv.SEPARADOR_CSV);
            if (c.length < 4) {
                errores.add("línea " + (i + 1) + ": se esperaban 4 columnas");
                continue;
            }
            try {
                double lat = Double.parseDouble(c[2]);
                double lon = Double.parseDouble(c[3]);
                if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                    errores.add("línea " + (i + 1) + ": coordenadas fuera de rango");
                } else if (!vistos.add(c[1])) {
                    errores.add("línea " + (i + 1) + ": sede repetida '" + c[1] + "'");
                } else {
                    puntos.add(new Punto(c[1], lat, lon));
                }
            } catch (NumberFormatException e) {
                errores.add("línea " + (i + 1) + ": latitud o longitud no numérica");
            }
        }

        // 2. Registrar todas las sedes (aunque alguna quede aislada).
        Grafo grafo = new Grafo();
        for (Punto p : puntos) {
            grafo.agregarSede(p.nombre);
        }

        // 3. Para cada sede, proponer tramos a sus k vecinas más cercanas.
        //    Se usa un conjunto "i-j" (i < j) para no repetir el mismo tramo.
        Set<String> tramos = new HashSet<>();
        int n = puntos.size();
        for (int i = 0; i < n; i++) {
            Punto a = puntos.get(i);
            Integer[] orden = new Integer[n];
            double[] dist = new double[n];
            for (int j = 0; j < n; j++) {
                orden[j] = j;
                dist[j] = Geo.haversineKm(a.lat, a.lon, puntos.get(j).lat, puntos.get(j).lon);
            }
            Arrays.sort(orden, (x, y) -> Double.compare(dist[x], dist[y]));
            int agregados = 0;
            for (int k = 0; k < n && agregados < vecinos; k++) {
                int j = orden[k];
                if (j == i) {
                    continue;                                   // la misma sede
                }
                agregados++;
                String clave = Math.min(i, j) + "-" + Math.max(i, j);
                if (tramos.add(clave)) {
                    double km = Math.round(dist[j] * 10) / 10.0;
                    grafo.agregarTramo(a.nombre, puntos.get(j).nombre, Math.max(km, 0.1));
                }
            }
        }
        return grafo;
    }
}
