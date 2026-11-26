// Genera el informe UNI/IEEE de FibraNet (Tema 5.a)
const fs = require('fs');
const {
  Document, Packer, Paragraph, TextRun, ImageRun, Table, TableRow, TableCell,
  AlignmentType, WidthType, BorderStyle, ShadingType, SectionType, LevelFormat,
  PageNumber, Footer, Header, ExternalHyperlink, VerticalAlign
} = require('docx');

const FONT = 'Times New Roman';
const COL = 4693;            // ancho de columna en DXA (A4, márgenes 0.75", separación 0.25")
const FIG = '/tmp/claude-0/inf5a/';

// --- mini marcado: **negrita**, *cursiva*, [[resaltado amarillo]], `código` ---
function runs(texto, base = {}) {
  const partes = texto.split(/(\*\*[^*]+\*\*|\*[^*]+\*|\[\[[^\]]+\]\]|`[^`]+`)/g).filter(Boolean);
  return partes.map(p => {
    const o = { font: FONT, size: 20, ...base };
    if (p.startsWith('**')) return new TextRun({ ...o, text: p.slice(2, -2), bold: true });
    if (p.startsWith('[[')) return new TextRun({ ...o, text: p.slice(2, -2), highlight: 'yellow' });
    if (p.startsWith('`')) return new TextRun({ ...o, text: p.slice(1, -1), font: 'Consolas', size: (o.size || 20) - 2 });
    if (p.startsWith('*')) return new TextRun({ ...o, text: p.slice(1, -1), italics: true });
    return new TextRun({ ...o, text: p });
  });
}
const P = (t, opt = {}) => new Paragraph({
  children: runs(t, opt.run), alignment: opt.align || AlignmentType.JUSTIFIED,
  spacing: { after: 80, line: 240 }, indent: opt.noIndent ? undefined : { firstLine: 202 }, ...opt.p
});
const H1 = t => new Paragraph({
  children: [new TextRun({ text: t.toUpperCase(), font: FONT, size: 20, bold: true })],
  alignment: AlignmentType.CENTER, spacing: { before: 200, after: 100 }, keepNext: true
});
const H2 = t => new Paragraph({
  children: [new TextRun({ text: t, font: FONT, size: 20, italics: true, bold: true })],
  spacing: { before: 120, after: 60 }, keepNext: true
});
const B = t => new Paragraph({ children: runs(t), numbering: { reference: 'vinetas', level: 0 },
  alignment: AlignmentType.JUSTIFIED, spacing: { after: 40, line: 240 } });
const N = (t, ref = 'numeros') => new Paragraph({ children: runs(t), numbering: { reference: ref, level: 0 },
  alignment: AlignmentType.JUSTIFIED, spacing: { after: 40, line: 240 } });
const CODE = lineas => lineas.map((l, i) => new Paragraph({
  children: [new TextRun({ text: l || ' ', font: 'Consolas', size: 15 })],
  spacing: { after: 0, line: 220 }, shading: { type: ShadingType.CLEAR, fill: 'F2F2F2' },
  ...(i === lineas.length - 1 ? { spacing: { after: 100, line: 220 } } : {})
}));

const borde = { style: BorderStyle.SINGLE, size: 4, color: '000000' };
const bordes = { top: borde, bottom: borde, left: borde, right: borde };
function tabla(encabezados, filas, anchos) {
  const total = anchos.reduce((a, b) => a + b, 0);
  const celda = (t, cab, w) => new TableCell({
    borders: bordes, width: { size: w, type: WidthType.DXA },
    margins: { top: 30, bottom: 30, left: 60, right: 60 }, verticalAlign: VerticalAlign.CENTER,
    shading: cab ? { type: ShadingType.CLEAR, fill: 'E7E6E6' } : undefined,
    children: [new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 0 },
      children: runs(t, { size: 15, bold: cab }) })]
  });
  return new Table({
    width: { size: total, type: WidthType.DXA }, columnWidths: anchos,
    rows: [new TableRow({ tableHeader: true, children: encabezados.map((e, i) => celda(e, true, anchos[i])) }),
      ...filas.map(f => new TableRow({ children: f.map((c, i) => celda(c, false, anchos[i])) }))]
  });
}
const tituloTabla = (num, t) => [
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 120, after: 0 }, keepNext: true,
    children: [new TextRun({ text: 'TABLA ' + num, font: FONT, size: 16 })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 60 }, keepNext: true,
    children: runs(t, { size: 16, smallCaps: true }) })
];
function figura(archivo, num, pie, anchoPx = 300) {
  const buf = fs.readFileSync(FIG + archivo);
  const w = buf.readUInt32BE(16), h = buf.readUInt32BE(20);
  return [
    new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 100, after: 40 }, keepNext: true,
      children: [new ImageRun({ type: 'png', data: buf, transformation: { width: anchoPx, height: Math.round(anchoPx * h / w) },
        altText: { title: 'Fig. ' + num, description: pie, name: 'fig' + num } })] }),
    new Paragraph({ alignment: AlignmentType.JUSTIFIED, spacing: { after: 120 },
      children: runs('Fig. ' + num + '. ' + pie, { size: 16 }) })
  ];
}
const salto = () => new Paragraph({ children: [], spacing: { after: 0 } });

