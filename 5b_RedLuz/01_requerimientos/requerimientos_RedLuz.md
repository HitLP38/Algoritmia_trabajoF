# Requerimientos · RedLuz (Tema 5.b)

- Proyecto: RedLuz, red eléctrica dinámica
- Tema: 5.b Grafos dinámicos: operaciones básicas, componentes conectadas y caminos en grafos dinámicos con Link-Cut Trees
- Curso: SW305 · Algoritmos Avanzados y Algoritmos Avanzados de Grafos · UNI FIIS · 2026-2
- Grupo 5 · Versión 1.0 · 26/11/2026
- Exposición: jueves 10/12/2026

## 1. Problema

RedLuz es una distribuidora eléctrica. Su red de distribución es **radial**: la energía sale de las subestaciones y llega a cada cliente por un único camino de tramos (cables entre postes). Durante el día la red cambia todo el tiempo: una cuadrilla conecta un tramo nuevo, otro se corta por una falla o por mantenimiento, y la central de atención recibe llamadas preguntando "¿mi casa tiene luz?".

La pregunta de negocio es: **después de cada cambio, ¿qué clientes siguen conectados a una subestación y por qué ruta les llega la energía?**

Recorrer toda la red con BFS en cada consulta cuesta O(n), demasiado lento cuando hay cientos de miles de postes y miles de eventos por hora. Un **Link-Cut Tree** mantiene el bosque y responde conectar, cortar y consultar en **O(log n) amortizado** por operación.

### Modelo

- Cada subestación, poste o cliente es un **vértice**. Cada cable es un **tramo** (arista) con su longitud en km.
- La red es un **bosque**: un tramo que cerraría un anillo se rechaza, porque una red radial no los permite.
- Se añade un vértice virtual **FUENTE** unido a todas las subestaciones. Así, "el cliente tiene luz" equivale a "el cliente está en el mismo árbol que FUENTE". Como consecuencia, tampoco se permite unir directamente dos zonas que ya tienen subestación, porque eso cerraría un anillo a través de FUENTE.
- Cada tramo se representa como un nodo propio dentro del Link-Cut Tree que guarda su longitud. Esto permite sumar longitudes a lo largo de un camino.

## 2. Alcance

Incluye:
- Un programa Java por consola que carga la red (CSV o teclado), procesa eventos de conexión y corte, y responde consultas en tablas.
- Un Link-Cut Tree implementado desde cero (árboles splay con `rotar`, `splay`, `access`, `makeRoot`, `findRoot`, `link`, `cut`).
- Una versión ingenua (listas de adyacencia + BFS) que sirve para las pruebas y como punto de comparación en el benchmark.

No incluye:
- Flujo de potencia, tensión ni carga de los transformadores.
- Redes malladas (con anillos). Se mencionan como trabajo futuro.

## 3. Actores

| Actor | Descripción |
|---|---|
| Operador de red | Registra los eventos de conexión y corte que informan las cuadrillas. |
| Agente de atención | Consulta si un cliente tiene luz y por qué ruta le llega. |
| Analista de rendimiento | Ejecuta el benchmark y las pruebas para el informe. |

## 4. Requerimientos funcionales

| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-01 | Cargar la red inicial desde dos CSV: nodos (`id,tipo`, con tipo SUBESTACION, POSTE o CLIENTE) y tramos (`origen,destino,km`). | Alta |
| RF-02 | Registrar nodos y tramos por teclado. | Alta |
| RF-03 | Validar los datos: tipo válido, ids existentes, km positivo, origen distinto de destino. Las filas inválidas se informan con su línea y se omiten. | Alta |
| RF-04 | **Conectar** un tramo entre dos nodos (`link`). Si ambos ya están en el mismo árbol, el tramo se rechaza porque cerraría un anillo. | Alta |
| RF-05 | **Cortar** un tramo existente (`cut`), por falla o mantenimiento. Si el tramo no existe, se informa. | Alta |
| RF-06 | **Consultar si un cliente tiene luz**: `connected(cliente, FUENTE)`. | Alta |
| RF-07 | **Consultar la ruta de alimentación** de un cliente con luz: distancia total en km y número de tramos hasta su subestación (suma y conteo sobre el camino). | Alta |
| RF-08 | Consultar si dos nodos cualesquiera están en la misma zona (mismo árbol). | Media |
| RF-09 | Procesar un archivo de eventos en lote (`CONECTAR`, `CORTAR`, `LUZ`, `RUTA`) y mostrar una tabla con el resultado de cada evento. | Alta |
| RF-10 | Mostrar el estado de la red: número de nodos, tramos activos y lista de tramos. | Media |
| RF-11 | Mostrar el paso a paso de una operación (accesos, rotaciones y splays) para usarlo en la exposición. | Media |
| RF-12 | Ejecutar el benchmark: redes aleatorias de tamaño creciente con una mezcla fija de operaciones, midiendo el Link-Cut Tree y la versión ingenua; guardar los tiempos en `benchmark\resultados_*.csv`. | Alta |
| RF-13 | Ejecutar las pruebas automáticas y mostrar cuántas pasan y cuáles fallan. | Alta |

## 5. Requerimientos no funcionales

