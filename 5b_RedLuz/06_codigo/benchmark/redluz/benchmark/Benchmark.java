package redluz.benchmark;

import redluz.io.Tabla;
import redluz.modelo.RedDinamica;
import redluz.modelo.RedElectrica;
import redluz.modelo.RedIngenua;
import redluz.modelo.Tipos;

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
import java.util.function.Supplier;

/**
 * Benchmark de RedLuz (RF-12, CU-07): Link-Cut Tree vs BFS.
 *
 * Red de prueba: una subestación (S0) y N-1 nodos. Cada nodo nuevo se une
 * a uno de los 5 anteriores, así la red es un árbol PROFUNDO (alimentadores
 * largos, como en la realidad), que es el caso difícil para BFS.
 *
 * Operaciones: M = N, generadas con semilla fija y aplicadas EN EL MISMO
 * ORDEN a las dos implementaciones: 40 % LUZ, 30 % CORTAR un tramo activo,
 * 30 % CONECTAR un par al azar (si cerraría un anillo, se rechaza).
 *
 * Teoría: Link-Cut Tree O(M log N); BFS O(M · N).
 * Se divide el tiempo entre cada cota: la razón que se mantiene constante
 * indica qué cota describe mejor la medición.
 *
 * Ejecutar solo:  java -cp out redluz.benchmark.Benchmark
 */
public final class Benchmark {

    private static final long SEMILLA = 2026;
    private static final int CALENTAMIENTO = 1;
    private static final int REPETICIONES = 3;

    private static final int[] TAMANOS = {1_000, 2_000, 5_000, 10_000, 20_000, 50_000, 100_000};
    private static final int MAXIMO_N_INGENUO = 5_000;    // más allá BFS tarda minutos

    private Benchmark() {
    }

    public static void main(String[] args) {
        ejecutar();
    }

    public static void ejecutar() {
        System.out.println("Benchmark de RedLuz (semilla " + SEMILLA + ", promedio de "
                + REPETICIONES + " corridas). Puede tardar alrededor de un minuto...");
        imprimirEntorno();
        Path ruta = Paths.get("benchmark", "resultados_lct.csv");
        Tabla tabla = new Tabla("N", "M", "LCT (ms)", "BFS (ms)", "LCT ns/(M log N)", "BFS ns/(M·N)", "BFS/LCT");
        try {
            Files.createDirectories(ruta.getParent());
            try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(ruta, StandardCharsets.UTF_8))) {
                csv.println("N,M,lct_ms,bfs_ms,lct_ns_por_MlogN,bfs_ns_por_MN,veces_mas_rapido");
                for (int n : TAMANOS) {
                    medirTamano(n, tabla, csv);
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo guardar el CSV: " + e.getMessage());
        }
        System.out.println("\nLink-Cut Tree vs BFS (tiempo promedio de M operaciones)");
        tabla.imprimir();
        System.out.println("CSV: " + ruta.toAbsolutePath());
    }

    private static void medirTamano(int n, Tabla tabla, PrintWriter csv) {
        int m = n;
        List<String[]> operaciones = generarOperaciones(n, m, new Random(SEMILLA));
        double msLct = medir(RedElectrica::new, n, operaciones);
        boolean conIngenuo = n <= MAXIMO_N_INGENUO;
        double msBfs = conIngenuo ? medir(RedIngenua::new, n, operaciones) : Double.NaN;

        double razonLct = msLct * 1e6 / (m * log2(n));
        double razonBfs = msBfs * 1e6 / ((double) m * n);
        double veces = msBfs / msLct;
        tabla.agregarFila(String.valueOf(n), String.valueOf(m), fmt(msLct),
                conIngenuo ? fmt(msBfs) : "-", fmt(razonLct),
                conIngenuo ? fmt(razonBfs) : "-", conIngenuo ? fmt(veces) : "-");
        csv.printf(Locale.US, "%d,%d,%.4f,%s,%.4f,%s,%s%n", n, m, msLct,
                conIngenuo ? String.format(Locale.US, "%.4f", msBfs) : "", razonLct,
                conIngenuo ? String.format(Locale.US, "%.6f", razonBfs) : "",
                conIngenuo ? String.format(Locale.US, "%.2f", veces) : "");
    }