// ------------------------------------------------------------------
// CARÁTULA
// ------------------------------------------------------------------
const C = (t, size = 24, extra = {}) => new Paragraph({ alignment: AlignmentType.CENTER,
  spacing: { after: 120, line: 276 }, children: runs(t, { size, ...extra }) });
const integrantes = [
  'Acosta Simon, Christopher Ricardo', 'Cerron Arica, Gianpierre Eyner', 'Chionglaw Kong Tarrillo, Javier',
  'Gavilano Kanashiro, Santiago', 'Guzman Lopez, Raul Antonio', 'Loli Aldunate, Cesar Antoni'
];
const caratula = [
  C('**UNIVERSIDAD NACIONAL DE INGENIERÍA**', 30),
  C('Facultad de Ingeniería Industrial y de Sistemas', 24),
  C('Escuela de Ingeniería de Software', 24),
  new Paragraph({ spacing: { after: 600 }, children: [] }),
  C('**FibraNet: diseño de una red de fibra óptica de costo mínimo con los algoritmos de Kruskal y Prim y la estructura Union-Find**', 32),
  new Paragraph({ spacing: { after: 300 }, children: [] }),
  C('Tema 5.a: Algoritmos de Prim, Kruskal y estructura Union-Find (conjuntos disjuntos)', 22),
  C('Curso: Algoritmos Avanzados y Algoritmos Avanzados de Grafos (SW305)', 22),
  C('Semestre académico 2026-2', 22),
  new Paragraph({ spacing: { after: 300 }, children: [] }),
  C('**Integrantes (Grupo 5)**', 22),
  tabla(['Apellidos y nombres', 'Código'], integrantes.map(n => [n, '[[20XXXXXXX]]']), [4800, 1800]),
  new Paragraph({ spacing: { after: 300 }, children: [] }),
  C('**Docente:** Ing. Jorge Medianero Acosta', 22),
  C('**Fecha de entrega:** martes 8 de diciembre de 2026', 22),
  C('Lima, Perú', 22),
];

