# RedLuz · Código (Tema 5.b)

Programa Java por consola que mantiene una red eléctrica radial que cambia (se conectan y cortan tramos) y responde si un cliente tiene luz y por qué ruta, con un **Link-Cut Tree** implementado desde cero.

## Estructura

```
06_codigo/
├── src/redluz/
│   ├── Main.java                   menú por consola (solo entrada y salida)
│   ├── motor/Nodo.java             MOTOR: nodo del splay (izq, der, padre, inversión, agregados)
│   ├── motor/LinkCutTree.java      MOTOR: rotar, splay, access, makeRoot, findRoot, link, cut
│   ├── modelo/RedDinamica.java     contrato común de la red
│   ├── modelo/RedElectrica.java    la red sobre el Link-Cut Tree (FUENTE virtual + tramos como nodos)
│   ├── modelo/RedIngenua.java      la misma red con BFS (referencia para pruebas y benchmark)
│   ├── modelo/Ruta.java, Tipos.java
│   └── io/                         LectorCsv, ProcesadorEventos, Mensajes, Tabla
├── pruebas/redluz/pruebas/Pruebas.java        45 verificaciones sin librerías externas
├── benchmark/redluz/benchmark/Benchmark.java  Link-Cut Tree vs BFS y CSV de resultados
└── datos/                          nodos.csv, tramos.csv, eventos.csv
```

## Cómo se modela la red

- `FUENTE` es un nodo virtual unido a todas las subestaciones: un cliente **tiene luz** si está conectado a `FUENTE`.
- Cada tramo es un nodo propio del Link-Cut Tree que guarda sus km. Después de `makeRoot(FUENTE)` y `access(cliente)`, la raíz del splay tiene la suma de km, el número de tramos y la subestación del camino.
- Un tramo que cerraría un anillo se rechaza (red radial). Unir dos zonas que ya tienen subestación también cierra un anillo a través de `FUENTE`.

## Requisitos

- Java 11 o superior (`java -version`).
- Ejecutar todos los comandos desde la carpeta `06_codigo`.

## Comandos (PowerShell)

1. `cd "C:\Users\Usuario\Desktop\Ciclo 4 2026-2\algoritmia avanzada\proyecto final\5b_RedLuz\06_codigo"`
2. `javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java -Path src,pruebas,benchmark).FullName`
3. `java -cp out redluz.pruebas.Pruebas`
4. `java -cp out redluz.Main`
5. `java -cp out redluz.benchmark.Benchmark`

El paso 3 debe terminar con `45 pasaron, 0 fallaron`. El paso 5 tarda alrededor de un minuto y deja `benchmark\resultados_lct.csv`. En el menú, la opción 11 activa el paso a paso del motor (accesos y rotaciones) para mostrarlo en la exposición.

## Complejidad

| Operación | Link-Cut Tree | Versión BFS |
|---|---|---|
| `link` / conectar | O(log n) amortizado | O(n) |
| `cut` / cortar | O(log n) amortizado | O(1) |
| `connected` / tiene luz | O(log n) amortizado | O(n) |
| ruta (suma de km sobre el camino) | O(log n) amortizado | O(n) |
| Memoria | O(n): un objeto `Nodo` por vértice y por tramo | O(n) |
