# PCB del proyecto — Proyecto Final SW305 · Grupo 5 (FibraNet y RedLuz)
> Regla: se lee al iniciar cada chat y se actualiza después de cada avance.
> Última actualización: 26/11/2026 02:40 (hora de Lima) · Tarea en curso: PR de 5.a y 5.b (revisión CodeRabbit); luego informes.

## 1. Identificación (PID)

| Campo | Valor |
|---|---|
| Curso | SW305 · Algoritmos Avanzados y Algoritmos Avanzados de Grafos (UNI · FIIS · Escuela de Ingeniería de Software), ciclo 2026-2 |
| Docente | Ing. Jorge Medianero Acosta (rúbrica firmada también por Ing. Raquel y Daniel Medianero) |
| Tema 5.a | Prim, Kruskal y Union-Find (conjuntos disjuntos). Caso **FibraNet**: conectar sedes con fibra óptica al menor costo. **Expo: martes 08/12/2026** |
| Tema 5.b | Grafos dinámicos con Link-Cut Trees. Caso **RedLuz**: red eléctrica donde se conectan y cortan tramos y se consulta si un cliente sigue con luz. **Expo: jueves 10/12/2026** |
| Equipo (6) | Christopher Ricardo Acosta Simon · Santiago Gavilano Kanashiro · Javier Chionglaw Kong Tarrillo · Gianpierre Eyner Cerron Arica · Raul Antonio Guzman Lopez · Cesar Antoni Loli Aldunate |
| Carpeta local | `C:\Users\Usuario\Desktop\Ciclo 4 2026-2\algoritmia avanzada\proyecto final` |
| Rúbrica | `C:\Users\Usuario\Desktop\Ciclo 4 2026-2\algoritmia avanzada\proyecto final\rubrica de evaluacion\doc de rubrica.pdf` |
| Referencia de estilo de láminas | `Laboratorio2_Slides_v2.html` (deck HTML de un laboratorio anterior, 25 láminas) |
| Repositorio | https://github.com/HitLP38/Algoritmia_trabajoF (público, rama `main`; la carpeta `proyecto final` es la copia de trabajo; CodeRabbit revisa los PR) |

## 2. Estado actual
**EN ESPERA** de Raul: primer push a `main`, compilar y probar 5.a en su PC, subir las ramas `feat/5a-fibranet` y `feat/5b-redluz` y abrir los PR para CodeRabbit. Mientras, Claude prepara los informes.

## 3. Contador de programa (PC) — siguiente instrucción
1. **Raul**: ejecutar los comandos de `git init` y primer push; enviar la salida de `java -version` y `javac -version`.
2. **Raul**: revisar los requerimientos de 5.a y 5.b (`01_requerimientos\requerimientos_*.md`).
3. **Raul**: compilar y probar 5.a (comandos en `5a_FibraNet\06_codigo\README.md`), subir la rama `feat/5a-fibranet`, abrir el PR y pegar los comentarios de CodeRabbit; **Claude** corrige.
4. **Raul**: pegar la tabla de su benchmark (o los CSV) para usar datos de su PC en el informe.
5. **Raul**: lo mismo para 5.b (comandos en `5b_RedLuz\06_codigo\README.md`; rama `feat/5b-redluz`).
6. **Claude**: informe UNI/IEEE de 5.a (con los tiempos de la PC de Raul cuando lleguen).
5. Luego informe, PPT, prompts de láminas, división y guiones (ver sección 4).

## 4. Cola de listos — plan