// ------------------------------------------------------------------
// CUERPO (dos columnas, estilo IEEE)
// ------------------------------------------------------------------
const cuerpo = [
  new Paragraph({ alignment: AlignmentType.JUSTIFIED, spacing: { after: 120 }, children: [
    new TextRun({ text: 'Resumen', font: FONT, size: 18, bold: true, italics: true }),
    ...runs('—Una empresa de telecomunicaciones debe unir todas sus sedes con fibra óptica al menor costo posible. El problema se modela como un grafo no dirigido y ponderado, cuya solución es un árbol de expansión mínima (MST), o un bosque cuando la red no es conexa. Se implementaron en Java, sin librerías externas, los algoritmos de Kruskal y de Prim y la estructura Union-Find con compresión de caminos y unión por rango. El programa funciona por consola, lee la red desde CSV o teclado y muestra el plan de tendido y el paso a paso de cada algoritmo en tablas. La corrección se verificó con 31 pruebas automáticas, entre ellas 200 redes aleatorias en las que Kruskal y Prim debían obtener el mismo costo. Para el análisis experimental se midieron redes de 1 000 a 50 000 sedes con densidad baja y alta. Las mediciones son coherentes con las cotas O(E log V) de ambos algoritmos. Kruskal fue hasta tres veces más rápido que Prim en redes densas grandes. Union-Find optimizado mantuvo un costo casi constante, de unos 4 ns por operación hasta un millón de elementos, mientras que la versión ingenua creció de forma lineal hasta 12 700 ns por operación. En la red de ejemplo de 12 sedes de Lima, el plan reduce el costo de tendido en 55,5 %.', { size: 18, bold: true })
  ] }),
  new Paragraph({ alignment: AlignmentType.JUSTIFIED, spacing: { after: 160 }, children: [
    new TextRun({ text: 'Palabras clave', font: FONT, size: 18, bold: true, italics: true }),
    ...runs('—árbol de expansión mínima, Kruskal, Prim, Union-Find, conjuntos disjuntos, análisis de complejidad, benchmarking.', { size: 18, bold: true })
  ] }),

  H1('I. Introducción y motivación'),
  P('Las redes de fibra óptica son la base de los servicios de internet, telefonía y transmisión de datos de una organización. Tender fibra cuesta dinero: zanjas, ductos, permisos municipales y mano de obra. Por eso, cuando una empresa debe unir muchas sedes, la pregunta no es solo *cómo* conectarlas, sino *qué tramos* tender para que todas queden comunicadas gastando lo mínimo.'),
  P('FibraNet es el caso de estudio de este trabajo: una empresa con sedes distribuidas en Lima Metropolitana que ya cuenta con un estudio de campo con los tramos posibles entre pares de sedes y el costo de cada uno. Basta con que exista *un* camino entre cualquier par de sedes, así que cualquier tramo que cierre un ciclo es un gasto innecesario. El problema corresponde al del árbol de expansión mínima, uno de los problemas clásicos de optimización en grafos, resuelto por Borůvka (1926), Jarník (1930), Kruskal (1956) [2] y Prim (1957) [3].'),
  P('La justificación técnica del tema es doble. Primero, los dos algoritmos son ejemplos claros de la estrategia voraz y de cómo una estructura de datos adecuada cambia la eficiencia: Kruskal depende de Union-Find y Prim de una cola de prioridad. Segundo, Union-Find es un caso notable de análisis amortizado, cuyo costo por operación crece con la función inversa de Ackermann α(n), que en la práctica nunca supera 4 [4], [5].'),
  H2('A. Objetivo general'),
  P('Diseñar, implementar y evaluar un sistema por consola que calcule la red de fibra óptica de costo mínimo de FibraNet con los algoritmos de Kruskal y Prim, y contrastar su rendimiento medido con su complejidad teórica.'),
  H2('B. Objetivos específicos'),
  N('Formalizar el problema como un árbol (o bosque) de expansión mínima y especificar las estructuras y algoritmos con sus tipos, entradas, salidas, precondiciones y poscondiciones.'),
  N('Implementar Union-Find con compresión de caminos y unión por rango, Kruskal y Prim con montículo binario, con código comentado y separado de la entrada y salida.'),
  N('Verificar la corrección con pruebas automáticas sobre casos resueltos a mano y redes aleatorias.'),
  N('Medir tiempos y memoria para tamaños crecientes y contrastarlos con las cotas O, Ω y Θ.'),

  H1('II. Marco teórico y especificación algorítmica'),
  H2('A. Definiciones'),
  P('Sea *G = (V, E, w)* un grafo no dirigido, donde *V* es el conjunto de sedes, *E ⊆ V × V* el conjunto de tramos posibles y *w: E → ℝ⁺* el costo de cada tramo. Se denota *n = |V|* y *m = |E|*.'),
  P('Un **árbol de expansión** de *G* es un subconjunto *T ⊆ E* que conecta todos los vértices sin formar ciclos; si *G* es conexo, *|T| = n − 1*. Un **árbol de expansión mínima** es aquel que minimiza *w(T) = Σ w(e)*, con *e ∈ T*. Si *G* tiene *k* componentes conexas, la solución es un **bosque de expansión mínima** con *n − k* aristas.'),
  P('**Propiedad del corte.** Para cualquier partición *(S, V − S)* de los vértices, la arista más liviana que cruza el corte pertenece a algún MST. **Propiedad del ciclo.** La arista más pesada de un ciclo no pertenece a ningún MST (si los pesos son distintos). Ambas propiedades justifican que una elección voraz correcta nunca deba deshacerse [1]: Kruskal aplica la del ciclo al rechazar aristas que cierran un ciclo, y Prim aplica la del corte entre el árbol que crece y el resto.'),
  H2('B. Estructura Union-Find (conjuntos disjuntos)'),
  P('Union-Find mantiene una partición de *{0, …, n − 1}* en conjuntos disjuntos. Cada conjunto es un árbol representado con un arreglo `padre[]`; la raíz es su representante. Se usan dos optimizaciones [4], [5]:'),
  B('**Compresión de caminos:** al ejecutar `find(x)`, cada nodo del camino hacia la raíz pasa a apuntar directamente a ella.'),
  B('**Unión por rango:** `union(a, b)` cuelga la raíz de menor rango (cota de la altura) debajo de la de mayor rango; solo si empatan, el rango crece en uno.'),
  P('Con ambas, una secuencia de *m* operaciones sobre *n* elementos cuesta *O(m · α(n))* [4]. Solo con unión por rango, la altura es a lo sumo *⌊log₂ n⌋*, por lo que una operación aislada cuesta *O(log n)* en el peor caso.'),
  H2('C. Algoritmo de Kruskal'),
  P('Kruskal ordena las aristas por costo creciente y las recorre; acepta una arista *(u, v)* si `find(u) ≠ find(v)` y en ese caso ejecuta `union(u, v)`. Se detiene cuando acepta *n − 1* aristas.'),
  ...CODE([
    'KRUSKAL(G):',
    '  T ← ∅ ; UF ← MAKE-SET(n)',
    '  ordenar E por w ascendente          O(m log m)',
    '  para cada (u,v) en E:',
    '    si |T| = n−1: terminar',
    '    si UF.union(u,v): T ← T ∪ {(u,v)}   O(α(n))',
    '  retornar T'
  ]),
  H2('D. Algoritmo de Prim'),
  P('Prim hace crecer un árbol desde una sede inicial *s*. Una cola de prioridad (montículo binario) guarda los tramos que salen del árbol; en cada paso se extrae el más barato que llega a una sede nueva. Se usó la versión perezosa: los tramos obsoletos se descartan al salir de la cola. Si la cola se vacía antes de visitar todas las sedes, se inicia un nuevo árbol desde una sede no visitada, lo que produce el mismo bosque que Kruskal.'),
  ...CODE([
    'PRIM(G, s):',
    '  T ← ∅ ; enArbol[·] ← falso ; Q ← montículo vacío',
    '  VISITAR(s)            // marca s y encola sus aristas',
    '  mientras Q ≠ ∅:',
    '    (u,v) ← Q.extraerMin()                O(log m)',
    '    si enArbol[u] y enArbol[v]: continuar',
    '    x ← extremo fuera del árbol',
    '    T ← T ∪ {(u,v)} ; VISITAR(x)',
    '  retornar T'
  ]),
  H2('E. Especificación formal (notación AS&ST)'),
  P('La Tabla I especifica cada operación con sus tipos de entrada y salida, su precondición y su poscondición. Los tipos son: *ℕₙ = {0, …, n − 1}*, *𝔹 = {verdadero, falso}* y *Arista = ℕₙ × ℕₙ × ℝ⁺*.'),
  ...tituloTabla('I', 'Especificación de las operaciones'),
  tabla(['Operación', 'Firma', 'Pre / Post'], [
    ['`UnionFind(n)`', 'ℕ → UF', 'Pre: n ≥ 0. Post: n conjuntos {i}; padre[i] = i.'],
    ['`find(x)`', 'UF × ℕₙ → ℕₙ', 'Pre: 0 ≤ x < n. Post: retorna la raíz de x; el camino queda comprimido.'],
    ['`union(a,b)`', 'UF × ℕₙ × ℕₙ → 𝔹', 'Post: falso si find(a) = find(b); si no, une los conjuntos y grupos−1.'],
    ['`Kruskal.calcular(G)`', 'Grafo → ResultadoMST', 'Pre: w(e) > 0. Post: T acíclico, |T| = n − k y w(T) mínimo.'],
    ['`Prim.calcular(G,s)`', 'Grafo × ℕₙ → ResultadoMST', 'Pre: s ∈ V. Post: igual que Kruskal; w(T) idéntico.'],
  ], [1300, 1350, 2043]),
  H2('F. Análisis de complejidad asintótica'),
  P('La Tabla II resume las cotas. En Kruskal domina el ordenamiento. Java usa TimSort, que es *Θ(m log m)* en general pero *O(m)* si los tramos ya vienen ordenados, de ahí la cota inferior *Ω(m)*. Como *m ≤ n²*, se cumple *log m ≤ 2 log n*, así que *O(m log m) = O(m log n)*. En Prim perezoso, cada arista entra y sale de la cola a lo sumo dos veces, con un costo de *O(log m)* cada vez. Ambos requieren al menos leer todas las aristas, por lo que *Ω(m)* es la cota inferior.'),
  ...tituloTabla('II', 'Complejidad en el mejor, promedio y peor caso'),
  tabla(['Operación', 'Mejor Ω', 'Promedio Θ', 'Peor O', 'Memoria'], [
    ['find / union', 'Ω(1)', 'Θ(α(n)) amort.', 'O(log n)', 'Θ(n)'],
    ['Kruskal', 'Ω(m)', 'Θ(m log n)', 'O(m log n)', 'Θ(n + m)'],
    ['Prim (heap)', 'Ω(m)', 'Θ(m log n)', 'O(m log n)', 'Θ(n + m)'],
    ['UF ingenuo (m ops)', 'Ω(m)', '—', 'O(m · n)', 'Θ(n)'],
  ], [1213, 700, 1100, 880, 800]),

  H1('III. Diseño de la solución e implementación'),
  H2('A. Arquitectura modular'),
  P('El sistema está escrito en Java 11 o superior, solo con la librería estándar, y se compila con `javac`. La Fig. 1 muestra sus paquetes. La regla de diseño es que el **motor** (UnionFind, Kruskal y Prim) no conoce la consola ni los archivos: recibe un `Grafo` y devuelve un `ResultadoMST`. Así se puede probar y medir por separado.'),
  ...figura('fig_arq.png', 1, 'Arquitectura por paquetes de FibraNet. El paquete motor (sombreado) contiene los algoritmos; Main solo pide datos y muestra tablas.'),
  B('**modelo:** `Arista` (origen, destino, costo; ordenable por costo) y `Grafo` (lista de aristas para Kruskal y lista de adyacencia para Prim).'),
  B('**motor:** `UnionFind`, `Kruskal`, `Prim`, `ResultadoMST` y `UnionFindIngenuo` (solo para comparar).'),
  B('**io:** `LectorCsv` valida cada fila y reporta las inválidas con su número de línea; `EscritorCsv` exporta el plan; `Tabla` imprime tablas alineadas.'),
  B('**pruebas** y **benchmark:** carpetas de código fuente aparte que usan el mismo motor.'),
  H2('B. Gestión de memoria y estructuras auxiliares'),
  P('Las sedes se identifican con enteros *0 … n − 1*, de modo que Union-Find usa dos arreglos contiguos `int[] padre` y `int[] rango` (8n bytes) sin objetos intermedios, lo que favorece la caché. Cada arista es un único objeto `Arista`; la lista de aristas y las dos listas de adyacencia de sus extremos guardan *referencias* (punteros administrados por la JVM) al mismo objeto, sin copiarlo. Kruskal copia solo las referencias para ordenarlas (*Θ(m)* extra) y Prim usa un arreglo `boolean[] enArbol` de *n* posiciones y una `PriorityQueue` de referencias. La memoria total es *Θ(n + m)*; la columna “Memoria” de la Tabla IV la mide con `Runtime`. `find` es recursivo, pero gracias a la unión por rango su profundidad es como máximo *log₂ n* (unos 20 niveles para un millón de sedes), por lo que no hay riesgo de desbordar la pila.'),
  H2('C. Entrada, salida y validación'),
  P('El menú permite cargar la red desde CSV (`origen,destino,costo`, separado por coma o punto y coma) o por teclado. Se descartan, con su número de línea, las filas con costo no numérico o no positivo, lazos o columnas incompletas. Kruskal y Prim pueden mostrar su paso a paso: en Kruskal, cada tramo evaluado con la decisión **ACEPTADO** o **RECHAZADO (ciclo)**; en Prim, cada sede que entra al árbol. Si la red no es conexa, se listan los grupos de sedes separados.'),
  H2('D. Repositorio'),
  new Paragraph({ alignment: AlignmentType.JUSTIFIED, spacing: { after: 80 }, indent: { firstLine: 202 }, children: [
    ...runs('El código fuente, los datos de ejemplo y las instrucciones están en '),
    new ExternalHyperlink({ link: 'https://github.com/HitLP38/Algoritmia_trabajoF', children: [
      new TextRun({ text: 'github.com/HitLP38/Algoritmia_trabajoF', font: FONT, size: 20, color: '1F4E79', underline: {} })] }),
    ...runs(', carpeta `5a_FibraNet/06_codigo`.')
  ] }),

  H1('IV. Resultados experimentales y discusión'),
  H2('A. Verificación de la corrección'),
  P('Las 31 verificaciones automáticas (`java -cp out fibranet.pruebas.Pruebas`) pasaron. Incluyen la red de 6 sedes resuelta a mano (costo 17, y A–B rechazada por ciclo), una red no conexa (dos grupos, costo 8), la validación de filas, la lectura del CSV de ejemplo, que Kruskal y Prim den el mismo costo en 200 redes aleatorias, y que el plan de Prim sobre 500 sedes tenga *n − 1* tramos sin ciclos.'),
  H2('B. Caso de estudio: 12 sedes de Lima'),
  P('La Fig. 2 muestra la red de ejemplo (`datos/red_ejemplo.csv`): 12 sedes y 18 tramos posibles que costarían 137 mil soles en total. Kruskal y Prim (este último desde cualquier sede inicial) eligen los mismos 11 tramos, con un costo de **61 mil soles**, es decir, un **ahorro de 55,5 %**. Kruskal rechaza tres tramos por formar ciclo: Surco–Chorrillos (6), Sede Central–Miraflores (7) y San Isidro–Surco (9).'),
  ...figura('fig_red.png', 2, 'Red de ejemplo de FibraNet. Líneas gruesas: plan de tendido mínimo (61); líneas discontinuas: tramos descartados. Costos en miles de soles.'),
  H2('C. Metodología del benchmark'),
  P('La clase `Benchmark` genera redes conexas aleatorias con semilla fija (2026): primero un árbol aleatorio y luego tramos al azar hasta completar *m*, con costos enteros entre 1 y 1 000. Se usan dos densidades: dispersa (*m = 4n*) y densa (*m ≈ n log₂ n*). Cada medición descarta 2 corridas de calentamiento de la JVM y promedia 5. Para Union-Find se usa la secuencia que es el peor caso de la versión ingenua: `union(i, i+1)` para todo *i*, seguida de *n* consultas `find(0)`. Para contrastar con la teoría se divide el tiempo entre la cota (*m log₂ n* o el número de operaciones): si la razón se mantiene casi constante al crecer *n*, la medición confirma la cota.'),
  ...tituloTabla('III', 'Entorno de ejecución'),
  tabla(['Elemento', 'Valor'], [
    ['Procesador', 'Intel Core i3-1005G1 @ 1,20 GHz'],
    ['Memoria RAM', '16 GB'],
    ['Sistema operativo', '[[completar: Windows 10/11]]'],
    ['Java', 'OpenJDK 21.0.12.1 (Eclipse Temurin)'],
    ['Datos preliminares', 'Linux, OpenJDK 21, 4 núcleos (entorno de desarrollo)'],
  ], [1500, 3193]),
  P('[[Nota: las Tablas IV y V y las Figs. 3 y 4 tienen datos preliminares del entorno de desarrollo. Se reemplazarán por la salida de java -cp out fibranet.benchmark.Benchmark en la PC del grupo.]]', { noIndent: true }),
  ...tituloTabla('IV', 'Kruskal vs Prim (ms, promedio de 5 corridas)'),
  tabla(['Dens.', 'n', 'm', 'Kruskal', 'Prim', 'K / (m log n)', 'P / (m log n)'], [
    ['disp.', '1 000', '4 000', '1,08', '1,29', '27,0', '32,4'],
    ['disp.', '5 000', '20 000', '3,36', '3,64', '13,7', '14,8'],
    ['disp.', '10 000', '40 000', '7,58', '8,54', '14,3', '16,1'],
    ['disp.', '20 000', '80 000', '11,31', '19,04', '9,9', '16,7'],
    ['disp.', '50 000', '200 000', '30,68', '67,21', '9,8', '21,5'],
    ['densa', '1 000', '9 965', '1,44', '1,64', '14,5', '16,5'],
    ['densa', '5 000', '61 438', '8,02', '13,82', '10,6', '18,3'],
    ['densa', '10 000', '132 877', '18,37', '42,47', '10,4', '24,1'],
    ['densa', '20 000', '285 754', '67,27', '120,64', '16,5', '29,5'],
    ['densa', '50 000', '780 482', '157,87', '485,85', '13,0', '39,9'],
  ], [560, 640, 760, 650, 650, 718, 715]),
  ...figura('fig_mst.png', 3, 'Tiempo de Kruskal y Prim frente a m·log₂n. Una recta indica crecimiento proporcional a la cota teórica.'),
  ...tituloTabla('V', 'Union-Find optimizado vs ingenuo (peor caso del ingenuo)'),
  tabla(['n', 'Operaciones', 'Optim. (ms)', 'Ingenuo (ms)', 'Optim. ns/op', 'Ingenuo ns/op'], [
    ['1 000', '1 999', '0,031', '1,35', '15,6', '675,5'],
    ['2 000', '3 999', '0,061', '5,29', '15,4', '1 323,4'],
    ['5 000', '9 999', '0,042', '32,27', '4,2', '3 226,9'],
    ['10 000', '19 999', '0,071', '131,68', '3,5', '6 584,2'],
    ['20 000', '39 999', '0,147', '509,66', '3,7', '12 741,9'],
    ['100 000', '199 999', '0,757', '—', '3,8', '—'],
    ['1 000 000', '1 999 999', '10,904', '—', '5,5', '—'],
  ], [760, 830, 740, 800, 760, 803]),
  ...figura('fig_uf.png', 4, 'Costo por operación de Union-Find en escala logarítmica. El ingenuo crece en proporción a n; el optimizado se mantiene casi constante, como predice α(n).'),
  H2('D. Discusión: contraste con la teoría'),
  P('**Kruskal.** La razón tiempo / (*m log₂ n*) baja de 27 a cerca de 10 ns al pasar de 1 000 a 20 000 sedes y luego se estabiliza entre 10 y 16 ns. La caída inicial se debe al compilador JIT y a costos fijos que pesan más en redes pequeñas. La estabilidad posterior es la que confirma el crecimiento *Θ(m log n)*.'),
  P('**Prim.** También crece de forma cercana a *m log n*, pero su razón sube de 15 a 40 ns en redes densas grandes. La causa no es asintótica sino de memoria: la cola de prioridad y las listas de adyacencia guardan referencias a objetos dispersos en el heap, lo que provoca fallos de caché. Kruskal, en cambio, ordena una sola lista y recorre `int[]` contiguos. Por eso, en este entorno, Kruskal fue entre 1,2 y 3,1 veces más rápido. La teoría dice que ambos son *O(m log n)*; la constante oculta favorece a Kruskal en grafos densos.'),
  P('**Union-Find.** La versión ingenua duplica su costo por operación cada vez que *n* se duplica (675 → 1 323 → … → 12 742 ns). Es el comportamiento lineal por operación y cuadrático en total, *O(m · n)*. La versión optimizada se mantiene entre 3,5 y 5,5 ns por operación desde mil hasta un millón de elementos, lo que es consistente con *O(α(n))*: con *n = 20 000* es unas 3 500 veces más rápida.'),
  P('**Memoria.** El heap usado creció de forma lineal con *n + m* (de 1,6 MB a 44,6 MB en la red densa de 780 482 tramos), lo que concuerda con *Θ(n + m)*.'),

  H1('V. Conclusiones y trabajo futuro'),
  N('El problema de FibraNet se modeló como un bosque de expansión mínima, y Kruskal y Prim lo resolvieron con el mismo costo en todas las pruebas. En la red de ejemplo, el plan reduce el costo de tendido en 55,5 %.', 'numeros2'),
  N('Las mediciones confirman las cotas *O(m log n)* de ambos algoritmos: las razones tiempo / cota se mantienen acotadas. Kruskal tuvo una constante menor gracias a un acceso a memoria más localizado.', 'numeros2'),
  N('La compresión de caminos y la unión por rango convierten una estructura que en el peor caso cuesta *O(n)* por operación en una de costo prácticamente constante, sin cambiar la idea del algoritmo.', 'numeros2'),
  N('Separar el motor de la entrada y salida permitió probarlo y medirlo de forma independiente. Además, el código puede explicarse por partes: modelo, motor e interfaz.', 'numeros2'),
  P('**Trabajo futuro:** (a) agregar restricciones reales de la fibra, como capacidad, distancia máxima entre repetidores y redundancia (2-conexidad); (b) implementar Prim con montículo de Fibonacci, *O(m + n log n)*, y comparar; (c) usar el algoritmo de Borůvka en paralelo para redes muy grandes; (d) leer coordenadas geográficas reales para calcular costos.'),

  H1('Referencias'),
  ...[
    '[1] T. H. Cormen, C. E. Leiserson, R. L. Rivest y C. Stein, *Introduction to Algorithms*, 4.ª ed. Cambridge, MA, EE. UU.: MIT Press, 2022.',
    '[2] J. B. Kruskal, “On the shortest spanning subtree of a graph and the traveling salesman problem,” *Proc. Amer. Math. Soc.*, vol. 7, n.º 1, pp. 48–50, 1956.',
    '[3] R. C. Prim, “Shortest connection networks and some generalizations,” *Bell Syst. Tech. J.*, vol. 36, n.º 6, pp. 1389–1401, 1957.',
    '[4] R. E. Tarjan, “Efficiency of a good but not linear set union algorithm,” *J. ACM*, vol. 22, n.º 2, pp. 215–225, 1975.',
    '[5] R. E. Tarjan y J. van Leeuwen, “Worst-case analysis of set union algorithms,” *J. ACM*, vol. 31, n.º 2, pp. 245–281, 1984.',
    '[6] B. A. Galler y M. J. Fischer, “An improved equivalence algorithm,” *Commun. ACM*, vol. 7, n.º 5, pp. 301–303, 1964.',
    '[7] R. Sedgewick y K. Wayne, *Algorithms*, 4.ª ed. Boston, MA, EE. UU.: Addison-Wesley, 2011.',
  ].map(r => new Paragraph({ alignment: AlignmentType.JUSTIFIED, spacing: { after: 40 }, indent: { left: 300, hanging: 300 }, children: runs(r, { size: 16 }) })),

  H1('Anexos'),
  P('**A. Recursos del proyecto.** Repositorio: github.com/HitLP38/Algoritmia_trabajoF (rama `main`). Requerimientos: `5a_FibraNet/01_requerimientos/requerimientos_FibraNet.md`. Código: `5a_FibraNet/06_codigo` (se adjunta en formato .zip).', { noIndent: true }),
  P('**B. Cómo ejecutar** (desde `5a_FibraNet/06_codigo`):', { noIndent: true }),
  ...CODE([
    'javac -encoding UTF-8 -d out (Get-ChildItem -Recurse',
    '  -Filter *.java -Path src,pruebas,benchmark).FullName',
    'java -cp out fibranet.pruebas.Pruebas',
    'java -cp out fibranet.Main',
    'java -cp out fibranet.benchmark.Benchmark'
  ]),
];

