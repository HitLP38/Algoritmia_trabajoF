package fibranet.io;

import fibranet.modelo.Arista;
import fibranet.modelo.Grafo;
import fibranet.motor.ResultadoMST;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Escribe el plan de tendido en un CSV (RF-12) con el mismo formato
 * de entrada, para poder volver a cargarlo o abrirlo en Excel.
 */
public final class EscritorCsv {

    private EscritorCsv() {
        // clase de utilidad
    }

    public static void escribirPlan(Path ruta, Grafo grafo, ResultadoMST plan) throws IOException {
        if (ruta.getParent() != null) {
            Files.createDirectories(ruta.getParent());     // crea la carpeta si no existe
        }
        try (PrintWriter salida = new PrintWriter(Files.newBufferedWriter(ruta, StandardCharsets.UTF_8))) {
            salida.println("origen,destino,costo");
            for (Arista arista : plan.tramosElegidos) {
                salida.printf(Locale.US, "%s,%s,%.2f%n",
                        grafo.nombre(arista.origen), grafo.nombre(arista.destino), arista.costo);
            }
        }
    }
}