| # | Entrega | 5.a FibraNet | 5.b RedLuz | Fecha objetivo |
|---|---|---|---|---|
| 0 | Estructura de carpetas + PCB | ✅ | ✅ | 26/11 |
| 1 | Requerimientos (RF, RNF, casos de uso) | ✅ | ✅ | 26/11 |
| 2 | Código Java por consola + pruebas (rama + PR + revisión CodeRabbit) | 🔄 verificado, falta PR | 🔄 verificado, falta PR | 5.a: 28-29/11 · 5.b: 30/11-01/12 |
| 3 | Benchmark (tablas CSV + gráficos para el informe) | 🔄 código listo, faltan datos de la PC de Raul | 🔄 código listo, faltan datos de la PC de Raul | 01/12 |
| 4 | Informe UNI/IEEE (.docx) | ⏳ | ⏳ | 5.a: 02/12 · 5.b: 03/12 |
| 5 | PPT de arquitectura + `04_slides` (un .txt de prompt por lámina) | ⏳ | ⏳ | 5.a: 04/12 · 5.b: 05/12 |
| 6 | División de trabajo + guiones de los 6 integrantes | ⏳ | ⏳ | 5.a: 04/12 · 5.b: 05/12 |
| 7 | Estudio del código en grupo (todos explican el motor) | ⏳ | ⏳ | 06/12 |
| 8 | Ensayo general | ⏳ | ⏳ | 5.a: 07/12 · 5.b: 09/12 |
| 9 | Exposición | ⏳ | ⏳ | 5.a: mar 08/12 · 5.b: jue 10/12 |