    /**
     * Construye la red inicial (sin medir) y mide solo las M operaciones.
     * Devuelve el promedio en ms.
     */
    private static double medir(Supplier<RedDinamica> fabrica, int n, List<String[]> operaciones) {
        long total = 0;
        long control = 0;
        for (int r = 0; r < CALENTAMIENTO + REPETICIONES; r++) {
            RedDinamica red = fabrica.get();
            construirRedInicial(red, n, new Random(SEMILLA));
            long inicio = System.nanoTime();
            for (String[] op : operaciones) {
                switch (op[0]) {
                    case "LUZ":
                        control += red.tieneLuz(op[1]) ? 1 : 0;
                        break;
                    case "CORTAR":
                        control += red.cortar(op[1], op[2]).ordinal();
                        break;
                    default:
                        control += red.conectar(op[1], op[2], 0.5).ordinal();
                }
            }
            long fin = System.nanoTime();
            if (r >= CALENTAMIENTO) {
                total += fin - inicio;
            }
        }
        if (control == -1) {
            System.out.println();                       // nunca ocurre: evita que la JVM elimine el trabajo
        }
        return total / (double) REPETICIONES / 1e6;
    }

    /** Subestación N0 y N-1 nodos; el nodo i se une a uno de los 5 anteriores (árbol profundo). */
    public static void construirRedInicial(RedDinamica red, int n, Random azar) {
        red.agregarNodo("N0", Tipos.SUBESTACION);
        for (int i = 1; i < n; i++) {
            red.agregarNodo("N" + i, (i % 3 == 0) ? Tipos.POSTE : Tipos.CLIENTE);
            int padre = Math.max(0, i - 1 - azar.nextInt(5));
            red.conectar("N" + padre, "N" + i, 0.1 + azar.nextInt(10) / 10.0);
        }
    }

    /**
     * Genera la secuencia de operaciones. Lleva una lista de tramos activos
     * para que CORTAR elija un tramo que existe. CONECTAR se anota como
     * activo solo si la red real lo acepta, por eso se simula sobre una
     * RedElectrica (rápida) mientras se genera.
     */
    public static List<String[]> generarOperaciones(int n, int m, Random azar) {
        RedElectrica simulada = new RedElectrica();
        Random azarRed = new Random(SEMILLA);
        construirRedInicial(simulada, n, azarRed);
        List<String[]> activos = new ArrayList<>();
        for (String[] t : simulada.tramos()) {
            activos.add(new String[] {t[0], t[1]});
        }

        List<String[]> operaciones = new ArrayList<>();
        for (int k = 0; k < m; k++) {
            int dado = azar.nextInt(10);
            if (dado < 4) {
                operaciones.add(new String[] {"LUZ", "N" + azar.nextInt(n)});
            } else if (dado < 7 && !activos.isEmpty()) {
                int i = azar.nextInt(activos.size());
                String[] t = activos.get(i);
                activos.set(i, activos.get(activos.size() - 1));   // quitar en O(1): se cambia por el último
                activos.remove(activos.size() - 1);
                simulada.cortar(t[0], t[1]);
                operaciones.add(new String[] {"CORTAR", t[0], t[1]});
            } else {
                String a = "N" + azar.nextInt(n);
                String b = "N" + azar.nextInt(n);
                if (simulada.conectar(a, b, 0.5) == RedDinamica.Resultado.OK) {
                    activos.add(new String[] {a, b});
                }
                operaciones.add(new String[] {"CONECTAR", a, b});
            }
        }
        return operaciones;
    }

    private static void imprimirEntorno() {
        Tabla tabla = new Tabla("Entorno", "Valor");
        tabla.agregarFila("Sistema operativo", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        tabla.agregarFila("Java", System.getProperty("java.version") + " (" + System.getProperty("java.vm.name") + ")");
        tabla.agregarFila("Núcleos disponibles", String.valueOf(Runtime.getRuntime().availableProcessors()));
        tabla.agregarFila("Memoria máxima JVM", (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " MB");
        tabla.imprimir();
    }

    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    private static String fmt(double valor) {
        return String.format(Locale.US, "%.3f", valor);
    }
}
