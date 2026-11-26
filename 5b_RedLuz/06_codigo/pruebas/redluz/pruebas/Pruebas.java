package redluz.pruebas;

import redluz.benchmark.Benchmark;
import redluz.io.LectorCsv;
import redluz.modelo.RedDinamica;
import redluz.modelo.RedDinamica.Resultado;
import redluz.modelo.RedElectrica;
import redluz.modelo.RedIngenua;
import redluz.modelo.Ruta;
import redluz.modelo.Tipos;
import redluz.motor.LinkCutTree;
import redluz.motor.Nodo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Pruebas automáticas de RedLuz (RF-13), sin librerías externas.
 *
 * La prueba más fuerte aplica miles de operaciones al azar a la vez sobre
 * el Link-Cut Tree y sobre la versión ingenua (BFS) y exige que respondan
 * exactamente lo mismo.
 *
 * Ejecutar:  java -cp out redluz.pruebas.Pruebas
 */
public final class Pruebas {

    private static int pasaron = 0;
    private static final List<String> fallas = new ArrayList<>();

    private Pruebas() {
    }

    public static void main(String[] args) {
        motorLinkYCut();
        motorRechazaCiclos();
        motorSumaDeCamino();
        redEjemploResueltoAMano();
        validaciones();
        csvDeEjemplo();
        lctCoincideConBfsEnOperacionesAleatorias();
        arbolProfundoSinDesbordar();
        coordenadasYCorteProgramado();

        System.out.println();
        System.out.printf("Verificaciones: %d pasaron, %d fallaron.%n", pasaron, fallas.size());
        for (String falla : fallas) {
            System.out.println("  FALLA: " + falla);
        }
        if (!fallas.isEmpty()) {
            System.exit(1);
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
    // Motor
    // ------------------------------------------------------------------

    private static Nodo nodo(String nombre) {
        return new Nodo(nombre, 0, 0, -1);
    }

    private static void motorLinkYCut() {
        titulo("Motor: link, cut y connected");
        LinkCutTree lct = new LinkCutTree();
        Nodo a = nodo("A");
        Nodo b = nodo("B");
        Nodo c = nodo("C");
        Nodo d = nodo("D");
        verificar(!lct.connected(a, b), "al inicio A y B están separados");
        verificar(lct.link(a, b), "link(A,B)");
        verificar(lct.link(b, c), "link(B,C)");
        verificar(lct.link(d, c), "link(D,C)");
        verificar(lct.connected(a, d), "A y D quedan conectados por B y C");
        verificar(lct.findRoot(a) == lct.findRoot(d), "misma raíz para A y D");
        verificar(lct.cut(b, c), "cut(B,C)");
        verificar(!lct.connected(a, d), "tras cortar B-C, A y D se separan");
        verificar(lct.connected(c, d), "C y D siguen juntos");
        verificar(!lct.cut(a, c), "cut(A,C) falla: no son vecinos");
    }

    private static void motorRechazaCiclos() {
        titulo("Motor: link rechaza ciclos");
        LinkCutTree lct = new LinkCutTree();
        Nodo a = nodo("A");
        Nodo b = nodo("B");
        Nodo c = nodo("C");
        lct.link(a, b);
        lct.link(b, c);
        verificar(!lct.link(c, a), "link(C,A) devuelve false: cerraría un ciclo");
    }

    private static void motorSumaDeCamino() {
        titulo("Motor: suma de km sobre un camino");
        LinkCutTree lct = new LinkCutTree();
        Nodo a = nodo("A");
        Nodo b = nodo("B");
        Nodo c = nodo("C");
        Nodo ab = new Nodo("A-B", 2.5, 1, -1);
        Nodo bc = new Nodo("B-C", 1.5, 1, -1);
        lct.link(a, ab);
        lct.link(ab, b);
        lct.link(b, bc);
        lct.link(bc, c);
        Nodo camino = lct.camino(a, c);
        verificar(Math.abs(lct.sumaKm(camino) - 4.0) < 1e-9, "A -> C suma 4.0 km");
        verificar(lct.sumaTramos(camino) == 2, "A -> C tiene 2 tramos");
        camino = lct.camino(c, b);
        verificar(Math.abs(lct.sumaKm(camino) - 1.5) < 1e-9, "C -> B suma 1.5 km (el camino se puede invertir)");
    }

    // ------------------------------------------------------------------
    // Red eléctrica
    // ------------------------------------------------------------------

    /**
     *   S1 --0.8-- P1 --0.3-- P2 --0.1-- C1
     *               \
     *                0.2-- C2           S2 --0.5-- C3
     */
    private static void redEjemploResueltoAMano() {
        titulo("Red de ejemplo resuelta a mano");
        RedElectrica red = new RedElectrica();
        red.agregarNodo("S1", Tipos.SUBESTACION);
        red.agregarNodo("S2", Tipos.SUBESTACION);
        red.agregarNodo("P1", Tipos.POSTE);
        red.agregarNodo("P2", Tipos.POSTE);
        red.agregarNodo("C1", Tipos.CLIENTE);
        red.agregarNodo("C2", Tipos.CLIENTE);
        red.agregarNodo("C3", Tipos.CLIENTE);
        red.conectar("S1", "P1", 0.8);
        red.conectar("P1", "P2", 0.3);
        red.conectar("P2", "C1", 0.1);
        red.conectar("P1", "C2", 0.2);
        red.conectar("S2", "C3", 0.5);

        verificar(red.tieneLuz("C1"), "C1 tiene luz");
        Ruta r = red.ruta("C1");
        verificar(r != null && r.subestacion.equals("S1"), "C1 recibe de S1");
        verificar(r != null && Math.abs(r.km - 1.2) < 1e-9, "C1 está a 1.2 km");
        verificar(r != null && r.tramos == 3, "C1 está a 3 tramos");
        verificar(red.ruta("C3").subestacion.equals("S2"), "C3 recibe de S2");
        verificar(!red.mismaZona("C1", "C3"), "C1 y C3 están en zonas distintas");
        verificar(red.mismaZona("C1", "C2"), "C1 y C2 están en la misma zona");

        verificar(red.cortar("P1", "P2") == Resultado.OK, "cortar P1-P2");
        verificar(!red.tieneLuz("C1"), "C1 se queda sin luz");
        verificar(red.tieneLuz("C2"), "C2 sigue con luz");
        verificar(red.ruta("C1") == null, "C1 no tiene ruta");

        verificar(red.conectar("C2", "C1", 0.4) == Resultado.OK, "reconectar C1 por C2");
        verificar(red.tieneLuz("C1"), "C1 recupera la luz");
        verificar(Math.abs(red.ruta("C1").km - 1.4) < 1e-9, "nueva ruta de C1: 0.8 + 0.2 + 0.4 = 1.4 km");

        verificar(red.conectar("S1", "C1", 1.0) == Resultado.ANILLO, "S1-C1 cerraría un anillo");
        verificar(red.conectar("C1", "C3", 1.0) == Resultado.ANILLO, "unir zonas de S1 y S2 se rechaza");
        verificar(red.cortar("S1", "C3") == Resultado.NO_EXISTE, "cortar un tramo inexistente");
    }

    private static void validaciones() {
        titulo("Validaciones");
        RedElectrica red = new RedElectrica();
        verificar(red.agregarNodo("X", "CASA") == Resultado.DATO_INVALIDO, "tipo inválido");
        red.agregarNodo("A", Tipos.POSTE);
        verificar(red.agregarNodo("A", Tipos.POSTE) == Resultado.YA_EXISTE, "nodo repetido");
        red.agregarNodo("B", Tipos.POSTE);
        verificar(red.conectar("A", "Z", 1) == Resultado.NODO_DESCONOCIDO, "nodo desconocido");
        verificar(red.conectar("A", "B", -1) == Resultado.DATO_INVALIDO, "km negativo");
        verificar(red.conectar("A", "A", 1) == Resultado.DATO_INVALIDO, "lazo");
        verificar(red.conectar("A", "B", 1) == Resultado.OK, "tramo correcto");
        verificar(red.conectar("B", "A", 1) == Resultado.YA_EXISTE, "tramo repetido en otro orden");
        verificar(!red.tieneLuz("A"), "sin subestación no hay luz");
    }

    private static void csvDeEjemplo() {
        titulo("Lectura de los CSV de ejemplo");
        try {
            RedElectrica red = new RedElectrica();
            List<String> errores = new ArrayList<>();
            int nodos = LectorCsv.cargarNodos(Paths.get("datos", "nodos.csv"), red, errores);
            int tramos = LectorCsv.cargarTramos(Paths.get("datos", "tramos.csv"), red, errores);
            verificar(nodos > 0 && tramos > 0, "se cargan nodos y tramos");
            verificar(errores.isEmpty(), "el ejemplo no tiene filas inválidas: " + errores);
        } catch (Exception e) {
            verificar(false, "no se pudieron leer los CSV (ejecute desde 06_codigo): " + e.getMessage());
        }
    }

    /** La prueba principal: LCT y BFS deben responder igual en 20 000 operaciones al azar. */
    private static void lctCoincideConBfsEnOperacionesAleatorias() {
        titulo("Link-Cut Tree y BFS coinciden en 20 000 operaciones al azar");
        Random azar = new Random(99);
        int n = 300;
        RedDinamica lct = new RedElectrica();
        RedDinamica bfs = new RedIngenua();
        for (int i = 0; i < n; i++) {
            String tipo = (i % 50 == 0) ? Tipos.SUBESTACION : Tipos.CLIENTE;  // 6 subestaciones
            lct.agregarNodo("N" + i, tipo);
            bfs.agregarNodo("N" + i, tipo);
        }
        int diferencias = 0;
        for (int k = 0; k < 20_000; k++) {
            String a = "N" + azar.nextInt(n);
            String b = "N" + azar.nextInt(n);
            int dado = azar.nextInt(10);
            if (dado < 4) {
                double km = 1 + azar.nextInt(5);
                if (lct.conectar(a, b, km) != bfs.conectar(a, b, km)) {
                    diferencias++;
                }
            } else if (dado < 6) {
                if (lct.cortar(a, b) != bfs.cortar(a, b)) {
                    diferencias++;
                }
            } else if (dado < 8) {
                if (lct.tieneLuz(a) != bfs.tieneLuz(a)) {
                    diferencias++;
                }
            } else {
                if (lct.mismaZona(a, b) != bfs.mismaZona(a, b)) {
                    diferencias++;
                }
                Ruta r1 = lct.ruta(a);
                Ruta r2 = bfs.ruta(a);
                boolean igual = (r1 == null) ? r2 == null
                        : r2 != null && r1.subestacion.equals(r2.subestacion)
                          && r1.tramos == r2.tramos && Math.abs(r1.km - r2.km) < 1e-9;
                if (!igual) {
                    diferencias++;
                }
            }
            if (k % 1000 == 0 && !lct.clientesSinLuz().equals(bfs.clientesSinLuz())) {
                diferencias++;                     // cada 1 000 operaciones: mismos clientes sin luz
            }
        }
        verificar(diferencias == 0, "respuestas distintas: " + diferencias);
    }

    /** Un alimentador de 100 000 nodos en línea: el motor no usa recursión, no debe desbordar la pila. */
    private static void arbolProfundoSinDesbordar() {
        titulo("Árbol profundo (100 000 nodos en línea)");
        RedElectrica red = new RedElectrica();
        red.agregarNodo("N0", Tipos.SUBESTACION);
        for (int i = 1; i < 100_000; i++) {
            red.agregarNodo("N" + i, Tipos.CLIENTE);
            red.conectar("N" + (i - 1), "N" + i, 0.01);
        }
        Ruta r = red.ruta("N99999");
        verificar(r != null && r.tramos == 99_999, "el último nodo está a 99 999 tramos");
        red.cortar("N50000", "N50001");
        verificar(!red.tieneLuz("N99999") && red.tieneLuz("N50000"), "el corte deja sin luz solo la parte de abajo");
        verificar(Benchmark.generarOperaciones(1000, 1000, new Random(1)).size() == 1000, "el benchmark genera M operaciones");
    }

    /**
     * Caso real: nodos con latitud y longitud, tramos sin km (se calculan con
     * haversine) y un corte programado que deja clientes sin luz.
     */
    private static void coordenadasYCorteProgramado() {
        titulo("Coordenadas, km por haversine y clientes afectados por un corte");
        try {
            Path dir = Files.createTempDirectory("redluz");
            Path nodos = dir.resolve("nodos.csv");
            Path tramos = dir.resolve("tramos.csv");
            Files.write(nodos, Arrays.asList("id,tipo,latitud,longitud",
                    "SET,SUBESTACION,-12.1400,-77.0200",
                    "P1,POSTE,-12.1490,-77.0200",          // 0,009° de latitud ≈ 1,0 km al sur
                    "C1,CLIENTE,-12.1490,-77.0210",
                    "C2,CLIENTE,-12.1500,-77.0200"), StandardCharsets.UTF_8);
            Files.write(tramos, Arrays.asList("origen,destino,km",
                    "SET,P1,",                              // sin km: se calcula
                    "P1,C1,",
                    "P1,C2,0.2"), StandardCharsets.UTF_8);  // con km: se respeta
            RedElectrica red = new RedElectrica();
            List<String> errores = new ArrayList<>();
            Map<String, double[]> coordenadas = new HashMap<>();
            LectorCsv.cargarNodos(nodos, red, errores, coordenadas);
            int conectados = LectorCsv.cargarTramos(tramos, red, errores, coordenadas);
            verificar(errores.isEmpty() && conectados == 3, "se cargan 3 tramos sin errores " + errores);
            Ruta r = red.ruta("C2");
            verificar(r != null && Math.abs(r.km - 1.201) < 0.005, "ruta de C2 ≈ 1,0 km (haversine) + 0,2 km");
            verificar(red.clientesSinLuz().isEmpty(), "antes del corte nadie está sin luz");
            red.cortar("SET", "P1");                         // corte programado del tramo troncal
            verificar(red.clientesSinLuz().equals(Arrays.asList("C1", "C2")), "el corte deja sin luz a C1 y C2");
            red.conectar("SET", "P1", 1.0);                  // fin del corte: se repone
            verificar(red.clientesSinLuz().isEmpty(), "al reponer el tramo vuelve la luz");
        } catch (IOException e) {
            verificar(false, "no se pudo crear el CSV temporal: " + e.getMessage());
        }
    }
}
