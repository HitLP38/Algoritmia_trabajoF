package redluz.io;

import redluz.modelo.RedDinamica;
import redluz.modelo.Ruta;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Ejecuta un archivo de eventos en orden (RF-09, CU-06):
 *
 *   evento,a,b,km[,nota]       (nota opcional: fecha u origen del evento)
 *   LUZ,C1,,
 *   CORTAR,S1,P1,
 *   CONECTAR,S1,P1,0.8
 *   RUTA,C1,,
 *   ZONA,C1,C2,
 *   AFECTADOS,,,               (cuántos clientes están sin luz ahora)
 *
 * y muestra una tabla con el resultado de cada uno.
 */
public final class ProcesadorEventos {

    private ProcesadorEventos() {
    }

    public static void procesar(Path ruta, RedDinamica red) throws IOException {
        Tabla tabla = new Tabla("#", "Evento", "Datos", "Resultado");
        int total = 0;
        int rechazados = 0;
        long inicio = System.nanoTime();

        for (String[] f : LectorCsv.leerFilas(ruta)) {
            total++;
            String evento = (f.length > 1) ? f[1].toUpperCase(Locale.ROOT) : "";
            String a = (f.length > 2) ? f[2] : "";
            String b = (f.length > 3) ? f[3] : "";
            String km = (f.length > 4) ? f[4] : "";
            String nota = (f.length > 5) ? f[5] : "";     // opcional: fecha u origen del evento real
            String resultado = ejecutar(red, evento, a, b, km);
            if (resultado.startsWith("RECHAZADO") || resultado.startsWith("ERROR")) {
                rechazados++;
            }
            String datos = b.isEmpty() ? a : a + " - " + b + (km.isEmpty() ? "" : " (" + km + " km)");
            if (!nota.isEmpty()) {
                datos = datos + " · " + nota;
            }
            tabla.agregarFila(String.valueOf(total), evento, datos, resultado);
        }

        double ms = (System.nanoTime() - inicio) / 1e6;
        tabla.imprimir();
        System.out.printf(Locale.US, "Eventos: %d · rechazados o con error: %d · tiempo: %.3f ms%n",
                total, rechazados, ms);
    }

    /** Ejecuta un evento y devuelve el texto del resultado. */
    static String ejecutar(RedDinamica red, String evento, String a, String b, String km) {
        switch (evento) {
            case "CONECTAR": {
                Double valor = LectorCsv.numero(km);
                if (valor == null) {
                    return "ERROR: km inválido";
                }
                return texto(red.conectar(a, b, valor));
            }
            case "CORTAR":
                return texto(red.cortar(a, b));
            case "LUZ":
                if (!red.existeNodo(a)) {
                    return "ERROR: nodo desconocido";
                }
                return red.tieneLuz(a) ? "CON LUZ" : "SIN LUZ";
            case "RUTA": {
                if (!red.existeNodo(a)) {
                    return "ERROR: nodo desconocido";
                }
                Ruta r = red.ruta(a);
                return (r == null) ? "SIN LUZ"
                        : String.format(Locale.US, "desde %s: %.2f km, %d tramos", r.subestacion, r.km, r.tramos);
            }
            case "AFECTADOS": {
                // Clientes sin luz en este momento (p. ej. tras un corte programado).
                List<String> sinLuz = red.clientesSinLuz();
                if (sinLuz.isEmpty()) {
                    return "0 clientes sin luz";
                }
                int mostrar = Math.min(5, sinLuz.size());
                String lista = String.join(", ", sinLuz.subList(0, mostrar));
                return sinLuz.size() + " clientes sin luz: " + lista + (sinLuz.size() > mostrar ? ", …" : "");
            }
            case "ZONA":
                return red.mismaZona(a, b) ? "MISMA ZONA" : "ZONAS DISTINTAS";
            default:
                return "ERROR: evento desconocido '" + evento + "'";
        }
    }

    private static String texto(RedDinamica.Resultado r) {
        if (r == RedDinamica.Resultado.OK) {
            return "OK";
        }
        String mensaje = Mensajes.de(r);
        return mensaje.startsWith("RECHAZADO") ? mensaje : "ERROR: " + mensaje;
    }
}
