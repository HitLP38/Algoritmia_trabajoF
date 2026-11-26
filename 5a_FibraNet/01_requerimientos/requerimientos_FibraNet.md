# Requerimientos · FibraNet (Tema 5.a)

- Proyecto: FibraNet, red de fibra óptica de costo mínimo
- Tema: 5.a Algoritmos de Prim, Kruskal y estructura Union-Find (conjuntos disjuntos)
- Curso: SW305 · Algoritmos Avanzados y Algoritmos Avanzados de Grafos · UNI FIIS · 2026-2
- Grupo 5 · Versión 1.0 · 26/11/2026
- Exposición: martes 08/12/2026

## 1. Problema

FibraNet es una empresa que debe unir con fibra óptica todas sus sedes (oficinas, centros de datos, antenas). Un estudio de campo ya identificó los tramos posibles entre pares de sedes y el costo de tender cada uno (en miles de soles). Tender todos los tramos sería caro e innecesario: basta con que exista un camino entre cualquier par de sedes.

La pregunta de negocio es: **¿qué tramos hay que tender para que todas las sedes queden conectadas al menor costo total?**

En términos de grafos, las sedes son vértices, los tramos son aristas con peso y la respuesta es un **árbol de expansión mínima** (MST). Si el estudio de campo deja sedes aisladas, la red no es conexa y la respuesta es un **bosque de expansión mínima**, uno por cada grupo de sedes que sí se pueden unir.

## 2. Alcance

Incluye:
- Un programa Java por consola que lee la red (CSV o teclado), calcula el plan de tendido con Kruskal y con Prim, y muestra los resultados en tablas.
- Una estructura Union-Find con compresión de caminos y unión por rango, usada por Kruskal y por las consultas de conectividad.
- Pruebas automáticas y un benchmark que contrasta los tiempos medidos con la complejidad teórica.

No incluye:
- Interfaz gráfica, mapas ni base de datos.
- Restricciones físicas de la fibra (capacidad, atenuación, redundancia). Se mencionan como trabajo futuro.

## 3. Actores

| Actor | Descripción |
|---|---|
| Planificador de red | Ingeniero de FibraNet que carga la red, calcula el plan y lo exporta. |
| Analista de rendimiento | Integrante del equipo que ejecuta el benchmark y las pruebas para el informe. |

## 4. Requerimientos funcionales

| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-01 | Cargar la red desde un archivo CSV con columnas `origen,destino,costo` (una fila por tramo; las sedes se deducen de los nombres). Una fila con un solo nombre declara una sede sin tramos; toda sede declarada aparece en los grupos del RF-10. | Alta |
| RF-02 | Ingresar la red por teclado: primero las sedes y luego los tramos, con opción de terminar en cualquier momento. | Alta |
| RF-03 | Validar los datos: costo numérico y positivo, origen distinto de destino, sedes no vacías. Las filas inválidas se informan con su número de línea y se omiten, sin cerrar el programa. | Alta |
| RF-04 | Mostrar la red cargada en una tabla: número de sedes, número de tramos y lista de tramos con su costo. | Media |
| RF-05 | Calcular el plan de tendido con **Kruskal** (ordenar tramos por costo y aceptar cada uno si no forma ciclo, según Union-Find). | Alta |
| RF-06 | Calcular el plan de tendido con **Prim** a partir de una sede inicial elegida por el usuario, usando una cola de prioridad. | Alta |
| RF-07 | Mostrar el recorrido paso a paso de cada algoritmo: en Kruskal, tramo evaluado y si se **acepta** o se **rechaza por ciclo**; en Prim, sede que entra al árbol y tramo que la une. | Alta |
| RF-08 | Mostrar el resultado en una tabla: tramos elegidos, costo de cada uno, costo total y ahorro frente a tender todos los tramos. | Alta |
| RF-09 | Comparar Kruskal y Prim sobre la misma red: costo total (debe coincidir), número de tramos y tiempo de ejecución. | Media |
| RF-10 | Detectar si la red no es conexa e informar los grupos de sedes que quedan separados (componentes obtenidas con Union-Find). | Alta |
| RF-11 | Consultar si dos sedes quedan conectadas en el plan calculado (operación `find` de Union-Find). | Media |
| RF-12 | Exportar el plan de tendido a un CSV en la carpeta `datos\`. | Baja |
| RF-13 | Ejecutar el benchmark: generar redes aleatorias de tamaño creciente con una semilla fija, medir Kruskal, Prim y Union-Find (versión optimizada y versión ingenua) y guardar los tiempos en `benchmark\resultados_*.csv`. | Alta |
| RF-14 | Ejecutar las pruebas automáticas y mostrar cuántas pasan y cuáles fallan. | Alta |

## 5. Requerimientos no funcionales

| ID | Categoría | Requerimiento |
|---|---|---|
| RNF-01 | Plataforma | Java 11 o superior, solo la librería estándar. Se compila y ejecuta con `javac` y `java` desde PowerShell, sin Maven ni Gradle. |
| RNF-02 | Interfaz | Solo consola: menú numerado, mensajes en español y tablas alineadas con `printf`. |
| RNF-03 | Eficiencia | Kruskal en O(E log E), Prim con montículo binario en O(E log V) y Union-Find en O(α(n)) amortizado por operación. |
| RNF-04 | Rendimiento | Una red de 10 000 sedes y 100 000 tramos se resuelve en menos de 2 segundos en una laptop de estudiante. |
| RNF-05 | Memoria | Espacio O(V + E). Union-Find usa dos arreglos `int[]` (`padre` y `rango`). |
| RNF-06 | Robustez | Ningún dato inválido cierra el programa: se informa el error y se vuelve al menú. |
| RNF-07 | Mantenibilidad | El motor (Union-Find, Kruskal, Prim) está separado de la entrada y salida. Cada clase tiene una sola responsabilidad. |
| RNF-08 | Explicabilidad | Cada bloque de código tiene un comentario que dice qué hace y por qué, para poder explicarlo línea por línea en la exposición. |
| RNF-09 | Reproducibilidad | El benchmark usa una semilla fija y repite cada medición varias veces (se reporta el promedio, descartando el calentamiento de la JVM). |
| RNF-10 | Verificabilidad | Las pruebas comparan los resultados con casos resueltos a mano y comprueban que Kruskal y Prim dan el mismo costo en redes aleatorias. |

## 6. Casos de uso

### CU-01 · Cargar la red desde CSV
- Actor: Planificador de red
- Precondición: existe un CSV con el formato de la sección 7.
- Flujo principal:
  1. El planificador elige "Cargar red desde CSV" e ingresa la ruta del archivo.
  2. El sistema lee cada fila, valida los datos (RF-03) y registra sedes y tramos.
  3. El sistema muestra el resumen de la red (RF-04).
- Flujos alternos:
  - 2a. El archivo no existe: se informa y se vuelve al menú.
  - 2b. Hay filas inválidas: se listan con su número de línea y se omiten.
- Postcondición: la red queda en memoria para los cálculos.

### CU-02 · Ingresar la red por teclado
- Actor: Planificador de red
- Flujo principal:
  1. El planificador indica cuántas sedes hay y escribe sus nombres.
  2. Escribe los tramos como `origen destino costo`, uno por línea, y termina con una línea vacía.
  3. El sistema valida cada tramo y muestra el resumen de la red.
- Flujo alterno: 2a. Un tramo menciona una sede no registrada: se informa y se pide de nuevo.

### CU-03 · Calcular el plan con Kruskal
- Actor: Planificador de red
- Precondición: hay una red cargada.
- Flujo principal:
  1. El planificador elige "Plan con Kruskal" y si quiere ver el paso a paso.
  2. El sistema ordena los tramos por costo y los recorre: acepta un tramo si sus sedes están en grupos distintos (`find`) y une esos grupos (`union`); si no, lo rechaza por formar ciclo.
  3. El sistema muestra la tabla de resultado (RF-08).
- Flujo alterno: 2a. Al terminar quedan varios grupos: se muestra el bosque y los grupos separados (RF-10).

### CU-04 · Calcular el plan con Prim
- Actor: Planificador de red
- Precondición: hay una red cargada.
- Flujo principal:
  1. El planificador elige "Plan con Prim" e indica la sede inicial.
  2. El sistema pone en la cola de prioridad los tramos de esa sede y, mientras queden sedes por unir, saca el tramo más barato que llega a una sede nueva y la agrega.
  3. El sistema muestra la tabla de resultado (RF-08).
- Flujos alternos:
  - 1a. La sede inicial no existe: se pide de nuevo.
  - 2a. La cola se vacía antes de unir todas las sedes: la red no es conexa y se informa (RF-10).

### CU-05 · Comparar Kruskal y Prim
- Actor: Planificador de red
- Flujo principal: el sistema ejecuta los dos algoritmos sobre la misma red y muestra una tabla con el costo total, el número de tramos y el tiempo de cada uno, e indica si los costos coinciden.

### CU-06 · Consultar si dos sedes quedan conectadas
- Actor: Planificador de red
- Precondición: ya se calculó un plan.
- Flujo principal: el planificador escribe dos sedes y el sistema responde si `find(a) == find(b)`, junto con el representante de cada grupo.

### CU-07 · Ejecutar el benchmark
- Actor: Analista de rendimiento
- Flujo principal:
  1. El analista elige "Benchmark" (o ejecuta la clase de benchmark directamente).
  2. El sistema genera redes aleatorias conexas para N = 1 000, 2 000, 5 000, 10 000, 20 000 y 50 000 sedes, con densidad baja (E ≈ 4N) y alta (E ≈ N log N o mayor).
  3. Mide Kruskal, Prim y Union-Find (optimizado e ingenuo), repite cada medición y guarda los promedios en CSV.
  4. Muestra la tabla de tiempos y la razón tiempo / cota teórica, que debe mantenerse casi constante.

### CU-08 · Exportar el plan
- Actor: Planificador de red
- Flujo principal: el sistema escribe el último plan calculado en `datos\plan_<algoritmo>.csv` y muestra la ruta del archivo.

## 7. Formato de datos

Archivo de entrada (`datos\red_ejemplo.csv`):

```
origen,destino,costo
Lima,Callao,12
Lima,Ate,9
Callao,Ate,15
```

- La primera fila es el encabezado.
- `costo` es un número positivo (puede tener decimales con punto).
- Un tramo repetido entre las mismas sedes se conserva; los algoritmos se quedan con el más barato de forma natural.
- Una fila con un solo nombre (por ejemplo `Pachacamac`) declara una sede aislada, sin tramos. El plan la reporta como un grupo de una sola sede.

## 8. Menú principal propuesto

```
===== FibraNet · Red de fibra de costo mínimo =====
1. Cargar red desde CSV
2. Ingresar red por teclado
3. Ver red cargada
4. Plan con Kruskal
5. Plan con Prim
6. Comparar Kruskal y Prim
7. Consultar si dos sedes quedan conectadas
8. Exportar último plan a CSV
9. Ejecutar benchmark
0. Salir
```

## 9. Trazabilidad

| Requerimiento | Caso de uso | Componente previsto | Criterio de la rúbrica |
|---|---|---|---|
| RF-01, RF-02, RF-03, RF-04 | CU-01, CU-02 | `LectorCsv`, `EntradaTeclado`, `Grafo` | Calidad de implementación |
| RF-05, RF-07 | CU-03 | `Kruskal`, `UnionFind` | Rigor teórico, Calidad de implementación |
| RF-06, RF-07 | CU-04 | `Prim` | Rigor teórico, Calidad de implementación |
| RF-08, RF-09 | CU-05 | `Tabla`, `Main` | Calidad de implementación |
| RF-10, RF-11 | CU-03, CU-06 | `UnionFind` | Rigor teórico |
| RF-12 | CU-08 | `EscritorCsv` | Calidad de implementación |
| RF-13 | CU-07 | `Benchmark`, `UnionFindIngenuo` | Análisis experimental y benchmarking |
| RF-14 | — | `Pruebas` | Calidad de implementación |
