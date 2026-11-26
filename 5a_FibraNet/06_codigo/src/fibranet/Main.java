package fibranet;

import fibranet.benchmark.Benchmark;
import fibranet.io.EscritorCsv;
import fibranet.io.LectorCsv;
import fibranet.io.Tabla;
import fibranet.modelo.Arista;
import fibranet.modelo.Grafo;
import fibranet.motor.Kruskal;
import fibranet.motor.Prim;
import fibranet.motor.ResultadoMST;
import fibranet.motor.UnionFind;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * FibraNet: menú por consola.
 *
 * Esta clase solo pide datos y muestra resultados. Los cálculos los hace
 * el paquete motor (UnionFind, Kruskal, Prim), que no sabe nada de la consola.
 */
public class Main {

    private static final int MAX_PASOS_EN_PANTALLA = 40;  // para no llenar la consola con redes grandes

    private static final Scanner teclado = new Scanner(System.in);
    private static Grafo red = new Grafo();               // red cargada
    private static ResultadoMST ultimoPlan = null;        // último plan calculado
    private static boolean finDeEntrada = false;          // true si se cerró la entrada estándar

    public static void main(String[] args) {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Opción: ");
            System.out.println();
            switch (opcion) {
                case 1: cargarCsv(); break;
                case 2: ingresarPorTeclado(); break;
                case 3: verRed(); break;
                case 4: planKruskal(); break;
                case 5: planPrim(); break;
                case 6: comparar(); break;
                case 7: consultarConexion(); break;
                case 8: exportarPlan(); break;
                case 9: Benchmark.ejecutar(); break;
                case 0: System.out.println("Hasta luego."); break;
                default: System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("===== FibraNet · Red de fibra de costo mínimo =====");
        System.out.println("1. Cargar red desde CSV");
        System.out.println("2. Ingresar red por teclado");
        System.out.println("3. Ver red cargada");
        System.out.println("4. Plan con Kruskal");
        System.out.println("5. Plan con Prim");
        System.out.println("6. Comparar Kruskal y Prim");
        System.out.println("7. Consultar si dos sedes quedan conectadas");
        System.out.println("8. Exportar último plan a CSV");
        System.out.println("9. Ejecutar benchmark");
        System.out.println("0. Salir");
    }

    // ------------------------------------------------------------------
    // 1 y 2. Entrada de datos
    // ------------------------------------------------------------------

    /** CU-01: carga la red desde un CSV y reporta las filas descartadas. */
    private static void cargarCsv() {
        String ruta = leerTexto("Ruta del CSV (Enter = datos/red_ejemplo.csv): ");
        if (ruta.isEmpty()) {
            ruta = "datos/red_ejemplo.csv";
        }
        Grafo nueva = new Grafo();
        List<String> errores = new ArrayList<>();
        try {
            int leidos = LectorCsv.leer(Paths.get(ruta), nueva, errores);
            red = nueva;
            ultimoPlan = null;
            System.out.printf("Se leyeron %d tramos válidos.%n", leidos);
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo: " + e.getMessage());
            return;
        }
        for (String error : errores) {
            System.out.println("  Descartada -> " + error);
        }
        verRed();
    }

    /** CU-02: el usuario escribe las sedes y luego los tramos. */
    private static void ingresarPorTeclado() {
        Grafo nueva = new Grafo();
        int cantidad = leerEntero("¿Cuántas sedes? ");
        for (int i = 1; i <= cantidad; i++) {
            String nombre = leerTexto("  Nombre de la sede " + i + ": ");
            if (finDeEntrada) {
                return;
            }
            if (nombre.isEmpty() || nombre.contains(" ")) {
                System.out.println("  El nombre no puede estar vacío ni tener espacios (use Santa_Anita).");
                i--;                                        // se repite la misma sede
                continue;
            }
            nueva.agregarSede(nombre);
        }
        System.out.println("Escriba cada tramo como: origen destino costo   (línea vacía para terminar)");
        while (true) {
            String linea = leerTexto("  Tramo: ");
            if (linea.isEmpty()) {
                break;
            }
            String[] partes = linea.split("\\s+");
            if (partes.length >= 2 && (nueva.indice(partes[0]) < 0 || nueva.indice(partes[1]) < 0)) {
                System.out.println("  Esa sede no está registrada. Intente de nuevo.");
                continue;
            }
            String error = LectorCsv.agregarTramo(linea, LectorCsv.SEPARADOR_TECLADO, nueva);
            if (error != null) {
                System.out.println("  Error: " + error);
            }
        }
        red = nueva;
        ultimoPlan = null;
        verRed();
    }

    // ------------------------------------------------------------------
    // 3. Ver red
    // ------------------------------------------------------------------

    private static void verRed() {
        if (sinRed()) {
            return;
        }
        System.out.printf("Red cargada: %d sedes y %d tramos posibles.%n", red.numeroSedes(), red.numeroTramos());
        if (red.numeroTramos() > MAX_PASOS_EN_PANTALLA) {
            System.out.println("(La red es grande; no se listan los tramos.)");
            return;
        }
        Tabla tabla = new Tabla("#", "Origen", "Destino", "Costo");
        int i = 1;
        for (Arista a : red.aristas()) {
            tabla.agregarFila(String.valueOf(i++), red.nombre(a.origen), red.nombre(a.destino), dinero(a.costo));
        }
        tabla.imprimir();
        System.out.println("Costo de tender TODOS los tramos: " + dinero(red.costoTotalDeTodosLosTramos()));
    }

    // ------------------------------------------------------------------
    // 4, 5 y 6. Cálculo del plan
    // ------------------------------------------------------------------

    /** CU-03 */
    private static void planKruskal() {
        if (sinRed()) {
            return;
        }
        boolean pasos = preguntarSiNo("¿Ver paso a paso? (s/n): ");
        ultimoPlan = Kruskal.calcular(red, pasos);
        if (pasos) {
            imprimirPasos(new Tabla("Paso", "Tramo evaluado", "Costo", "Decisión"), ultimoPlan);
        }
        imprimirPlan(ultimoPlan);
    }

    /** CU-04 */
    private static void planPrim() {
        if (sinRed()) {
            return;
        }
        int inicio = pedirSede("Sede inicial (Enter = " + red.nombre(0) + "): ", true);
        boolean pasos = preguntarSiNo("¿Ver paso a paso? (s/n): ");
        ultimoPlan = Prim.calcular(red, inicio, pasos);
        if (pasos) {
            imprimirPasos(new Tabla("Paso", "Sede que entra", "Tramo usado", "Costo", "Decisión"), ultimoPlan);
        }
        imprimirPlan(ultimoPlan);
    }

    /** CU-05: corre los dos algoritmos sobre la misma red. */
    private static void comparar() {
        if (sinRed()) {
            return;
        }
        ResultadoMST k = Kruskal.calcular(red, false);
        ResultadoMST p = Prim.calcular(red, 0, false);
        Tabla tabla = new Tabla("Algoritmo", "Tramos", "Costo total", "Grupos", "Tiempo (ms)");
        for (ResultadoMST r : new ResultadoMST[] {k, p}) {
            tabla.agregarFila(r.algoritmo, String.valueOf(r.tramosElegidos.size()), dinero(r.costoTotal),
                    String.valueOf(r.grupos), String.format(Locale.US, "%.3f", r.nanosegundos / 1e6));
        }
        tabla.imprimir();
        boolean iguales = Math.abs(k.costoTotal - p.costoTotal) < 1e-6;
        System.out.println(iguales
                ? "Los dos algoritmos encuentran el mismo costo mínimo (los tramos pueden variar si hay empates)."
                : "ATENCIÓN: los costos no coinciden.");
        ultimoPlan = k;
    }

    // ------------------------------------------------------------------
    // 7 y 8. Consultas y exportación
    // ------------------------------------------------------------------

    /** CU-06: usa Union-Find sobre los tramos del plan. */
    private static void consultarConexion() {
        if (sinPlan()) {
            return;
        }
        int a = pedirSede("Sede A: ", false);
        int b = pedirSede("Sede B: ", false);
        UnionFind grupos = Kruskal.gruposDelPlan(red, ultimoPlan);
        int raizA = grupos.find(a);
        int raizB = grupos.find(b);
        Tabla tabla = new Tabla("Sede", "Representante de su grupo");
        tabla.agregarFila(red.nombre(a), red.nombre(raizA));
        tabla.agregarFila(red.nombre(b), red.nombre(raizB));
        tabla.imprimir();
        System.out.println(raizA == raizB
                ? "Quedan CONECTADAS por la fibra del plan."
                : "NO quedan conectadas: están en grupos distintos.");
    }

    /** CU-08 */
    private static void exportarPlan() {
        if (sinPlan()) {
            return;
        }
        Path ruta = Paths.get("datos", "plan_" + ultimoPlan.algoritmo.toLowerCase(Locale.ROOT) + ".csv");
        try {
            EscritorCsv.escribirPlan(ruta, red, ultimoPlan);
            System.out.println("Plan guardado en: " + ruta.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("No se pudo guardar: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Impresión de resultados
    // ------------------------------------------------------------------

    private static void imprimirPasos(Tabla tabla, ResultadoMST plan) {
        int mostrados = 0;
        for (String[] paso : plan.pasos) {
            if (mostrados++ == MAX_PASOS_EN_PANTALLA) {
                break;
            }
            tabla.agregarFila(paso);
        }
        System.out.println("Paso a paso de " + plan.algoritmo + ":");
        tabla.imprimir();
        if (plan.pasos.size() > MAX_PASOS_EN_PANTALLA) {
            System.out.printf("(Se muestran %d de %d pasos.)%n", MAX_PASOS_EN_PANTALLA, plan.pasos.size());
        }
    }

    /** RF-08 y RF-10: tabla del plan, costo, ahorro y grupos. */
    private static void imprimirPlan(ResultadoMST plan) {
        System.out.println("Plan de tendido con " + plan.algoritmo + ":");
        if (plan.tramosElegidos.size() <= MAX_PASOS_EN_PANTALLA) {
            Tabla tabla = new Tabla("#", "Origen", "Destino", "Costo");
            int i = 1;
            for (Arista a : plan.tramosElegidos) {
                tabla.agregarFila(String.valueOf(i++), red.nombre(a.origen), red.nombre(a.destino), dinero(a.costo));
            }
            tabla.imprimir();
        }
        double todos = red.costoTotalDeTodosLosTramos();
        Tabla resumen = new Tabla("Dato", "Valor");
        resumen.agregarFila("Tramos elegidos", plan.tramosElegidos.size() + " de " + red.numeroTramos());
        resumen.agregarFila("Costo total del plan", dinero(plan.costoTotal));
        resumen.agregarFila("Costo de tender todo", dinero(todos));
        resumen.agregarFila("Ahorro", String.format(Locale.US, "%.1f %%", 100 * (todos - plan.costoTotal) / todos));
        resumen.agregarFila("Tiempo", String.format(Locale.US, "%.3f ms", plan.nanosegundos / 1e6));
        resumen.agregarFila("Red conexa", plan.esConexo() ? "Sí" : "No (" + plan.grupos + " grupos)");
        resumen.imprimir();
        if (!plan.esConexo()) {
            imprimirGrupos(plan);
        }
    }

    /** RF-10: lista qué sedes quedaron juntas cuando la red no es conexa. */
    private static void imprimirGrupos(ResultadoMST plan) {
        UnionFind grupos = Kruskal.gruposDelPlan(red, plan);
        Map<Integer, List<String>> porRaiz = new HashMap<>();
        for (int s = 0; s < red.numeroSedes(); s++) {
            porRaiz.computeIfAbsent(grupos.find(s), r -> new ArrayList<>()).add(red.nombre(s));
        }
        Tabla tabla = new Tabla("Grupo", "Sedes");
        int g = 1;
        for (List<String> sedes : porRaiz.values()) {
            tabla.agregarFila(String.valueOf(g++), String.join(", ", sedes));
        }
        System.out.println("Grupos de sedes que no se pueden unir entre sí:");
        tabla.imprimir();
    }

    // ------------------------------------------------------------------
    // Utilidades de consola
    // ------------------------------------------------------------------

    private static String dinero(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }

    private static boolean sinRed() {
        if (red.estaVacio()) {
            System.out.println("Primero cargue una red (opción 1 o 2).");
            return true;
        }
        return false;
    }

    private static boolean sinPlan() {
        if (sinRed()) {
            return true;
        }
        if (ultimoPlan == null) {
            System.out.println("Primero calcule un plan (opción 4, 5 o 6).");
            return true;
        }
        return false;
    }

    /** Pide un nombre de sede hasta que exista. Con permitirVacio, Enter = sede 0. */
    private static int pedirSede(String mensaje, boolean permitirVacio) {
        while (true) {
            String nombre = leerTexto(mensaje);
            if (finDeEntrada || (nombre.isEmpty() && permitirVacio)) {
                return 0;
            }
            int indice = red.indice(nombre);
            if (indice >= 0) {
                return indice;
            }
            System.out.println("  No existe la sede '" + nombre + "'.");
        }
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

    /** Lee un entero; si el usuario escribe otra cosa, vuelve a preguntar. */
    private static int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (finDeEntrada) {
                return 0;                                   // sin más entrada: equivale a Salir
            }
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("  Escriba un número entero.");
            }
        }
    }

    private static boolean preguntarSiNo(String mensaje) {
        return leerTexto(mensaje).toLowerCase(Locale.ROOT).startsWith("s");
    }
}
