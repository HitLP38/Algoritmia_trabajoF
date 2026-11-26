# FibraNet · Código (Tema 5.a)

Programa Java por consola que calcula la red de fibra óptica de costo mínimo con **Kruskal** y **Prim**, usando **Union-Find** con compresión de caminos y unión por rango.

## Estructura

```
06_codigo/
├── src/fibranet/
│   ├── Main.java                 menú por consola (solo entrada y salida)
│   ├── modelo/Arista.java        un tramo: origen, destino, costo
│   ├── modelo/Grafo.java         sedes + lista de aristas + lista de adyacencia
│   ├── motor/UnionFind.java      MOTOR: find con compresión, union por rango
│   ├── motor/Kruskal.java        MOTOR: ordenar tramos + Union-Find
│   ├── motor/Prim.java           MOTOR: cola de prioridad desde una sede
│   ├── motor/ResultadoMST.java   plan obtenido, costo, grupos, tiempo, paso a paso
│   ├── motor/UnionFindIngenuo.java  versión sin optimizar (solo para el benchmark)
│   └── io/                       LectorCsv, EscritorCsv, Tabla
├── pruebas/fibranet/pruebas/Pruebas.java      31 verificaciones sin librerías externas
├── benchmark/fibranet/benchmark/Benchmark.java  tiempos vs N y CSV de resultados
└── datos/                        red_ejemplo.csv, red_no_conexa.csv, red_con_errores.csv
```

## Requisitos

- Java 11 o superior (`java -version`).
- Ejecutar todos los comandos desde la carpeta `06_codigo`.

## Comandos (PowerShell)

1. `cd "C:\Users\Usuario\Desktop\Ciclo 4 2026-2\algoritmia avanzada\proyecto final\5a_FibraNet\06_codigo"`
2. `javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java -Path src,pruebas,benchmark).FullName`
3. `java -cp out fibranet.pruebas.Pruebas`
4. `java -cp out fibranet.Main`
5. `java -cp out fibranet.benchmark.Benchmark`

El paso 3 debe terminar con `31 pasaron, 0 fallaron`. El paso 5 tarda menos de un minuto y deja `benchmark\resultados_mst.csv` y `benchmark\resultados_unionfind.csv`.

## Complejidad

| Operación | Peor caso | Mejor caso | Memoria |
|---|---|---|---|
| `UnionFind.find` / `union` | O(α(n)) amortizado | Ω(1) | O(n) |
| `Kruskal.calcular` | O(E log E) = O(E log V) | Ω(E log E) por el ordenamiento | O(V + E) |
| `Prim.calcular` (montículo binario) | O(E log V) | Ω(E) | O(V + E) |
