package fibranet.benchmark;

import fibranet.io.Tabla;
import fibranet.modelo.Grafo;
import fibranet.motor.Kruskal;
import fibranet.motor.Prim;
import fibranet.motor.ResultadoMST;
import fibranet.motor.UnionFind;
import fibranet.motor.UnionFindIngenuo;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Random;

/**
 * Benchmark de FibraNet (RF-13, CU-07).
 *
 * Experimento 1: Kruskal vs Prim en redes aleatorias conexas de tamaño
 *                creciente, dispersas (E = 4V) y densas (E = V log2 V).
 * Experimento 2: Union-Find optimizado vs ingenuo con la secuencia que
 *                es el peor caso del ingenuo (une en cadena y luego consulta).
 *
 * Método: semilla fija, 2 corridas de calentamiento (la JVM compila el
 * código "caliente" en las primeras corridas) y promedio de 5 corridas.
 * Para comparar con la teoría se divide el tiempo entre la cota (por
 * ejemplo E log2 V): si la razón se mantiene casi constante al crecer N,
 * la medición confirma la complejidad.
 *
 * Ejecutar solo:  java -cp out fibranet.benchmark.Benchmark
 */
public final class Benchmark {

    private static final long SEMILLA = 2026;
    private static final int CALENTAMIENTO = 2;
    private static final int REPETICIONES = 5;

    private static final int[] TAMANOS_MST = {1_000, 2_000, 5_000, 10_000, 20_000, 50_000};
    private static final int[] TAMANOS_UF = {1_000, 2_000, 5_000, 10_000, 20_000};
    private static final int[] TAMANOS_UF_SOLO_OPTIMIZADO = {100_000, 1_000_000};

    private Benchmark() {
    }

    public static void main(String[] args) {
        ejecutar();
    }

    public static void ejecutar() {
        System.out.println("Benchmark de FibraNet (semilla " + SEMILLA + ", promedio de "
                + REPETICIONES + " corridas). Puede tardar alrededor de un minuto...");
        imprimirEntorno();
        try {
            experimentoMst();
            experimentoUnionFind();
        } catch (IOException e) {
            System.out.println("No se pudo guardar el CSV: " + e.getMessage());
        }
    }

    /** Datos de hardware y software para la sección 5 del informe. */
    private static void imprimirEntorno() {
        Tabla tabla = new Tabla("Entorno", "Valor");
        tabla.agregarFila("Sistema operativo", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        tabla.agregarFila("Java", System.getProperty("java.version") + " (" + System.getProperty("java.vm.name") + ")");
        tabla.agregarFila("Núcleos disponibles", String.valueOf(Runtime.getRuntime().availableProcessors()));
        tabla.agregarFila("Memoria máxima JVM", (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " MB");
        tabla.imprimir();
    }

    // ------------------------------------------------------------------
    // Experimento 1: Kruskal vs Prim
    // ------------------------------------------------------------------

    private static void experimentoMst() throws IOException {
        Path ruta = Paths.get("benchmark", "resultados_mst.csv");
        Files.createDirectories(ruta.getParent());
        Tabla tabla = new Tabla("Densidad", "V", "E", "Kruskal (ms)", "Prim (ms)",
                "Kruskal/(E log V)", "Prim/(E log V)", "Memoria (MB)");
        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(ruta, StandardCharsets.UTF_8))) {
            csv.println("densidad,V,E,kruskal_ms,prim_ms,kruskal_ns_por_ElogV,prim_ns_por_ElogV,memoria_mb");
            for (String densidad : new String[] {"dispersa", "densa"}) {
                for (int v : TAMANOS_MST) {
                    int e = densidad.equals("dispersa") ? 4 * v : (int) (v * log2(v));
                    Grafo grafo = generarRedConexa(v, e, new Random(SEMILLA));
                    double memoria = memoriaUsadaMb();

                    double msKruskal = medirMst(grafo, true);
                    double msPrim = medirMst(grafo, false);
                    double cota = e * log2(v);               // E log2 V
                    double razonK = msKruskal * 1e6 / cota;  // nanosegundos por unidad de la cota
                    double razonP = msPrim * 1e6 / cota;

                    tabla.agregarFila(densidad, String.valueOf(v), String.valueOf(e), fmt(msKruskal),
                            fmt(msPrim), fmt(razonK), fmt(razonP), fmt(memoria));
                    csv.printf(Locale.US, "%s,%d,%d,%.4f,%.4f,%.4f,%.4f,%.2f%n",
                            densidad, v, e, msKruskal, msPrim, razonK, razonP, memoria);
                }
            }
            if (csv.checkError()) {                        // PrintWriter no lanza excepciones: se revisa aquí
                throw new IOException("falló la escritura de " + ruta);
            }
        }
        System.out.println("\nExperimento 1: Kruskal vs Prim (tiempo promedio)");
        tabla.imprimir();
        System.out.println("CSV: " + ruta.toAbsolutePath());
    }

    /** Mide el promedio de varias corridas de Kruskal (true) o Prim (false), en ms. */
    private static double medirMst(Grafo grafo, boolean kruskal) {
        long total = 0;
        for (int r = 0; r < CALENTAMIENTO + REPETICIONES; r++) {
            ResultadoMST res = kruskal ? Kruskal.calcular(grafo, false) : Prim.calcular(grafo, 0, false);
            if (r >= CALENTAMIENTO) {
                total += res.nanosegundos;               // las de calentamiento no cuentan
            }
        }
        return total / (double) REPETICIONES / 1e6;
    }