// ------------------------------------------------------------------
const pie = new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER,
  children: [new TextRun({ children: [PageNumber.CURRENT], font: FONT, size: 16 })] })] });
const encabezado = new Header({ children: [new Paragraph({ alignment: AlignmentType.RIGHT,
  children: [new TextRun({ text: 'SW305 · Grupo 5 · FibraNet', font: FONT, size: 14, italics: true })] })] });
const pagina = { size: { width: 11906, height: 16838 }, margin: { top: 1080, bottom: 1080, left: 1080, right: 1080 } };

const doc = new Document({
  creator: 'Grupo 5 SW305', title: 'FibraNet: red de fibra óptica de costo mínimo',
  styles: { default: { document: { run: { font: FONT, size: 20 } } } },
  numbering: { config: [
    { reference: 'vinetas', levels: [{ level: 0, format: LevelFormat.BULLET, text: '•', alignment: AlignmentType.LEFT,
      style: { paragraph: { indent: { left: 360, hanging: 200 } } } }] },
    { reference: 'numeros2', levels: [{ level: 0, format: LevelFormat.DECIMAL, text: '%1)', alignment: AlignmentType.LEFT,
      style: { paragraph: { indent: { left: 360, hanging: 260 } } } }] },
    { reference: 'numeros', levels: [{ level: 0, format: LevelFormat.DECIMAL, text: '%1)', alignment: AlignmentType.LEFT,
      style: { paragraph: { indent: { left: 360, hanging: 260 } } } }] },
  ] },
  sections: [
    { properties: { page: pagina }, children: caratula },
    { properties: { page: pagina, type: SectionType.NEXT_PAGE, column: { space: 360, count: 2 } },
      headers: { default: encabezado }, footers: { default: pie }, children: cuerpo },
  ]
});
// numeración decimal propia para cada lista N(): se reinicia por bloque usando instancias
Packer.toBuffer(doc).then(b => {
  fs.writeFileSync(process.argv[2], b);
  console.log('ok', process.argv[2]);
});
