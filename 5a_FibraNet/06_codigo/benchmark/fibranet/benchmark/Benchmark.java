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
import java.util.ArrayList;
import java.util.List;
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
 * código "caliente" en las primeras corridas) y 5 corridas medidas, de las
 * que se reporta media, desviación estándar, mínimo y máximo. Kruskal y Prim
 * se alternan en cada corrida y cada corrida se guarda aparte
 * (resultados_mst_corridas.csv). La curva teórica es c · cota, con c
 * ajustada por mínimos cuadrados.
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
        Path rutaCorridas = Paths.get("benchmark", "resultados_mst_corridas.csv");
        Files.createDirectories(ruta.getParent());
        List<String[]> filas = new ArrayList<>();          // se guardan para ajustar la curva teórica al final
        List<double[]> medidas = new ArrayList<>();        // {cota, mediaK, mediaP}
        try (PrintWriter corridas = new PrintWriter(Files.newBufferedWriter(rutaCorridas, StandardCharsets.UTF_8))) {
            corridas.println("densidad,V,E,corrida,primero,kruskal_ms,prim_ms");
            for (String densidad : new String[] {"dispersa", "densa"}) {
                for (int v : TAMANOS_MST) {
                    int e = densidad.equals("dispersa") ? 4 * v : (int) (v * log2(v));
                    Grafo grafo = generarRedConexa(v, e, new Random(SEMILLA));
                    double memoria = memoriaUsadaMb();
                    double[][] t = medirMstAlternado(grafo, densidad, v, e, corridas);
                    Estadistica k = new Estadistica(t[0]);
                    Estadistica p = new Estadistica(t[1]);
                    double cota = e * log2(v);               // E log2 V
                    medidas.add(new double[] {cota, k.media, p.media});
                    filas.add(new String[] {densidad, String.valueOf(v), String.valueOf(e),
                            f4(k.media), f4(k.desv), f4(k.min), f4(k.max),
                            f4(p.media), f4(p.desv), f4(p.min), f4(p.max),
                            f4(k.media * 1e6 / cota), f4(p.media * 1e6 / cota), f2(memoria)});
                }
            }
            revisar(corridas, rutaCorridas);
        }
        // Curva teórica de referencia: t = c * E log2 V, con c ajustada por mínimos cuadrados
        // a cada serie (densidad y algoritmo). Si la medición sigue la cota, queda pegada a ella.
        double[] cK = new double[2];
        double[] cP = new double[2];
        for (int d = 0; d < 2; d++) {
            double sk = 0, sp = 0, ff = 0;
            for (int i = d * TAMANOS_MST.length; i < (d + 1) * TAMANOS_MST.length; i++) {
                double[] m = medidas.get(i);
                sk += m[1] * m[0];
                sp += m[2] * m[0];
                ff += m[0] * m[0];
            }
            cK[d] = sk / ff;
            cP[d] = sp / ff;
        }
        Tabla tabla = new Tabla("Densidad", "V", "E", "Kruskal (ms)", "± desv", "Prim (ms)", "± desv",
                "Kruskal teórico", "Prim teórico", "Memoria (MB)");
        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(ruta, StandardCharsets.UTF_8))) {
            csv.println("densidad,V,E,kruskal_ms,kruskal_desv,kruskal_min,kruskal_max,prim_ms,prim_desv,prim_min,prim_max,"
                    + "kruskal_ns_por_ElogV,prim_ns_por_ElogV,memoria_mb,kruskal_teorico_ms,prim_teorico_ms");
            for (int i = 0; i < filas.size(); i++) {
                String[] f = filas.get(i);
                int d = i / TAMANOS_MST.length;
                double cota = medidas.get(i)[0];
                String tk = f4(cK[d] * cota);
                String tp = f4(cP[d] * cota);
                csv.println(String.join(",", f) + "," + tk + "," + tp);
                tabla.agregarFila(f[0], f[1], f[2], f[3], f[4], f[7], f[8], tk, tp, f[13]);
            }
            revisar(csv, ruta);
        }
        System.out.println("\nExperimento 1: Kruskal vs Prim (media y desviación estándar de "
                + REPETICIONES + " corridas; teórico = c · E log2 V ajustado)");
        tabla.imprimir();
        System.out.println("CSV: " + ruta.toAbsolutePath());
        System.out.println("Corridas una por una: " + rutaCorridas.toAbsolutePath());
    }

    /**
     * Mide Kruskal y Prim sobre la misma red. En cada corrida se alterna cuál va primero,
     * para que el orden (caché, recolector de basura) no favorezca siempre al mismo.
     * Devuelve {tiemposKruskal, tiemposPrim} en ms, sin las corridas de calentamiento.
     */
    private static double[][] medirMstAlternado(Grafo grafo, String densidad, int v, int e, PrintWriter corridas) {
        double[] k = new double[REPETICIONES];
        double[] p = new double[REPETICIONES];
        for (int r = 0; r < CALENTAMIENTO + REPETICIONES; r++) {
            boolean kruskalPrimero = (r % 2 == 0);
            double msK;
            double msP;
            if (kruskalPrimero) {
                msK = Kruskal.calcular(grafo, false).nanosegundos / 1e6;
                msP = Prim.calcular(grafo, 0, false).nanosegundos / 1e6;
            } else {
                msP = Prim.calcular(grafo, 0, false).nanosegundos / 1e6;
                msK = Kruskal.calcular(grafo, false).nanosegundos / 1e6;
            }
            if (r >= CALENTAMIENTO) {                      // las de calentamiento no cuentan
                k[r - CALENTAMIENTO] = msK;
                p[r - CALENTAMIENTO] = msP;
                corridas.printf(Locale.US, "%s,%d,%d,%d,%s,%.4f,%.4f%n", densidad, v, e, r - CALENTAMIENTO + 1,
                        kruskalPrimero ? "kruskal" : "prim", msK, msP);
            }
        }
        return new double[][] {k, p};
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
        Tabla tabla = new Tabla("n", "Operaciones", "Optimizado (ms)", "± desv", "Ingenuo (ms)", "± desv",
                "Optimizado ns/op", "Ingenuo ns/op", "Ingenuo teórico (ms)");
        List<Integer> tamanos = new ArrayList<>();
        List<Estadistica[]> stats = new ArrayList<>();
        for (int n : TAMANOS_UF) {
            tamanos.add(n);
            stats.add(new Estadistica[] {new Estadistica(medirUnionFind(n, true)), new Estadistica(medirUnionFind(n, false))});
        }
        for (int n : TAMANOS_UF_SOLO_OPTIMIZADO) {         // el ingenuo tardaría minutos
            tamanos.add(n);
            stats.add(new Estadistica[] {new Estadistica(medirUnionFind(n, true)), null});
        }
        // Curvas teóricas: optimizado c1 · n (α(n) constante), ingenuo c2 · n² (cadena de n eslabones, n consultas)
        double so = 0, oo = 0, si = 0, ii = 0;
        for (int i = 0; i < tamanos.size(); i++) {
            double n = tamanos.get(i);
            so += stats.get(i)[0].media * n;
            oo += n * n;
            if (stats.get(i)[1] != null) {
                si += stats.get(i)[1].media * n * n;
                ii += n * n * n * n;
            }
        }
        double c1 = so / oo;
        double c2 = si / ii;
        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(ruta, StandardCharsets.UTF_8))) {
            csv.println("n,operaciones,optimizado_ms,ingenuo_ms,optimizado_ns_por_op,ingenuo_ns_por_op,"
                    + "optimizado_desv,optimizado_min,optimizado_max,ingenuo_desv,ingenuo_min,ingenuo_max,"
                    + "optimizado_teorico_ms,ingenuo_teorico_ms");
            for (int i = 0; i < tamanos.size(); i++) {
                int n = tamanos.get(i);
                int operaciones = 2 * n - 1;               // n-1 uniones + n consultas
                Estadistica o = stats.get(i)[0];
                Estadistica g = stats.get(i)[1];
                String teoIng = f4(c2 * (double) n * n);
                csv.println(String.join(",", String.valueOf(n), String.valueOf(operaciones), f4(o.media),
                        g == null ? "" : f4(g.media), f4(o.media * 1e6 / operaciones),
                        g == null ? "" : f4(g.media * 1e6 / operaciones),
                        f4(o.desv), f4(o.min), f4(o.max),
                        g == null ? "" : f4(g.desv), g == null ? "" : f4(g.min), g == null ? "" : f4(g.max),
                        f4(c1 * n), teoIng));
                tabla.agregarFila(String.valueOf(n), String.valueOf(operaciones), fmt(o.media), fmt(o.desv),
                        g == null ? "-" : fmt(g.media), g == null ? "-" : fmt(g.desv),
                        fmt(o.media * 1e6 / operaciones), g == null ? "-" : fmt(g.media * 1e6 / operaciones), teoIng);
            }
            revisar(csv, ruta);
        }
        System.out.println("\nExperimento 2: Union-Find optimizado vs ingenuo (peor caso del ingenuo; teórico = c · n²)");
        tabla.imprimir();
        System.out.println("CSV: " + ruta.toAbsolutePath());
    }

    /**
     * Secuencia de prueba:
     *   union(0,1), union(1,2), ..., union(n-2,n-1)   -> en el ingenuo forma una cadena
     *   find(0) repetido n veces                      -> en el ingenuo recorre la cadena entera
     * Ingenuo: O(n²). Optimizado: O(n α(n)), prácticamente lineal.
     * Devuelve el tiempo de cada corrida en ms, sin las de calentamiento.
     */
    private static double[] medirUnionFind(int n, boolean optimizado) {
        double[] tiempos = new double[REPETICIONES];
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
                tiempos[r - CALENTAMIENTO] = (fin - inicio) / 1e6;
            }
        }
        if (control == -1) {
            System.out.println();                          // nunca ocurre
        }
        return tiempos;
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

    private static String f4(double valor) {
        return String.format(Locale.US, "%.4f", valor);
    }

    private static String f2(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }

    private static void revisar(PrintWriter csv, Path ruta) throws IOException {
        if (csv.checkError()) {                            // PrintWriter no lanza excepciones: se revisa aquí
            throw new IOException("falló la escritura de " + ruta);
        }
    }

    /** Media, desviación estándar muestral, mínimo y máximo de las corridas. */
    static final class Estadistica {
        final double media;
        final double desv;
        final double min;
        final double max;

        Estadistica(double[] x) {
            double suma = 0;
            double mn = Double.MAX_VALUE;
            double mx = -Double.MAX_VALUE;
            for (double v : x) {
                suma += v;
                mn = Math.min(mn, v);
                mx = Math.max(mx, v);
            }
            media = suma / x.length;
            double cuadrados = 0;
            for (double v : x) {
                cuadrados += (v - media) * (v - media);
            }
            desv = x.length > 1 ? Math.sqrt(cuadrados / (x.length - 1)) : 0;
            min = mn;
            max = mx;
        }
    }
}
