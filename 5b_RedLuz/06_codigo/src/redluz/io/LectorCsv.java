package redluz.io;

import redluz.modelo.RedDinamica;
import redluz.modelo.RedDinamica.Resultado;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Lee los CSV de RedLuz (RF-01, RF-03):
 *
 *   nodos.csv        tramos.csv
 *   id,tipo          origen,destino,km
 *   S1,SUBESTACION   S1,P1,0.8
 *
 * Las filas inválidas no detienen la lectura: se anotan con su línea.
 */
public final class LectorCsv {

    /** Coma o punto y coma (Excel en español guarda con punto y coma). */
    public static final String SEPARADOR = "\\s*[,;]\\s*";

    private LectorCsv() {
    }

    /** Lee todas las líneas de datos (sin el encabezado ni líneas vacías), con su número. */
    public static List<String[]> leerFilas(Path ruta) throws IOException {
        List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        List<String[]> filas = new ArrayList<>();
        for (int i = 1; i < lineas.size(); i++) {           // i = 0 es el encabezado
            String linea = lineas.get(i).trim();
            if (!linea.isEmpty()) {
                String[] partes = linea.split(SEPARADOR, -1);
                String[] conNumero = new String[partes.length + 1];
                conNumero[0] = String.valueOf(i + 1);         // número de línea del archivo
                System.arraycopy(partes, 0, conNumero, 1, partes.length);
                filas.add(conNumero);
            }
        }
        return filas;
    }

    /** Carga los nodos. Devuelve cuántos se agregaron. */
    public static int cargarNodos(Path ruta, RedDinamica red, List<String> errores) throws IOException {
        int agregados = 0;
        for (String[] f : leerFilas(ruta)) {
            if (f.length < 3) {
                errores.add("Línea " + f[0] + ": se esperaba id,tipo");
                continue;
            }
            Resultado r = red.agregarNodo(f[1], f[2].toUpperCase(Locale.ROOT));
            if (r == Resultado.OK) {
                agregados++;
            } else {
                errores.add("Línea " + f[0] + " (" + f[1] + "): " + Mensajes.de(r));
            }
        }
        return agregados;
    }

    /** Carga los tramos. Devuelve cuántos se conectaron. */
    public static int cargarTramos(Path ruta, RedDinamica red, List<String> errores) throws IOException {
        int conectados = 0;
        for (String[] f : leerFilas(ruta)) {
            if (f.length < 4) {
                errores.add("Línea " + f[0] + ": se esperaba origen,destino,km");
                continue;
            }
            Double km = numero(f[3]);
            if (km == null) {
                errores.add("Línea " + f[0] + ": km '" + f[3] + "' no es un número");
                continue;
            }
            Resultado r = red.conectar(f[1], f[2], km);
            if (r == Resultado.OK) {
                conectados++;
            } else {
                errores.add("Línea " + f[0] + " (" + f[1] + "-" + f[2] + "): " + Mensajes.de(r));
            }
        }
        return conectados;
    }

    /** Convierte texto a número; null si no se puede. */
    public static Double numero(String texto) {
        try {
            return Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
