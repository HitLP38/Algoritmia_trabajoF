package fibranet.pruebas;

import fibranet.benchmark.Benchmark;
import fibranet.io.LectorCoordenadas;
import fibranet.io.LectorCsv;
import fibranet.modelo.Arista;
import fibranet.modelo.Geo;
import fibranet.modelo.Grafo;
import fibranet.motor.Kruskal;
import fibranet.motor.Prim;
import fibranet.motor.ResultadoMST;
import fibranet.motor.UnionFind;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pruebas automáticas de FibraNet (RF-14), sin librerías externas.
 *
 * Cada prueba es un método que llama a verificar(condición, mensaje).
 * Al final se imprime cuántas verificaciones pasaron y cuáles fallaron.
 *
 * Ejecutar:  java -cp out fibranet.pruebas.Pruebas
 */
public final class Pruebas {

    private static int pasaron = 0;
    private static final List<String> fallas = new ArrayList<>();

    private Pruebas() {
    }

    public static void main(String[] args) {
        unionFindBasico();
        unionFindCuentaGrupos();
        kruskalEjemploResueltoAMano();
        primEjemploResueltoAMano();
        redNoConexaDaUnBosque();
        validacionDeFilas();
        csvDeEjemplo();
        kruskalYPrimCoincidenEnRedesAleatorias();
        planEsArbolSinCiclos();
        haversineDistanciasConocidas();
        redDorsalNacional();

        System.out.println();
        System.out.printf("Verificaciones: %d pasaron, %d fallaron.%n", pasaron, fallas.size());
        for (String falla : fallas) {
            System.out.println("  FALLA: " + falla);
        }
        if (!fallas.isEmpty()) {
            System.exit(1);                                // código de salida distinto de 0 si algo falló
        }
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (condicion) {
            pasaron++;
        } else {
            fallas.add(mensaje);
        }
    }

    private static void titulo(String nombre) {
        System.out.println("- " + nombre);
    }

    // ------------------------------------------------------------------

    private static void unionFindBasico() {
        titulo("Union-Find: find, union y conectados");
        UnionFind uf = new UnionFind(5);
        verificar(!uf.conectados(0, 1), "al inicio 0 y 1 están separados");
        verificar(uf.union(0, 1), "union(0,1) une dos grupos distintos");
        verificar(uf.union(1, 2), "union(1,2) une dos grupos distintos");
        verificar(uf.conectados(0, 2), "0 y 2 quedan conectados a través de 1");
        verificar(!uf.union(0, 2), "union(0,2) devuelve false: ya estaban juntos (sería un ciclo)");
        verificar(!uf.conectados(0, 3), "3 sigue separado");
    }

    private static void unionFindCuentaGrupos() {
        titulo("Union-Find: número de grupos");
        UnionFind uf = new UnionFind(6);
        verificar(uf.numeroGrupos() == 6, "6 grupos al inicio");
        uf.union(0, 1);
        uf.union(2, 3);
        uf.union(1, 0);                                     // repetida: no cambia nada
        verificar(uf.numeroGrupos() == 4, "quedan 4 grupos: {0,1} {2,3} {4} {5}");
    }

    /**
     * Red de ejemplo de la exposición, resuelta a mano:
     *
     *        4        2
     *    A ----- B ----- C
     *    |     / |       |
     *  1 |  3/   | 5     | 6
     *    | /     |       |
     *    D ----- E ----- F
     *        7       8
     *
     * MST: A-D(1) + B-C(2) + B-D(3) + B-E(5) + C-F(6) = 17
     */
    private static Grafo redDeEjemplo() {
        Grafo g = new Grafo();
        g.agregarTramo("A", "B", 4);
        g.agregarTramo("B", "C", 2);
        g.agregarTramo("A", "D", 1);
        g.agregarTramo("B", "D", 3);
        g.agregarTramo("B", "E", 5);
        g.agregarTramo("C", "F", 6);
        g.agregarTramo("D", "E", 7);
        g.agregarTramo("E", "F", 8);
        return g;
    }

