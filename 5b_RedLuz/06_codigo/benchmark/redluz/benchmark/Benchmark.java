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
 * Perfiles (se mide cada uno con las dos implementaciones):
 *   A · profundo + mixto:    cada nodo nuevo se une a uno de los 5 anteriores
 *                            (alimentadores largos); 40 % LUZ, 30 % CORTAR, 30 % CONECTAR.
 *   B · aleatorio + mixto:   cada nodo se une a cualquier nodo anterior (árbol bajo,
 *                            profundidad ~ log N); mismas proporciones que A.
 *   C · profundo + consultas: red de A, pero 80 % LUZ, 10 % CORTAR, 10 % CONECTAR.
 * B cambia solo la forma del árbol y C solo la mezcla de eventos, para ver de qué
 * depende la ventaja del Link-Cut Tree.
 *
 * Operaciones: M = N, generadas con semilla fija y aplicadas EN EL MISMO
 * ORDEN a las dos implementaciones (CONECTAR que cerraría un anillo se rechaza).
 * Método: 1 calentamiento y 5 corridas; se reporta media, desviación estándar,
 * mínimo y máximo. La curva teórica es c · cota (M log2 N para LCT, M · N para BFS),
 * con c ajustada por mínimos cuadrados en cada perfil.
 *
 * Teoría: Link-Cut Tree O(M log N) amortizado; BFS O(M · N) en el peor caso.
 *
 * Salida: resultados_lct.csv (perfil A, el de las figuras del informe) y
 *         resultados_lct_perfiles.csv (los tres perfiles).
 *
 * Ejecutar solo:  java -cp out redluz.benchmark.Benchmark
 */
public final class Benchmark {

    private static final long SEMILLA = 2026;
    private static final int CALENTAMIENTO = 1;
    private static final int REPETICIONES = 5;

    private static final int[] TAMANOS = {1_000, 2_000, 5_000, 10_000, 20_000, 50_000, 100_000};
    private static final int MAXIMO_N_INGENUO = 5_000;    // perfil A: más allá BFS tarda minutos
    private static final int MAXIMO_N_INGENUO_OTROS = 2_000;   // perfiles B y C: basta para comparar la tendencia

    /** Perfil de prueba: forma del árbol y proporción de eventos (en décimos). */
    private static final class Perfil {
        final String nombre;
        final String descripcion;
        final boolean profundo;
        final int luz;        // de 10
        final int cortar;     // de 10 (el resto es CONECTAR)

