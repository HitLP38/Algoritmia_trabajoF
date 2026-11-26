package fibranet.io;

import fibranet.modelo.Grafo;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee la red desde un CSV con el formato:
 *
 *   origen,destino,costo
 *   Lima,Callao,12
 *   Pachacamac          (una sola columna: sede aislada, sin tramos)
 *
 * Valida cada fila (RF-03). Las filas inválidas no detienen la lectura:
 * se guardan en la lista de errores con su número de línea.
 */
public final class LectorCsv {

    /** En el CSV se separa con coma (o punto y coma, como guarda Excel en español). */
    public static final String SEPARADOR_CSV = "\\s*[,;]\\s*";

    /** Por teclado se separa con espacios: "Lima Callao 12". */
    public static final String SEPARADOR_TECLADO = "\\s+";

    private LectorCsv() {
        // clase de utilidad
    }

    /**
     * @param ruta    ruta del archivo CSV
     * @param grafo   red donde se agregan los tramos leídos
     * @param errores aquí se anotan las filas descartadas
     * @return cantidad de tramos válidos leídos
     * @throws IOException si el archivo no existe o no se puede leer
     */
    public static int leer(Path ruta, Grafo grafo, List<String> errores) throws IOException {
        int validos = 0;
        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            String linea = lector.readLine();               // la primera línea es el encabezado
            int numero = 1;
            while ((linea = lector.readLine()) != null) {
                numero++;
                if (linea.isBlank()) {
                    continue;                               // las líneas vacías se ignoran
                }
                String sola = linea.trim();
                if (!sola.contains(",") && !sola.contains(";")) {
                    grafo.agregarSede(sola);                // fila de una sola columna: sede sin tramos (aislada)
                    continue;
                }
                String error = agregarTramo(linea, SEPARADOR_CSV, grafo);
                if (error == null) {
                    validos++;
                } else {
                    errores.add("Línea " + numero + ": " + error);
                }
            }
        }
        return validos;
    }

    /**
     * Valida una fila "origen,destino,costo" y, si está bien, la agrega.
     * También la usa la entrada por teclado, que separa con espacios.
     *
     * @param separador expresión regular que separa los valores
     * @return null si se agregó, o el motivo del error
     */
    public static String agregarTramo(String linea, String separador, Grafo grafo) {
        String[] partes = linea.trim().split(separador, -1);   // -1: no descarta campos vacíos al final
        if (partes.length != 3) {
            return "se esperaban 3 valores (origen, destino, costo) y hay " + partes.length;
        }
        String origen = partes[0].trim();
        String destino = partes[1].trim();
        if (origen.isEmpty() || destino.isEmpty()) {
            return "sede vacía";
        }
        if (origen.equals(destino)) {
            return "el origen y el destino son la misma sede (" + origen + ")";
        }
        double costo;
        try {
            costo = Double.parseDouble(partes[2].trim());   // acepta punto decimal
        } catch (NumberFormatException e) {
            return "el costo '" + partes[2] + "' no es un número";
        }
        if (costo <= 0 || Double.isNaN(costo) || Double.isInfinite(costo)) {
            return "el costo debe ser positivo";
        }
        grafo.agregarTramo(origen, destino, costo);
        return null;
    }

    /** Para leer el CSV sin lista de errores (pruebas y benchmark). */
    public static Grafo leer(Path ruta) throws IOException {
        Grafo grafo = new Grafo();
        leer(ruta, grafo, new ArrayList<>());
        return grafo;
    }
}