    private static void kruskalEjemploResueltoAMano() {
        titulo("Kruskal: ejemplo resuelto a mano");
        ResultadoMST r = Kruskal.calcular(redDeEjemplo(), true);
        verificar(r.costoTotal == 17, "costo mínimo 17 (obtenido " + r.costoTotal + ")");
        verificar(r.tramosElegidos.size() == 5, "6 sedes -> 5 tramos");
        verificar(r.esConexo(), "la red es conexa");
        // A-B (4) se evalúa después de A-D, B-C y B-D, y se rechaza porque A y B ya están unidos.
        boolean rechazoAB = false;
        for (String[] paso : r.pasos) {
            if (paso[1].equals("A - B") && paso[3].startsWith("RECHAZADO")) {
                rechazoAB = true;
            }
        }
        verificar(rechazoAB, "Kruskal rechaza A-B por formar ciclo");
    }

    private static void primEjemploResueltoAMano() {
        titulo("Prim: ejemplo resuelto a mano desde A y desde F");
        Grafo g = redDeEjemplo();
        verificar(Prim.calcular(g, g.indice("A"), false).costoTotal == 17, "Prim desde A da 17");
        verificar(Prim.calcular(g, g.indice("F"), false).costoTotal == 17, "Prim desde F también da 17");
    }

    private static void redNoConexaDaUnBosque() {
        titulo("Red no conexa: bosque de expansión mínima");
        Grafo g = new Grafo();
        g.agregarTramo("A", "B", 1);
        g.agregarTramo("B", "C", 2);
        g.agregarTramo("X", "Y", 5);                       // isla separada
        ResultadoMST k = Kruskal.calcular(g, false);
        ResultadoMST p = Prim.calcular(g, 0, false);
        verificar(k.grupos == 2, "Kruskal detecta 2 grupos");
        verificar(p.grupos == 2, "Prim detecta 2 grupos");
        verificar(k.costoTotal == 8 && p.costoTotal == 8, "ambos suman 8");
        UnionFind grupos = Kruskal.gruposDelPlan(g, k);
        verificar(grupos.conectados(g.indice("A"), g.indice("C")), "A y C quedan conectadas");
        verificar(!grupos.conectados(g.indice("A"), g.indice("X")), "A y X no quedan conectadas");
    }

    private static void validacionDeFilas() {
        titulo("Validación de filas (RF-03)");
        Grafo g = new Grafo();
        String sep = LectorCsv.SEPARADOR_CSV;
        verificar(LectorCsv.agregarTramo("Lima,Callao,12", sep, g) == null, "fila correcta se acepta");
        verificar(LectorCsv.agregarTramo("San Isidro,Lima,3.5", sep, g) == null, "nombre con espacio y decimal se acepta");
        verificar(LectorCsv.agregarTramo("Lima,Lima,5", sep, g) != null, "lazo se rechaza");
        verificar(LectorCsv.agregarTramo("Lima,Ate,-3", sep, g) != null, "costo negativo se rechaza");
        verificar(LectorCsv.agregarTramo("Lima,Ate,abc", sep, g) != null, "costo no numérico se rechaza");
        verificar(LectorCsv.agregarTramo("Lima,Ate", sep, g) != null, "fila incompleta se rechaza");
        verificar(g.numeroTramos() == 2, "solo quedan los 2 tramos válidos");
    }

    private static void csvDeEjemplo() {
        titulo("Lectura del CSV de ejemplo");
        try {
            Grafo g = LectorCsv.leer(Paths.get("datos", "red_ejemplo.csv"));
            verificar(g.numeroSedes() > 0, "el CSV de ejemplo tiene sedes");
            ResultadoMST k = Kruskal.calcular(g, false);
            ResultadoMST p = Prim.calcular(g, 0, false);
            verificar(Math.abs(k.costoTotal - p.costoTotal) < 1e-9, "Kruskal y Prim coinciden en el CSV de ejemplo");
        } catch (Exception e) {
            verificar(false, "no se pudo leer datos/red_ejemplo.csv (ejecute desde 06_codigo): " + e.getMessage());
        }
    }

