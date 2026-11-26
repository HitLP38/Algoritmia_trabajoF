// Genera el informe UNI/IEEE de RedLuz (Tema 5.b). Uso: node informe_5b.js salida.docx
const fs = require('fs');
const path = require('path');
const {
  Document, Packer, Paragraph, TextRun, ImageRun, Table, TableRow, TableCell,
  AlignmentType, WidthType, BorderStyle, ShadingType, SectionType, LevelFormat,
  PageNumber, Footer, Header, ExternalHyperlink, VerticalAlign
} = require('docx');

const FONT = 'Times New Roman';
const COL = 4693;            // ancho de columna en DXA (A4, márgenes 0.75", separación 0.25")
const FIG = process.env.FIG || '/tmp/claude-0/inf5b/';

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
  const buf = fs.readFileSync(path.join(FIG, archivo));
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
  C('**RedLuz: conectividad dinámica de una red eléctrica de distribución con Link-Cut Trees**', 32),
  new Paragraph({ spacing: { after: 300 }, children: [] }),
  C('Tema 5.b: Grafos dinámicos y estructuras de datos avanzadas (Link-Cut Trees)', 22),
  C('Curso: Algoritmos Avanzados y Algoritmos Avanzados de Grafos (SW305)', 22),
  C('Semestre académico 2026-2', 22),
  new Paragraph({ spacing: { after: 300 }, children: [] }),
  C('**Integrantes (Grupo 5)**', 22),
  tabla(['Apellidos y nombres', 'Código'], integrantes.map(n => [n, '[[20XXXXXXX]]']), [4800, 1800]),
  new Paragraph({ spacing: { after: 300 }, children: [] }),
  C('**Docente:** Ing. Jorge Medianero Acosta', 22),
  C('**Fecha de entrega:** jueves 10 de diciembre de 2026', 22),
  C('Lima, Perú', 22),
];