        Perfil(String nombre, String descripcion, boolean profundo, int luz, int cortar) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.profundo = profundo;
            this.luz = luz;
            this.cortar = cortar;
        }
    }

    private static final Perfil[] PERFILES = {
        new Perfil("A", "profundo + mixto (40/30/30)", true, 4, 3),
        new Perfil("B", "aleatorio + mixto (40/30/30)", false, 4, 3),
        new Perfil("C", "profundo + consultas (80/10/10)", true, 8, 1),
    };

    private Benchmark() {
    }

    public static void main(String[] args) {
        ejecutar();
    }

    public static void ejecutar() {
        System.out.println("Benchmark de RedLuz (semilla " + SEMILLA + ", " + REPETICIONES
                + " corridas por medición, 3 perfiles). Tarda 1 a 3 minutos; cada línea muestra el avance.");
        imprimirEntorno();
        // calentamiento global: la primera medición no paga la compilación JIT del Link-Cut Tree
        medir(RedElectrica::new, 5_000, generarOperaciones(5_000, 5_000, new Random(SEMILLA)), true);
        Path ruta = Paths.get("benchmark", "resultados_lct.csv");
        Path rutaPerfiles = Paths.get("benchmark", "resultados_lct_perfiles.csv");
        String columnas = "N,M,lct_ms,bfs_ms,lct_ns_por_MlogN,bfs_ns_por_MN,veces_mas_rapido,"
                + "lct_desv,lct_min,lct_max,bfs_desv,bfs_min,bfs_max,lct_teorico_ms,bfs_teorico_ms";
        try {
            Files.createDirectories(ruta.getParent());
            try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(ruta, StandardCharsets.UTF_8));
                 PrintWriter todos = new PrintWriter(Files.newBufferedWriter(rutaPerfiles, StandardCharsets.UTF_8))) {
                csv.println(columnas);
                todos.println("perfil,descripcion," + columnas);
                for (Perfil perfil : PERFILES) {
                    List<String> filas = medirPerfil(perfil);
                    for (String fila : filas) {
                        todos.println(perfil.nombre + "," + perfil.descripcion + "," + fila);
                        if (perfil == PERFILES[0]) {
                            csv.println(fila);
                        }
                    }
                }
                if (csv.checkError() || todos.checkError()) {   // PrintWriter no lanza excepciones
                    throw new IOException("falló la escritura de los CSV del benchmark");
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo guardar el CSV: " + e.getMessage());
        }
        System.out.println("CSV (perfil A): " + ruta.toAbsolutePath());
        System.out.println("CSV (3 perfiles): " + rutaPerfiles.toAbsolutePath());
    }

    /** Mide todos los tamaños de un perfil, ajusta las curvas teóricas e imprime su tabla. */
    private static List<String> medirPerfil(Perfil perfil) {
        List<Estadistica> lct = new ArrayList<>();
        List<Estadistica> bfs = new ArrayList<>();
        for (int n : TAMANOS) {
            List<String[]> operaciones = generarOperaciones(n, n, new Random(SEMILLA), perfil);
            lct.add(new Estadistica(medir(RedElectrica::new, n, operaciones, perfil.profundo)));
            System.out.printf(Locale.US, "  perfil %s, N = %d: LCT %.1f ms", perfil.nombre, n, lct.get(lct.size() - 1).media);
            int maximo = (perfil == PERFILES[0]) ? MAXIMO_N_INGENUO : MAXIMO_N_INGENUO_OTROS;
            if (n <= maximo) {
                System.out.print(", midiendo BFS...");
                System.out.flush();
                bfs.add(new Estadistica(medir(RedIngenua::new, n, operaciones, perfil.profundo)));
                System.out.printf(Locale.US, " %.1f ms", bfs.get(bfs.size() - 1).media);
            } else {
                bfs.add(null);
            }
            System.out.println();                          // progreso: se ve que el programa avanza
        }
        double sl = 0, ll = 0, sb = 0, bb = 0;              // mínimos cuadrados: c = Σ t·f / Σ f²
        for (int i = 0; i < TAMANOS.length; i++) {
            double n = TAMANOS[i];
            double fl = n * log2(n);
            double fb = n * n;
            sl += lct.get(i).media * fl;
            ll += fl * fl;
            if (bfs.get(i) != null) {
                sb += bfs.get(i).media * fb;
                bb += fb * fb;
            }
        }
        double cl = sl / ll;
        double cb = sb / bb;

        Tabla tabla = new Tabla("N", "LCT (ms)", "± desv", "LCT teórico", "BFS (ms)", "± desv", "BFS teórico",
                "LCT ns/(M log N)", "BFS ns/(M·N)", "BFS/LCT");
        List<String> filas = new ArrayList<>();
        for (int i = 0; i < TAMANOS.length; i++) {
            int n = TAMANOS[i];
            int m = n;
            Estadistica l = lct.get(i);
            Estadistica b = bfs.get(i);
            double razonLct = l.media * 1e6 / (m * log2(n));
            String teoL = f4(cl * m * log2(n));
            String teoB = f4(cb * (double) m * n);
            filas.add(String.join(",", String.valueOf(n), String.valueOf(m), f4(l.media),
                    b == null ? "" : f4(b.media), f4(razonLct),
                    b == null ? "" : String.format(Locale.US, "%.6f", b.media * 1e6 / ((double) m * n)),
                    b == null ? "" : String.format(Locale.US, "%.2f", b.media / l.media),
                    f4(l.desv), f4(l.min), f4(l.max),
                    b == null ? "" : f4(b.desv), b == null ? "" : f4(b.min), b == null ? "" : f4(b.max),
                    teoL, teoB));
            tabla.agregarFila(String.valueOf(n), fmt(l.media), fmt(l.desv), teoL,
                    b == null ? "-" : fmt(b.media), b == null ? "-" : fmt(b.desv), teoB, fmt(razonLct),
                    b == null ? "-" : String.format(Locale.US, "%.4f", b.media * 1e6 / ((double) m * n)),
                    b == null ? "-" : fmt(b.media / l.media));
        }
        System.out.println("\nPerfil " + perfil.nombre + " · " + perfil.descripcion
                + " (media y desviación de " + REPETICIONES + " corridas de M = N operaciones)");
        tabla.imprimir();
        return filas;
    }

    /**
     * Construye la red inicial (sin medir) y mide solo las M operaciones.
     * Devuelve el tiempo de cada corrida en ms, sin la de calentamiento.
     */
    private static double[] medir(Supplier<RedDinamica> fabrica, int n, List<String[]> operaciones, boolean profundo) {
        double[] tiempos = new double[REPETICIONES];
        long control = 0;
        for (int r = 0; r < CALENTAMIENTO + REPETICIONES; r++) {
            RedDinamica red = fabrica.get();
            construirRedInicial(red, n, new Random(SEMILLA), profundo);
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
                tiempos[r - CALENTAMIENTO] = (fin - inicio) / 1e6;
            }
        }
        if (control == -1) {
            System.out.println();                       // nunca ocurre: evita que la JVM elimine el trabajo
        }
        return tiempos;
    }

    /** Red del perfil A: el nodo i se une a uno de los 5 anteriores (árbol profundo). */
    public static void construirRedInicial(RedDinamica red, int n, Random azar) {
        construirRedInicial(red, n, azar, true);
    }

    /**
     * Subestación N0 y N-1 nodos. Profundo: el nodo i se une a uno de los 5 anteriores.
     * Aleatorio: el nodo i se une a cualquier nodo anterior (profundidad esperada ~ ln N).
     */
    public static void construirRedInicial(RedDinamica red, int n, Random azar, boolean profundo) {
        red.agregarNodo("N0", Tipos.SUBESTACION);
        for (int i = 1; i < n; i++) {
            red.agregarNodo("N" + i, (i % 3 == 0) ? Tipos.POSTE : Tipos.CLIENTE);
            int padre = profundo ? Math.max(0, i - 1 - azar.nextInt(5)) : azar.nextInt(i);
            red.conectar("N" + padre, "N" + i, 0.1 + azar.nextInt(10) / 10.0);
        }
    }

    /** Operaciones del perfil A (se conserva para las pruebas). */
    public static List<String[]> generarOperaciones(int n, int m, Random azar) {
        return generarOperaciones(n, m, azar, PERFILES[0]);
    }

    /**
     * Genera la secuencia de operaciones. Lleva una lista de tramos activos
     * para que CORTAR elija un tramo que existe. CONECTAR se anota como
     * activo solo si la red real lo acepta, por eso se simula sobre una
     * RedElectrica (rápida) mientras se genera.
     */
    private static List<String[]> generarOperaciones(int n, int m, Random azar, Perfil perfil) {
        RedElectrica simulada = new RedElectrica();
        construirRedInicial(simulada, n, new Random(SEMILLA), perfil.profundo);
        List<String[]> activos = new ArrayList<>();
        for (String[] t : simulada.tramos()) {
            activos.add(new String[] {t[0], t[1]});
        }

        List<String[]> operaciones = new ArrayList<>();
        for (int k = 0; k < m; k++) {
            int dado = azar.nextInt(10);
            if (dado < perfil.luz) {
                operaciones.add(new String[] {"LUZ", "N" + azar.nextInt(n)});
            } else if (dado < perfil.luz + perfil.cortar && !activos.isEmpty()) {
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
    private static String f4(double valor) {
        return String.format(Locale.US, "%.4f", valor);
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