    private static void kruskalYPrimCoincidenEnRedesAleatorias() {
        titulo("Kruskal y Prim coinciden en 200 redes aleatorias");
        Random azar = new Random(7);
        boolean todas = true;
        for (int i = 0; i < 200; i++) {
            int v = 2 + azar.nextInt(60);
            int e = v - 1 + azar.nextInt(3 * v);
            Grafo g = Benchmark.generarRedConexa(v, e, azar);
            double k = Kruskal.calcular(g, false).costoTotal;
            double p = Prim.calcular(g, azar.nextInt(v), false).costoTotal;
            if (Math.abs(k - p) > 1e-9) {
                todas = false;
            }
        }
        verificar(todas, "mismo costo total en todas las redes");
    }

    /** Un plan válido tiene V-1 tramos y ninguno cierra un ciclo. */
    private static void planEsArbolSinCiclos() {
        titulo("El plan de Prim es un árbol (V-1 tramos, sin ciclos)");
        Grafo g = Benchmark.generarRedConexa(500, 3000, new Random(11));
        ResultadoMST p = Prim.calcular(g, 0, false);
        verificar(p.tramosElegidos.size() == g.numeroSedes() - 1, "tiene V-1 tramos");
        UnionFind uf = new UnionFind(g.numeroSedes());
        boolean sinCiclos = true;
        for (Arista a : p.tramosElegidos) {
            if (!uf.union(a.origen, a.destino)) {
                sinCiclos = false;
            }
        }
        verificar(sinCiclos, "ningún tramo del plan cierra un ciclo");
    }

    // ------------------------------------------------------------------
    // Caso real: Red Dorsal Nacional
    // ------------------------------------------------------------------

    private static void haversineDistanciasConocidas() {
        titulo("Haversine: distancias conocidas");
        verificar(Geo.haversineKm(-12.0464, -77.0428, -12.0464, -77.0428) == 0.0, "distancia de un punto a sí mismo = 0");
        double limaCallao = Geo.haversineKm(-12.0464, -77.0428, -12.0566, -77.1181);
        verificar(limaCallao > 7 && limaCallao < 10, "Lima - Callao entre 7 y 10 km (obtenido " + limaCallao + ")");
        double limaArequipa = Geo.haversineKm(-12.0464, -77.0428, -16.3989, -71.5350);
        verificar(limaArequipa > 740 && limaArequipa < 790, "Lima - Arequipa entre 740 y 790 km (obtenido " + limaArequipa + ")");
        double ida = Geo.haversineKm(-3.7491, -73.2538, -18.0146, -70.2536);
        double vuelta = Geo.haversineKm(-18.0146, -70.2536, -3.7491, -73.2538);
        verificar(Math.abs(ida - vuelta) < 1e-9, "la distancia es simétrica");
    }

    private static void redDorsalNacional() {
        titulo("Red Dorsal Nacional: 25 capitales con coordenadas");
        try {
            List<String> errores = new ArrayList<>();
            Grafo g = LectorCoordenadas.leer(Paths.get("datos", "capitales_peru.csv"), 4, errores);
            verificar(errores.isEmpty(), "el CSV de capitales no tiene filas inválidas");
            verificar(g.numeroSedes() == 25, "25 capitales (24 departamentos + Callao)");
            ResultadoMST k = Kruskal.calcular(g, false);
            ResultadoMST p = Prim.calcular(g, g.indice("Lima"), false);
            verificar(k.esConexo(), "con k = 4 la red queda conectada");
            verificar(k.tramosElegidos.size() == 24, "25 capitales -> 24 tramos");
            verificar(Math.abs(k.costoTotal - p.costoTotal) < 1e-6, "Kruskal y Prim dan los mismos km");
            verificar(k.costoTotal < g.costoTotalDeTodosLosTramos() / 2, "el plan usa menos de la mitad de los km candidatos");
        } catch (IOException e) {
            verificar(false, "no se pudo leer datos/capitales_peru.csv (ejecute desde 06_codigo)");
        }
    }
}
