package redluz.io;

import java.util.ArrayList;
import java.util.List;

/**
 * Imprime tablas de texto alineadas en la consola.
 *
 *   +-----+---------------+
 *   | #   | Tramo         |
 *   +-----+---------------+
 *   | 1   | Lima - Callao |
 *   +-----+---------------+
 *
 * Primero se agregan todas las filas; al imprimir se calcula el ancho de
 * cada columna como el texto más largo de esa columna.
 */
public class Tabla {

    private final String[] encabezados;
    private final List<String[]> filas = new ArrayList<>();

    public Tabla(String... encabezados) {
        this.encabezados = encabezados;
    }

    public void agregarFila(String... celdas) {
        filas.add(celdas);
    }

    public void imprimir() {
        // 1. Ancho de cada columna = el texto más largo (encabezado incluido).
        int[] anchos = new int[encabezados.length];
        for (int c = 0; c < encabezados.length; c++) {
            anchos[c] = encabezados[c].length();
        }
        for (String[] fila : filas) {
            for (int c = 0; c < anchos.length && c < fila.length; c++) {
                anchos[c] = Math.max(anchos[c], fila[c].length());
            }
        }

        // 2. Imprimir: borde, encabezados, borde, filas, borde.
        String borde = linea(anchos);
        System.out.println(borde);
        System.out.println(fila(encabezados, anchos));
        System.out.println(borde);
        for (String[] f : filas) {
            System.out.println(fila(f, anchos));
        }
        System.out.println(borde);
    }

    /** Arma "+-----+------+" según los anchos. */
    private static String linea(int[] anchos) {
        StringBuilder sb = new StringBuilder("+");
        for (int ancho : anchos) {
            sb.append("-".repeat(ancho + 2)).append("+");
        }
        return sb.toString();
    }

    /** Arma "| a   | b    |" rellenando cada celda con espacios. */
    private static String fila(String[] celdas, int[] anchos) {
        StringBuilder sb = new StringBuilder("|");
        for (int c = 0; c < anchos.length; c++) {
            String texto = (c < celdas.length) ? celdas[c] : "";
            sb.append(" ").append(texto).append(" ".repeat(anchos[c] - texto.length())).append(" |");
        }
        return sb.toString();
    }
}