| ID | Categoría | Requerimiento |
|---|---|---|
| RNF-01 | Plataforma | Java 17 o superior, solo la librería estándar, compilado con `javac` desde PowerShell. |
| RNF-02 | Interfaz | Solo consola: menú numerado, mensajes en español y tablas alineadas con `printf`. |
| RNF-03 | Eficiencia | `link`, `cut`, `connected` y la consulta de ruta en O(log n) amortizado. La versión ingenua es O(n) por consulta. |
| RNF-04 | Rendimiento | 100 000 nodos y 100 000 operaciones mezcladas en menos de 2 segundos con el Link-Cut Tree. |
| RNF-05 | Memoria | Espacio O(n). Cada nodo es un objeto con referencias a hijo izquierdo, hijo derecho y padre, un indicador de inversión y sus agregados. |
| RNF-06 | Robustez | Ninguna operación inválida (anillo, tramo inexistente, id desconocido) cierra el programa. |
| RNF-07 | Mantenibilidad | El motor (`LinkCutTree`, `Nodo`) no conoce la red eléctrica; la clase `RedElectrica` traduce clientes y tramos a operaciones del motor. |
| RNF-08 | Explicabilidad | Cada método del motor tiene un comentario con su idea, un dibujo ASCII cuando ayuda y su costo. |
| RNF-09 | Reproducibilidad | El benchmark usa semilla fija y repite las mediciones (promedio tras el calentamiento de la JVM). |
| RNF-10 | Verificabilidad | Las pruebas aplican miles de operaciones aleatorias a la vez sobre el Link-Cut Tree y sobre la versión ingenua y exigen que respondan lo mismo. |

## 6. Casos de uso

### CU-01 · Cargar la red inicial
- Actor: Operador de red
- Flujo principal:
  1. El operador elige "Cargar red desde CSV" e ingresa la ruta de nodos y la de tramos.
  2. El sistema crea los nodos, une cada subestación a FUENTE y conecta cada tramo (RF-04).
  3. El sistema muestra el estado de la red (RF-10) y los tramos rechazados, si los hubo.
- Flujos alternos: 2a. Un archivo no existe o una fila es inválida: se informa y se omite.

### CU-02 · Conectar un tramo
- Actor: Operador de red
- Flujo principal:
  1. El operador escribe `origen destino km`.
  2. El sistema verifica que no estén ya conectados y ejecuta `link`.
  3. El sistema confirma el tramo y cuántos clientes ganaron luz, si se pide.
- Flujo alterno: 2a. Ya están conectados: se rechaza y se explica que cerraría un anillo.

### CU-03 · Cortar un tramo
- Actor: Operador de red
- Flujo principal:
  1. El operador escribe `origen destino`.
  2. El sistema ejecuta `cut` sobre el nodo del tramo y confirma.
- Flujo alterno: 2a. El tramo no existe: se informa.

### CU-04 · Consultar si un cliente tiene luz
- Actor: Agente de atención
- Flujo principal: el agente escribe el id del cliente y el sistema responde "CON LUZ" o "SIN LUZ" según `connected(cliente, FUENTE)`.

### CU-05 · Consultar la ruta de alimentación
- Actor: Agente de atención
- Precondición: el cliente tiene luz.
- Flujo principal: el sistema hace `makeRoot(FUENTE)` y `access(cliente)` y lee del árbol splay la suma de km y el número de tramos del camino.
- Flujo alterno: el cliente no tiene luz; se informa y no se calcula la ruta.

### CU-06 · Procesar eventos en lote
- Actor: Operador de red
- Flujo principal:
  1. El operador indica la ruta del archivo de eventos.
  2. El sistema ejecuta cada línea en orden y arma una tabla `# | evento | datos | resultado`.
  3. Al final muestra el total de eventos, rechazados y tiempo empleado.

### CU-07 · Ejecutar el benchmark
- Actor: Analista de rendimiento
- Flujo principal:
  1. El sistema genera, con semilla fija, bosques de N = 1 000, 5 000, 10 000, 50 000 y 100 000 nodos y M = N operaciones (40 % consultas de luz, 30 % conectar, 30 % cortar).
  2. Ejecuta la misma secuencia con el Link-Cut Tree y con la versión ingenua (esta última hasta el N que termine en un tiempo razonable).
  3. Guarda los promedios en CSV y muestra la tabla con la razón tiempo / (M log N) y tiempo / (M · N).

## 7. Formato de datos

`datos\nodos.csv`
```
id,tipo
S1,SUBESTACION
P1,POSTE
C1,CLIENTE
```

`datos\tramos.csv`
```
origen,destino,km
S1,P1,0.8
P1,C1,0.1
```

`datos\eventos.csv`
```
evento,a,b,km
LUZ,C1,,
CORTAR,S1,P1,
LUZ,C1,,
CONECTAR,S1,P1,0.8
RUTA,C1,,
```

## 8. Menú principal propuesto

```
===== RedLuz · Red eléctrica dinámica =====
1. Cargar red desde CSV
2. Registrar nodo o tramo por teclado
3. Ver estado de la red
4. Conectar tramo
5. Cortar tramo
6. ¿El cliente tiene luz?
7. Ruta de alimentación del cliente
8. ¿Dos nodos están en la misma zona?
9. Procesar archivo de eventos
10. Ejecutar benchmark
0. Salir
```

## 9. Trazabilidad

| Requerimiento | Caso de uso | Componente previsto | Criterio de la rúbrica |
|---|---|---|---|
| RF-01, RF-02, RF-03 | CU-01 | `LectorCsv`, `RedElectrica` | Calidad de implementación |
| RF-04, RF-05 | CU-02, CU-03 | `LinkCutTree.link`, `LinkCutTree.cut` | Rigor teórico, Calidad de implementación |
| RF-06, RF-08 | CU-04 | `LinkCutTree.connected`, `findRoot` | Rigor teórico |
| RF-07 | CU-05 | `LinkCutTree.access`, agregados en `Nodo` | Rigor teórico |
| RF-09, RF-10, RF-11 | CU-06 | `ProcesadorEventos`, `Tabla` | Calidad de implementación |
| RF-12 | CU-07 | `Benchmark`, `RedIngenua` | Análisis experimental y benchmarking |
| RF-13 | — | `Pruebas`, `RedIngenua` | Calidad de implementación |