// ------------------------------------------------------------------
// CUERPO
// ------------------------------------------------------------------
const cuerpo = [
  new Paragraph({ alignment: AlignmentType.JUSTIFIED, spacing: { after: 120 }, children: [
    new TextRun({ text: 'Resumen', font: FONT, size: 18, bold: true, italics: true }),
    ...runs('—Una empresa de distribución eléctrica necesita saber, después de cada corte o reconexión de un tramo de su red, qué clientes siguen con luz y por qué ruta les llega la energía. Recalcular la red completa con un recorrido en anchura (BFS) cuesta O(n) por consulta, lo que no escala a redes con cientos de miles de elementos y miles de eventos. Se modeló la red como un bosque radial con un vértice virtual FUENTE unido a las subestaciones, de modo que un cliente tiene luz si y solo si está conectado con FUENTE. La estructura elegida es el Link-Cut Tree de Sleator y Tarjan, implementado en Java sin recursión, con árboles splay, punteros path-parent, inversión perezosa y agregados por camino (km y número de tramos). Las operaciones link, cut, connected y la consulta de ruta cuestan O(log n) amortizado. La corrección se verificó con 45 pruebas automáticas, entre ellas 20 000 operaciones aleatorias comparadas con BFS y un alimentador de 100 000 nodos en línea. En el benchmark, con N nodos y M = N operaciones mezcladas, el Link-Cut Tree procesó 100 000 operaciones en 0,34 s y fue 209 veces más rápido que BFS con N = 5 000. El tiempo normalizado por M log N se mantuvo entre 143 y 202 ns, coherente con la cota teórica.', { size: 18, bold: true })
  ] }),
  new Paragraph({ alignment: AlignmentType.JUSTIFIED, spacing: { after: 160 }, children: [
    new TextRun({ text: 'Palabras clave', font: FONT, size: 18, bold: true, italics: true }),
    ...runs('—grafos dinámicos, Link-Cut Tree, árbol splay, conectividad dinámica, análisis amortizado, redes eléctricas, benchmarking.', { size: 18, bold: true })
  ] }),

  H1('I. Introducción y motivación'),
  P('Una red eléctrica de distribución en media y baja tensión opera de forma **radial**: cada cliente recibe energía por un solo camino desde una subestación. La red cambia todo el tiempo. Se abren tramos por fallas, por mantenimiento o por cortes programados, y se cierran otros para transferir carga entre alimentadores. Después de cada cambio, el centro de control debe responder rápido dos preguntas: ¿este cliente tiene luz? y, si la tiene, ¿por qué ruta y a qué distancia de su subestación?'),
  P('[[Caso de estudio: pendiente de la decisión del grupo (red de ejemplo actual o red real de un distrito de Lima con cortes programados publicados por la distribuidora). Aquí irán dos o tres oraciones que presentan el caso concreto, su tamaño y la fuente de los datos.]]'),
  P('La solución directa es recorrer la red con BFS en cada consulta, con costo *O(n)*. Con *n* del orden de 10⁵ elementos y miles de eventos por hora, ese costo se multiplica. El tema 5.b propone una estructura que mantiene un **bosque dinámico** y responde conectividad y agregados de camino en *O(log n)* amortizado: el Link-Cut Tree [1].'),
  H2('A. Objetivo general'),
  P('Diseñar, implementar y evaluar un sistema por consola que mantenga el estado de una red eléctrica radial ante conexiones y cortes de tramos, y que responda si un cliente tiene luz y cuál es su ruta de alimentación, usando Link-Cut Trees.'),
  H2('B. Objetivos específicos'),
  N('Modelar la red como un bosque dinámico y reducir la pregunta "¿tiene luz?" a una consulta de conectividad.'),
  N('Implementar el Link-Cut Tree (splay, access, makeRoot, findRoot, link, cut) sin recursión, con agregados por camino y código comentado.'),
  N('Verificar la corrección contra una versión ingenua basada en BFS con miles de operaciones aleatorias.'),
  N('Medir el tiempo para tamaños crecientes y contrastarlo con *O(log n)* amortizado frente a *O(n)* por consulta.'),

  H1('II. Marco teórico y especificación algorítmica'),
  H2('A. Conectividad dinámica en bosques'),
  P('Sea *F = (V, E)* un bosque que cambia en el tiempo. El problema de **conectividad dinámica** pide soportar `link(u, v)` (agregar la arista si *u* y *v* están en árboles distintos), `cut(u, v)` (quitar una arista existente) y `connected(u, v)`, intercaladas en cualquier orden. Union-Find resuelve solo uniones; no permite cortar. Los Link-Cut Trees resuelven las tres operaciones y además mantienen **agregados de camino**, como la suma de pesos entre dos vértices [1], [2].'),
  H2('B. Árboles splay'),
  P('Un árbol splay es un árbol binario de búsqueda autoajustable. La operación `splay(x)` sube a *x* hasta la raíz con rotaciones zig, zig-zig y zig-zag. Ninguna operación aislada está acotada por *O(log n)*, pero el análisis amortizado con la función potencial *Φ = Σ log₂ tamaño(x)* prueba que *m* operaciones cuestan *O(m log n)* [3].'),
  H2('C. Link-Cut Tree'),
  P('El árbol real se descompone en **caminos preferidos**. Cada camino se guarda en un árbol splay ordenado por profundidad: a la izquierda los nodos más cercanos a la raíz. La raíz de cada splay guarda un puntero **path-parent** hacia el nodo del que cuelga su camino. La operación central es `access(x)`, que reorganiza los caminos para que el camino preferido vaya de la raíz del árbol real hasta *x*, y deja a *x* en la raíz de su splay:'),
  ...CODE([
    'ACCESS(x):',
    '  y ← nulo',
    '  para z ← x; z ≠ nulo; z ← z.padre:',
    '    SPLAY(z)',
    '    z.der ← y ; ACTUALIZAR(z)   // pega el camino de abajo',
    '    y ← z',
    '  SPLAY(x)',
  ]),
  P('Sobre `access` se construye el resto: `makeRoot(x)` hace `access(x)` e invierte el camino con una marca perezosa; `findRoot(x)` baja por la izquierda del splay; `link(x, y)` hace `makeRoot(x)` y cuelga *x* de *y*; `cut(x, y)` hace `makeRoot(x)`, `access(y)` y separa a *x*, que debe ser el hijo izquierdo de *y*. Tras `makeRoot(x)` y `access(y)`, el splay de *y* contiene exactamente el camino *x → y*, y los campos agregados de *y* (suma de km, número de tramos) son la respuesta de la consulta.'),
  P('El costo amortizado de `access` es *O(log n)*: el número de cambios de camino preferido es *O(log n)* amortizado (argumento de aristas pesadas y livianas) y cada splay agrega *O(log n)* amortizado por el teorema del acceso [1], [3].'),
  H2('D. Especificación formal (notación AS&ST)'),
  P('La Tabla I especifica las operaciones públicas. Sea *𝔽* el conjunto de bosques sobre los nodos *𝒩*, y *𝔹 = {verdadero, falso}*.'),
  ...tituloTabla('I', 'Especificación de las operaciones del Link-Cut Tree'),
  tabla(['Operación', 'Firma', 'Pre / Post'], [
    ['`link(x,y)`', '𝔽 × 𝒩 × 𝒩 → 𝔹', 'Post: si x e y estaban en árboles distintos, agrega (x,y) y retorna verdadero; si no, el bosque no cambia y retorna falso (anillo).'],
    ['`cut(x,y)`', '𝔽 × 𝒩 × 𝒩 → 𝔹', 'Post: si (x,y) ∈ E la quita y retorna verdadero; si no, retorna falso.'],
    ['`connected(x,y)`', '𝔽 × 𝒩 × 𝒩 → 𝔹', 'Post: verdadero ⇔ existe un camino x ⇝ y. El bosque lógico no cambia.'],
    ['`findRoot(x)`', '𝔽 × 𝒩 → 𝒩', 'Post: retorna la raíz del árbol de x.'],
    ['`camino(x,y)`', '𝔽 × 𝒩 × 𝒩 → 𝒩', 'Pre: connected(x,y). Post: el nodo retornado agrega Σkm y Σtramos del camino x ⇝ y.'],
  ], [1150, 1250, 2293]),
  H2('E. Análisis de complejidad asintótica'),
  P('La Tabla II resume las cotas con *n* nodos. En el mejor caso, si *x* ya está en la raíz de su splay y su camino es el preferido, `access` cuesta *Ω(1)*. Una operación aislada puede costar *O(n)* (un splay sobre un camino largo), pero el costo **amortizado** es *O(log n)*. La versión ingenua guarda listas de adyacencia: conectar y cortar son *O(1)* (más *O(n)* para rechazar anillos), pero cada consulta recorre el componente con BFS.'),
  ...tituloTabla('II', 'Complejidad en el mejor, promedio y peor caso'),
  tabla(['Operación', 'Mejor Ω', 'Amortizado Θ', 'Peor O (una op.)', 'Memoria'], [
    ['access / splay', 'Ω(1)', 'Θ(log n)', 'O(n)', 'Θ(n)'],
    ['link / cut', 'Ω(1)', 'Θ(log n)', 'O(n)', '—'],
    ['connected / ruta', 'Ω(1)', 'Θ(log n)', 'O(n)', '—'],
    ['M operaciones LCT', 'Ω(M)', 'Θ(M log n)', 'O(M log n)', 'Θ(n)'],
    ['BFS (M consultas)', 'Ω(M)', 'Θ(M · n)', 'O(M · n)', 'Θ(n)'],
  ], [1213, 650, 1000, 1030, 800]),

  H1('III. Diseño de la solución e implementación'),
  H2('A. Modelo de la red'),
  P('La Fig. 1 muestra el modelo. Se agrega un vértice virtual **FUENTE** unido con aristas de 0 km a cada subestación. Así, un cliente tiene luz si y solo si `connected(cliente, FUENTE)`. Para sumar kilómetros sobre un camino, cada tramo eléctrico se representa como un **nodo-tramo** con su longitud y un contador de tramos igual a 1, intercalado entre sus dos extremos. Los nodos físicos (subestaciones, postes y clientes) aportan 0 km. Si un tramo nuevo uniría dos nodos que ya están en el mismo árbol, se rechaza porque cerraría un anillo. Esto incluye el caso de unir dos zonas que tienen subestación, ya que ambas cuelgan de FUENTE. Así la red se mantiene radial.'),
  ...figura('fig_modelo.png', 1, 'Modelo de RedLuz. FUENTE se une a las subestaciones S1 y S2; cada tramo es un nodo con sus km. C1 tiene luz por S1–P1–C1; C2 perdió la luz al cortarse P3–C2.'),
  H2('B. Arquitectura modular'),
  P('El sistema está escrito en Java 11 o superior, solo con la librería estándar (Fig. 2). El **motor** (`LinkCutTree`, `Nodo`) no sabe nada de electricidad. La clase `RedElectrica` traduce clientes y tramos a operaciones del motor e implementa la interfaz `RedDinamica`, la misma que implementa `RedIngenua` con BFS. Gracias a esa interfaz, las pruebas y el benchmark ejecutan exactamente las mismas operaciones sobre las dos versiones.'),
  ...figura('fig_arq.png', 2, 'Arquitectura por paquetes de RedLuz. El paquete motor (sombreado) contiene el Link-Cut Tree.'),
  H2('C. Gestión de memoria'),
  P('Cada `Nodo` es un objeto con tres referencias (hijo izquierdo, hijo derecho y padre, que también hace de path-parent), la marca `invertido` y los agregados `sumaKm`, `sumaTramos` y `maxMarca`. El espacio total es *Θ(n)* con *n = |nodos físicos| + |tramos| + |subestaciones| + 1*. Las referencias de Java cumplen el papel de los punteros de C++: no hay arreglos paralelos ni copias de subárboles. Para soportar alimentadores de 100 000 nodos en línea, `splay` empuja las marcas perezosas de arriba hacia abajo con una pila explícita y ninguna operación usa recursión. Así se evita el desbordamiento de la pila de la JVM.'),
  H2('D. Entrada, salida y repositorio'),
  new Paragraph({ alignment: AlignmentType.JUSTIFIED, spacing: { after: 80 }, indent: { firstLine: 202 }, children: [
    ...runs('El menú (opciones 0 a 11) carga nodos y tramos desde CSV, conecta y corta tramos, consulta luz, ruta y zona, procesa un archivo de eventos en lote (`CONECTAR`, `CORTAR`, `LUZ`, `RUTA`, `ZONA`) y puede mostrar la traza del motor (accesos y splays). Las filas inválidas se informan con su número de línea y ningún error detiene el programa. Código y datos: '),
    new ExternalHyperlink({ link: 'https://github.com/HitLP38/Algoritmia_trabajoF', children: [
      new TextRun({ text: 'github.com/HitLP38/Algoritmia_trabajoF', font: FONT, size: 20, color: '1F4E79', underline: {} })] }),
    ...runs(', carpeta `5b_RedLuz/06_codigo`.')
  ] }),

  H1('IV. Resultados experimentales y discusión'),
  H2('A. Verificación de la corrección'),
  P('Las 45 verificaciones automáticas (`java -cp out redluz.pruebas.Pruebas`) pasaron. Cubren link, cut y connected sobre casos pequeños, el rechazo de anillos, la suma de km sobre un camino, una red de ejemplo resuelta a mano, las validaciones y la lectura de los CSV. La prueba más fuerte aplica **20 000 operaciones aleatorias** a la vez sobre `RedElectrica` y `RedIngenua` y exige el mismo resultado en cada una, incluida la ruta en km. Otra prueba arma un alimentador de 100 000 nodos en línea, el peor caso de profundidad, sin desbordar la pila.'),
  H2('B. Caso de estudio'),
  P('[[Pendiente de la decisión del grupo: figura de la red del caso, tabla de eventos (cortes y reconexiones) con el resultado de cada consulta LUZ y RUTA, y comentario de los resultados.]]'),
  H2('C. Metodología del benchmark'),
  P('La clase `Benchmark` construye una red con una subestación y *N − 1* nodos, donde cada nodo nuevo se une a uno de los 5 anteriores. Así resulta un árbol **profundo**, como un alimentador largo, que es el caso difícil para BFS. Luego aplica *M = N* operaciones generadas con semilla fija (2026), en el mismo orden para ambas versiones: 40 % consultas LUZ, 30 % CORTAR un tramo activo y 30 % CONECTAR un par al azar (rechazado si cerraría un anillo). Cada medición es el promedio de 3 corridas tras 1 de calentamiento. BFS se mide hasta *N = 5 000* porque su costo cuadrático hace inviables tamaños mayores. El entorno está en la Tabla III.'),
  ...tituloTabla('III', 'Entorno de ejecución'),
  tabla(['Elemento', 'Valor'], [
    ['Procesador', 'Intel Core i3-1005G1 @ 1,20 GHz'],
    ['Memoria RAM', '16 GB'],
    ['Sistema operativo', '[[completar: Windows 10/11]]'],
    ['Java', 'OpenJDK 21.0.12.1 (Eclipse Temurin)'],
    ['Datos preliminares', 'Linux, OpenJDK 21, 4 núcleos (entorno de desarrollo)'],
  ], [1500, 3193]),
  P('[[Nota: la Tabla IV y las Figs. 3 y 4 tienen datos preliminares del entorno de desarrollo. Se reemplazarán por la salida de java -cp out redluz.benchmark.Benchmark en la PC del grupo.]]', { noIndent: true }),
  ...tituloTabla('IV', 'Link-Cut Tree vs BFS (M = N operaciones)'),
  tabla(['N', 'LCT (ms)', 'BFS (ms)', 'LCT ns / (M log N)', 'BFS ns / (M·N)', 'BFS / LCT'], [
    ['1 000', '2,73', '44,38', '273,9', '44,4', '16×'],
    ['2 000', '3,41', '176,89', '155,3', '44,2', '52×'],
    ['5 000', '9,02', '1 884,73', '146,8', '75,4', '209×'],
    ['10 000', '18,99', '—', '142,9', '—', '—'],
    ['20 000', '45,98', '—', '160,9', '—', '—'],
    ['50 000', '132,03', '—', '169,2', '—', '—'],
    ['100 000', '336,38', '—', '202,5', '—', '—'],
  ], [700, 700, 800, 900, 850, 743]),
  ...figura('fig_lct.png', 3, 'Tiempo total de M = N operaciones en escala logarítmica. La pendiente de BFS es cercana a 2 (cuadrático en total); la del Link-Cut Tree, cercana a 1.'),
  ...figura('fig_ratio.png', 4, 'Tiempo dividido entre la cota teórica de cada versión. Una razón que se mantiene acotada indica que la cota describe bien la medición.'),
  H2('D. Discusión: contraste con la teoría'),
  P('**Link-Cut Tree.** La razón tiempo / (*M log₂ N*) baja de 274 ns con *N = 1 000*, donde pesan el JIT y los costos fijos, a un rango estable de 143 a 169 ns entre *N = 2 000* y *N = 50 000*. Con *N = 100 000* sube a 202 ns. La causa probable no es asintótica: los nodos son objetos dispersos en el heap y, al crecer la red, aumentan los fallos de caché en cada rotación. El comportamiento es el de *Θ(M log N)* amortizado.'),
  P('**BFS.** El tiempo total se multiplica por unos 4 cada vez que *N* se duplica (44 → 177 ms), lo que corresponde a *Θ(M · N)*: cada consulta recorre el componente completo. La razón tiempo / (*M · N*) se mantiene cerca de 44 ns y sube a 75 ns con *N = 5 000* por el mismo efecto de caché.'),
  P('**Comparación.** La ventaja del Link-Cut Tree crece con *N*: de 16 veces con *N = 1 000* a 209 veces con *N = 5 000*. Extrapolando la cota de BFS, con *N = 100 000* la versión ingenua tardaría del orden de 10 minutos, frente a 0,34 s del Link-Cut Tree. Esto cumple el RNF-04 (100 000 operaciones en menos de 2 s).'),

  H1('V. Conclusiones y trabajo futuro'),
  N('La pregunta "¿el cliente tiene luz?" se redujo a una consulta de conectividad gracias al vértice virtual FUENTE. Representar cada tramo como nodo permitió responder además la ruta en km con los agregados del splay.', 'numeros2'),
  N('El Link-Cut Tree implementado sin recursión soporta conexiones, cortes y consultas en *O(log n)* amortizado, y coincidió con BFS en 20 000 operaciones aleatorias.', 'numeros2'),
  N('Las mediciones confirman la cota: el tiempo normalizado por *M log N* se mantiene acotado, mientras que BFS crece en proporción a *M · N*. Con *N = 5 000* la diferencia ya es de dos órdenes de magnitud.', 'numeros2'),
  N('Separar el motor del dominio eléctrico mediante la interfaz `RedDinamica` permitió probar y medir las dos versiones con las mismas operaciones.', 'numeros2'),
  P('**Trabajo futuro:** (a) soportar redes malladas con *Euler Tour Trees* o conectividad totalmente dinámica de Holm *et al.* [4]; (b) agregar capacidad por tramo y consultar el tramo más cargado del camino con el agregado máximo; (c) reducir fallos de caché guardando los nodos en arreglos paralelos.'),

  H1('Referencias'),
  ...[
    '[1] D. D. Sleator y R. E. Tarjan, “A data structure for dynamic trees,” *J. Comput. Syst. Sci.*, vol. 26, n.º 3, pp. 362–391, 1983.',
    '[2] R. E. Tarjan, *Data Structures and Network Algorithms*. Philadelphia, PA, EE. UU.: SIAM, 1983.',
    '[3] D. D. Sleator y R. E. Tarjan, “Self-adjusting binary search trees,” *J. ACM*, vol. 32, n.º 3, pp. 652–686, 1985.',
    '[4] J. Holm, K. de Lichtenberg y M. Thorup, “Poly-logarithmic deterministic fully-dynamic algorithms for connectivity, minimum spanning tree, 2-edge, and biconnectivity,” *J. ACM*, vol. 48, n.º 4, pp. 723–760, 2001.',
    '[5] T. H. Cormen, C. E. Leiserson, R. L. Rivest y C. Stein, *Introduction to Algorithms*, 4.ª ed. Cambridge, MA, EE. UU.: MIT Press, 2022.',
    '[6] E. D. Demaine, “Dynamic graphs,” *MIT 6.851: Advanced Data Structures*, lecture notes, 2012.',
  ].map(r => new Paragraph({ alignment: AlignmentType.JUSTIFIED, spacing: { after: 40 }, indent: { left: 300, hanging: 300 }, children: runs(r, { size: 16 }) })),

  H1('Anexos'),
  P('**A. Recursos del proyecto.** Repositorio: github.com/HitLP38/Algoritmia_trabajoF (rama `main`). Requerimientos: `5b_RedLuz/01_requerimientos/requerimientos_RedLuz.md`. Código: `5b_RedLuz/06_codigo` (se adjunta en formato .zip).', { noIndent: true }),
  P('**B. Cómo ejecutar** (desde `5b_RedLuz/06_codigo`):', { noIndent: true }),
  ...CODE([
    'javac -encoding UTF-8 -d out (Get-ChildItem -Recurse',
    '  -Filter *.java -Path src,pruebas,benchmark).FullName',
    'java -cp out redluz.pruebas.Pruebas',
    'java -cp out redluz.Main',
    'java -cp out redluz.benchmark.Benchmark'
  ]),
];

// ------------------------------------------------------------------
const pie = new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER,
  children: [new TextRun({ children: [PageNumber.CURRENT], font: FONT, size: 16 })] })] });
const encabezado = new Header({ children: [new Paragraph({ alignment: AlignmentType.RIGHT,
  children: [new TextRun({ text: 'SW305 · Grupo 5 · RedLuz', font: FONT, size: 14, italics: true })] })] });
const pagina = { size: { width: 11906, height: 16838 }, margin: { top: 1080, bottom: 1080, left: 1080, right: 1080 } };

const doc = new Document({
  creator: 'Grupo 5 SW305', title: 'RedLuz: conectividad dinámica con Link-Cut Trees',
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
Packer.toBuffer(doc).then(b => {
  fs.writeFileSync(process.argv[2], b);
  console.log('ok', process.argv[2]);
});