## 5. Registros — decisiones vigentes
- RedLuz modela la red como bosque radial con un vértice virtual FUENTE unido a las subestaciones (tiene luz ⇔ connected(cliente, FUENTE)); cada tramo es un nodo del Link-Cut Tree para sumar km sobre el camino.
- Dos temas independientes: cada uno tiene su propia carpeta con los 6 tipos de entregable.
- Carpetas numeradas (01_ a 06_) para que el Explorador las muestre en orden de trabajo.
- **Código en Java** (decisión de Raul, 26/11: le es más rápido). Sin interfaz gráfica: menú por consola (`Scanner`), entrada por teclado o CSV, salidas en tablas de texto con `printf`. Solo la librería estándar, se compila con `javac` sin Maven ni Gradle. Compatible con Java 11+ (sin `record` ni `switch` con flechas).
- Estructura de cada `06_codigo`: `src\` (programa y motor, por paquetes), `pruebas\` (pruebas propias sin JUnit, con un ejecutor que muestra OK/FALLA), `benchmark\` (mide tiempos y escribe CSV), `datos\` (CSV de ejemplo). Los gráficos del informe se generan a partir de esos CSV.
- Flujo de código: Claude escribe y verifica; Raul hace commit y push con comandos numerados de una línea; cada tema va en su rama y PR; CodeRabbit revisa y Claude corrige.
- El PPT es un deck HTML al estilo de `Laboratorio2_Slides_v2.html` (una sola página, láminas navegables).
- Código simple y comentado bloque por bloque para poder explicarlo línea por línea.
- La rúbrica pide para "Sobresaliente" C++ o equivalente con punteros y gestión de memoria. Java cumple como equivalente: arreglos `int[]` `padre`/`rango` en Union-Find, referencias entre objetos `Nodo` (hijo izq./der., padre) en el Link-Cut Tree, y memoria medida con `Runtime`.
- Benchmark contra teoría: Kruskal O(E log E), Prim con heap O(E log V), Union-Find con compresión de caminos y unión por rango O(α(n)) frente a la versión ingenua; Link-Cut Tree O(log n) amortizado frente a BFS O(n) por consulta.
- División de trabajo cruzada: quien lidera el motor en 5.a lidera soporte en 5.b y viceversa; además todos exponen un tramo del recorrido del motor en ambos temas.

## 6. Reglas de trabajo
- Responder en español; en el chat, tono directo.
- Siempre rutas completas desde `C:\Users\Usuario\Desktop\Ciclo 4 2026-2\algoritmia avanzada\proyecto final`.
- Comandos numerados, uno por línea, empezando por el `cd` a la carpeta.
- Planificar → OK de Raul → recién crear entregables.
- Claude no hace commit ni push en el repositorio del grupo sin que Raul lo pida.
- Informe según la sección 3 de la rúbrica: Carátula, 1 Resumen (≤250 palabras), 2 Introducción, 3 Marco teórico (definición formal, notación AS&ST, O/Ω/Θ en peor, mejor y promedio), 4 Diseño (arquitectura, memoria, enlace al repositorio), 5 Resultados (metodología, hardware/software, tablas y gráficos tiempo vs N, contraste con la teoría), 6 Conclusiones y trabajo futuro, 7 Referencias IEEE, 8 Anexos.

## 7. Recursos (tabla de archivos abiertos)

| Recurso | Ruta |
|---|---|
| Gestión | `...\proyecto final\00_gestion\` (PCB.md) |
| Raíz del repo | `...\proyecto final\` (`.gitignore` excluye `rubrica de evaluacion\`, `out\` y `*.class`) |
| Código 5.a | `...\proyecto final\5a_FibraNet\06_codigo\` (README con comandos; motor en `src\fibranet\motor\`) |
| Código 5.b | `...\proyecto final\5b_RedLuz\06_codigo\` (README con comandos; motor en `src\redluz\motor\`) |
| Tema 5.a | `...\proyecto final\5a_FibraNet\` (01_requerimientos, 02_informe, 03_ppt, 04_slides, 05_guiones, 06_codigo\{src,pruebas,benchmark,datos}) |
| Tema 5.b | `...\proyecto final\5b_RedLuz\` (misma estructura) |
| Rúbrica | `...\proyecto final\rubrica de evaluacion\doc de rubrica.pdf` |

Criterios de la rúbrica (5 pts cada uno: Sobresaliente 5 · Bueno 4 · En proceso 3 · Deficiente 1-2):
1. Modelamiento y rigor teórico.
2. Calidad de implementación y código.
3. Análisis experimental y benchmarking.
4. Informe técnico y formato académico.

## 8. Pendientes (bloqueados o en espera)
- [ ] Raul: `git init` y primer push al repositorio (comandos en el chat del 26/11).
- [ ] Raul: versión de Java instalada (`java -version`), para fijar la versión mínima.
- [ ] Códigos de alumno: los escribe Raul a mano en la carátula de cada informe.

## 9. Bitácora (más reciente arriba)
- **26/11 04:10** Código Java de 5.b escrito y verificado: Link-Cut Tree sin recursión, FUENTE virtual y tramos como nodos; 45/45 pruebas (incluye 20 000 operaciones al azar comparadas con BFS y un alimentador de 100 000 nodos); 100 000 operaciones en 0,36 s; con N = 5 000 el LCT es 213 veces más rápido que BFS.
- **26/11 03:30** Código Java de 5.a escrito y verificado en Java 21 con `--release 11`: compila sin advertencias (`-Xlint:all`), 31/31 pruebas OK, menú probado con los 3 CSV de `datos\`, benchmark en 13 s (Union-Find ingenuo 12 855 ns/op vs optimizado 3,6 ns/op con n = 20 000).
- **26/11 03:00** Requerimientos v1.0 de FibraNet (14 RF, 10 RNF, 8 CU) y RedLuz (13 RF, 10 RNF, 7 CU) en `01_requerimientos`.
- **26/11 02:40** Raul aprobó: repositorio https://github.com/HitLP38/Algoritmia_trabajoF, código en Java, PPT en HTML tipo Laboratorio 2, códigos de alumno a mano. Creados `.gitignore` y `README.md` en la raíz.
- **26/11 02:35** Leída la rúbrica (criterios, formato de informe, fechas). Creada la estructura de carpetas y este PCB.

## 10. Cambio de contexto — cómo retomar en un chat nuevo
```
Lee el PCB en C:\Users\Usuario\Desktop\Ciclo 4 2026-2\algoritmia avanzada\proyecto final\00_gestion\PCB.md y sigue desde el PC (sección 3).
```
