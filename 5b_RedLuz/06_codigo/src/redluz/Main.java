package redluz;

import redluz.benchmark.Benchmark;
import redluz.io.LectorCsv;
import redluz.io.Mensajes;
import redluz.io.ProcesadorEventos;
import redluz.io.Tabla;
import redluz.modelo.RedDinamica.Resultado;
import redluz.modelo.RedElectrica;
import redluz.modelo.Ruta;
import redluz.modelo.Tipos;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * RedLuz: menú por consola.
 *
 * Esta clase solo pide datos y muestra resultados. La red (RedElectrica)
 * traduce cada pedido a operaciones del Link-Cut Tree (paquete motor).
 */
public class Main {

    private static final int MAX_FILAS_EN_PANTALLA = 40;

    private static final Scanner teclado = new Scanner(System.in);
    private static RedElectrica red = new RedElectrica();
    private static boolean pasoAPaso = false;     // RF-11: mostrar rotaciones y accesos
    private static boolean finDeEntrada = false;

    public static void main(String[] args) {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Opción: ");
            System.out.println();
            red.motor().activarTraza(pasoAPaso);  // limpia la traza antes de cada operación
            switch (opcion) {
                case 1: cargarCsv(); break;
                case 2: registrarPorTeclado(); break;
                case 3: verEstado(); break;
                case 4: conectar(); break;
                case 5: cortar(); break;
                case 6: consultarLuz(); break;
                case 7: consultarRuta(); break;
                case 8: consultarZona(); break;
                case 9: procesarEventos(); break;
                case 10: Benchmark.ejecutar(); break;
                case 11: alternarPasoAPaso(); break;
                case 0: System.out.println("Hasta luego."); break;
                default: System.out.println("Opción no válida.");
            }
            if (pasoAPaso && opcion >= 4 && opcion <= 8) {
                imprimirTraza();
            }
        } while (opcion != 0);
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("===== RedLuz · Red eléctrica dinámica =====");
        System.out.println("1. Cargar red desde CSV");
        System.out.println("2. Registrar nodo o tramo por teclado");
        System.out.println("3. Ver estado de la red");
        System.out.println("4. Conectar tramo");
        System.out.println("5. Cortar tramo");
        System.out.println("6. ¿El cliente tiene luz?");
        System.out.println("7. Ruta de alimentación del cliente");
        System.out.println("8. ¿Dos nodos están en la misma zona?");
        System.out.println("9. Procesar archivo de eventos");
        System.out.println("10. Ejecutar benchmark");
        System.out.println("11. Paso a paso del motor: " + (pasoAPaso ? "ACTIVADO" : "desactivado"));
        System.out.println("0. Salir");
    }

    // ------------------------------------------------------------------
    // 1 y 2. Entrada de datos
    // ------------------------------------------------------------------

    /** CU-01: carga nodos y tramos en una red nueva. */
    private static void cargarCsv() {
        String rutaNodos = leerTexto("CSV de nodos (Enter = datos/nodos.csv): ");
        String rutaTramos = leerTexto("CSV de tramos (Enter = datos/tramos.csv): ");
        RedElectrica nueva = new RedElectrica();
        List<String> errores = new ArrayList<>();
        try {
            int nodos = LectorCsv.cargarNodos(Paths.get(rutaNodos.isEmpty() ? "datos/nodos.csv" : rutaNodos),
                    nueva, errores);
            int tramos = LectorCsv.cargarTramos(Paths.get(rutaTramos.isEmpty() ? "datos/tramos.csv" : rutaTramos),
                    nueva, errores);
            red = nueva;
            System.out.printf("Se cargaron %d nodos y %d tramos.%n", nodos, tramos);
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo: " + e.getMessage());
            return;
        }
        for (String error : errores) {
            System.out.println("  Descartada -> " + error);
        }
        verEstado();
    }

    /** RF-02 */
    private static void registrarPorTeclado() {
        String que = leerTexto("¿Registrar (n)odo o (t)ramo? ").toLowerCase(Locale.ROOT);
        if (que.startsWith("n")) {
            String id = leerTexto("  Id del nodo (sin espacios): ");
            String tipo = leerTexto("  Tipo (SUBESTACION, POSTE, CLIENTE): ").toUpperCase(Locale.ROOT);
            if (!Tipos.esValido(tipo) || id.contains(" ")) {
                System.out.println("  Dato inválido.");
                return;
            }
            System.out.println("  " + Mensajes.de(red.agregarNodo(id, tipo)));
        } else if (que.startsWith("t")) {
            conectar();
        }
    }

    // ------------------------------------------------------------------
    // 3. Estado
    // ------------------------------------------------------------------

    /** RF-10 */
    private static void verEstado() {
        List<String[]> tramos = red.tramos();
        System.out.printf("Red: %d nodos y %d tramos activos.%n", red.numeroNodos(), tramos.size());
        if (tramos.isEmpty() || tramos.size() > MAX_FILAS_EN_PANTALLA) {
            return;
        }
        Tabla tabla = new Tabla("#", "Origen", "Destino", "km");
        int i = 1;
        for (String[] t : tramos) {
            tabla.agregarFila(String.valueOf(i++), t[0], t[1], t[2]);
        }
        tabla.imprimir();
    }

    // ------------------------------------------------------------------
    // 4 y 5. Cambios en la red
    // ------------------------------------------------------------------

    /** CU-02 */
    private static void conectar() {
        String[] partes = leerTexto("Tramo a conectar (origen destino km): ").split("\\s+");
        if (partes.length != 3 || LectorCsv.numero(partes[2]) == null) {
            System.out.println("Escriba: origen destino km   (ejemplo: P3 C7 0.2)");
            return;
        }
        Resultado r = red.conectar(partes[0], partes[1], LectorCsv.numero(partes[2]));
        System.out.println(r == Resultado.OK ? "Tramo conectado." : Mensajes.de(r));
    }

    /** CU-03 */
    private static void cortar() {
        String[] partes = leerTexto("Tramo a cortar (origen destino): ").split("\\s+");
        if (partes.length != 2) {
            System.out.println("Escriba: origen destino   (ejemplo: P3 C7)");
            return;
        }
        Resultado r = red.cortar(partes[0], partes[1]);
        System.out.println(r == Resultado.OK ? "Tramo cortado." : Mensajes.de(r));
    }

    // ------------------------------------------------------------------
    // 6, 7 y 8. Consultas
    // ------------------------------------------------------------------

    /** CU-04 */
    private static void consultarLuz() {
        String id = pedirNodo("Id del cliente: ");
        if (id != null) {
            System.out.println(id + ": " + (red.tieneLuz(id) ? "CON LUZ" : "SIN LUZ"));
        }
    }

    /** CU-05 */
    private static void consultarRuta() {
        String id = pedirNodo("Id del cliente: ");
        if (id == null) {
            return;
        }
        Ruta ruta = red.ruta(id);
        if (ruta == null) {
            System.out.println(id + " no tiene luz: no hay ruta de alimentación.");
            return;
        }
        Tabla tabla = new Tabla("Cliente", "Subestación", "Distancia (km)", "Tramos");
        tabla.agregarFila(id, ruta.subestacion, String.format(Locale.US, "%.2f", ruta.km), String.valueOf(ruta.tramos));
        tabla.imprimir();
    }

    /** RF-08 */
    private static void consultarZona() {
        String a = pedirNodo("Nodo A: ");
        String b = (a == null) ? null : pedirNodo("Nodo B: ");
        if (b != null) {
            System.out.println(red.mismaZona(a, b) ? "Están en la MISMA zona." : "Están en zonas DISTINTAS.");
        }
    }

    // ------------------------------------------------------------------
    // 9 y 11. Lote de eventos y paso a paso
    // ------------------------------------------------------------------

    /** CU-06 */
    private static void procesarEventos() {
        String ruta = leerTexto("CSV de eventos (Enter = datos/eventos.csv): ");
        try {
            ProcesadorEventos.procesar(Paths.get(ruta.isEmpty() ? "datos/eventos.csv" : ruta), red);
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo: " + e.getMessage());
        }
    }

    private static void alternarPasoAPaso() {
        pasoAPaso = !pasoAPaso;
        System.out.println("Paso a paso del motor " + (pasoAPaso ? "ACTIVADO" : "desactivado") + ".");
    }

    private static void imprimirTraza() {
        List<String> traza = red.motor().traza();
        Tabla tabla = new Tabla("#", "Operación del motor");
        int limite = Math.min(traza.size(), MAX_FILAS_EN_PANTALLA);
        for (int i = 0; i < limite; i++) {
            tabla.agregarFila(String.valueOf(i + 1), traza.get(i));
        }
        System.out.println("Paso a paso del Link-Cut Tree:");
        tabla.imprimir();
        if (traza.size() > limite) {
            System.out.printf("(Se muestran %d de %d pasos.)%n", limite, traza.size());
        }
    }

    // ------------------------------------------------------------------
    // Utilidades de consola
    // ------------------------------------------------------------------

    /** Pide un id; devuelve null si no existe. */
    private static String pedirNodo(String mensaje) {
        String id = leerTexto(mensaje);
        if (!red.existeNodo(id)) {
            System.out.println("No existe el nodo '" + id + "'.");
            return null;
        }
        return id;
    }

    /** Lee una línea. Si la entrada se terminó (Ctrl+Z), devuelve texto vacío. */
    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!teclado.hasNextLine()) {
            finDeEntrada = true;
            return "";
        }
        return teclado.nextLine().trim();
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (finDeEntrada) {
                return 0;
            }
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("  Escriba un número entero.");
            }
        }
    }
}