    /**
     * Genera una red conexa aleatoria con v sedes y e tramos:
     *  1. un árbol aleatorio (la sede i se une a una sede anterior) asegura que sea conexa;
     *  2. el resto de tramos une pares al azar.
     * Los costos son enteros entre 1 y 1000.
     */
    public static Grafo generarRedConexa(int v, int e, Random azar) {
        Grafo grafo = new Grafo();
        for (int i = 0; i < v; i++) {
            grafo.agregarSede("S" + i);
        }
        for (int i = 1; i < v; i++) {
            grafo.agregarTramo("S" + i, "S" + azar.nextInt(i), 1 + azar.nextInt(1000));
        }
        for (int k = v - 1; k < e; k++) {
            int a = azar.nextInt(v);
            int b = azar.nextInt(v);
            if (a == b) {
                b = (a + 1) % v;                         // evita lazos
            }
            grafo.agregarTramo("S" + a, "S" + b, 1 + azar.nextInt(1000));
        }
        return grafo;
    }

    // ------------------------------------------------------------------
    // Experimento 2: Union-Find optimizado vs ingenuo
    // ------------------------------------------------------------------

    private static void experimentoUnionFind() throws IOException {
        Path ruta = Paths.get("benchmark", "resultados_unionfind.csv");
        Tabla tabla = new Tabla("n", "Operaciones", "Optimizado (ms)", "Ingenuo (ms)",
                "Optimizado ns/op", "Ingenuo ns/op");
        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(ruta, StandardCharsets.UTF_8))) {
            csv.println("n,operaciones,optimizado_ms,ingenuo_ms,optimizado_ns_por_op,ingenuo_ns_por_op");
            for (int n : TAMANOS_UF) {
                agregarFilaUf(tabla, csv, n, true);
            }
            for (int n : TAMANOS_UF_SOLO_OPTIMIZADO) {
                agregarFilaUf(tabla, csv, n, false);       // el ingenuo tardaría minutos
            }
            if (csv.checkError()) {
                throw new IOException("falló la escritura de " + ruta);
            }
        }
        System.out.println("\nExperimento 2: Union-Find optimizado vs ingenuo (peor caso del ingenuo)");
        tabla.imprimir();
        System.out.println("CSV: " + ruta.toAbsolutePath());
    }

    private static void agregarFilaUf(Tabla tabla, PrintWriter csv, int n, boolean conIngenuo) {
        int operaciones = 2 * n - 1;                       // n-1 uniones + n consultas
        double msOpt = medirUnionFind(n, true);
        double msIng = conIngenuo ? medirUnionFind(n, false) : Double.NaN;
        double nsOpt = msOpt * 1e6 / operaciones;
        double nsIng = msIng * 1e6 / operaciones;
        tabla.agregarFila(String.valueOf(n), String.valueOf(operaciones), fmt(msOpt),
                conIngenuo ? fmt(msIng) : "-", fmt(nsOpt), conIngenuo ? fmt(nsIng) : "-");
        csv.printf(Locale.US, "%d,%d,%.4f,%s,%.4f,%s%n", n, operaciones, msOpt,
                conIngenuo ? String.format(Locale.US, "%.4f", msIng) : "",
                nsOpt, conIngenuo ? String.format(Locale.US, "%.4f", nsIng) : "");
    }

    /**
     * Secuencia de prueba:
     *   union(0,1), union(1,2), ..., union(n-2,n-1)   -> en el ingenuo forma una cadena
     *   find(0) repetido n veces                      -> en el ingenuo recorre la cadena entera
     * Ingenuo: O(n²). Optimizado: O(n α(n)), prácticamente lineal.
     */
    private static double medirUnionFind(int n, boolean optimizado) {
        long total = 0;
        long control = 0;                                  // evita que la JVM elimine el trabajo
        for (int r = 0; r < CALENTAMIENTO + REPETICIONES; r++) {
            long inicio = System.nanoTime();
            if (optimizado) {
                UnionFind uf = new UnionFind(n);
                for (int i = 0; i + 1 < n; i++) {
                    uf.union(i, i + 1);
                }
                for (int i = 0; i < n; i++) {
                    control += uf.find(0);
                }
            } else {
                UnionFindIngenuo uf = new UnionFindIngenuo(n);
                for (int i = 0; i + 1 < n; i++) {
                    uf.union(i, i + 1);
                }
                for (int i = 0; i < n; i++) {
                    control += uf.find(0);
                }
            }
            long fin = System.nanoTime();
            if (r >= CALENTAMIENTO) {
                total += fin - inicio;
            }
        }
        if (control == -1) {
            System.out.println();                          // nunca ocurre
        }
        return total / (double) REPETICIONES / 1e6;
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    /** Memoria del heap en uso, en MB (aproximada: depende del recolector de basura). */
    private static double memoriaUsadaMb() {
        Runtime rt = Runtime.getRuntime();
        System.gc();
        return (rt.totalMemory() - rt.freeMemory()) / (1024.0 * 1024.0);
    }

    private static String fmt(double valor) {
        return String.format(Locale.US, "%.3f", valor);
    }
}
