/* =====================================================================
   Simuladores de FibraNet: Union-Find, Kruskal y Prim paso a paso.
   Usan la misma lógica que el código Java (unión por rango, compresión
   de caminos, orden estable de tramos y Prim perezoso).
   ===================================================================== */

/* ---------- control común: Reiniciar / Atrás / Paso / Todo ---------- */
function montarSim(box, pasos, dibujar) {
  let i = 0;
  const bar = box.querySelector(".bar");
  bar.innerHTML = `<button data-a="r">⟲ Reiniciar</button><button data-a="b">◀ Atrás</button><button data-a="s">Paso ▶</button><button data-a="e">Todo ▶▶</button><span class="cnt"></span>`;
  const draw = () => { dibujar(pasos[i], i); bar.querySelector(".cnt").textContent = `paso ${i} / ${pasos.length - 1}`; };
  bar.addEventListener("click", e => {
    const b = e.target.closest("button"); if (!b) return;
    const a = b.dataset.a;
    if (a === "r") i = 0; if (a === "b") i = Math.max(0, i - 1);
    if (a === "s") i = Math.min(pasos.length - 1, i + 1); if (a === "e") i = pasos.length - 1;
    draw();
  });
  draw();
}

/* ---------- Union-Find (igual que UnionFind.java) ---------- */
function ufNuevo(n) { return { padre: [...Array(n).keys()], rango: Array(n).fill(0) }; }
function ufFind(uf, x, camino) {
  if (camino) camino.push(x);
  if (uf.padre[x] !== x) uf.padre[x] = ufFind(uf, uf.padre[x], camino);
  return uf.padre[x];
}
function ufUnion(uf, a, b) {
  const ra = ufFind(uf, a), rb = ufFind(uf, b);
  if (ra === rb) return false;
  if (uf.rango[ra] < uf.rango[rb]) uf.padre[ra] = rb;
  else if (uf.rango[ra] > uf.rango[rb]) uf.padre[rb] = ra;
  else { uf.padre[rb] = ra; uf.rango[ra]++; }
  return true;
}
const copiaUF = uf => ({ padre: [...uf.padre], rango: [...uf.rango] });

/* ---------- 1. Simulador de Union-Find ---------- */
(() => {
  const box = document.getElementById("simUF"); if (!box) return;
  const L = "ABCDEFGH".split(""), id = c => L.indexOf(c);
  const ops = [
    ["union", "A", "B"], ["union", "C", "D"], ["union", "A", "C"], ["union", "E", "F"],
    ["union", "G", "H"], ["union", "F", "H"], ["union", "B", "G"], ["find", "H"], ["find", "D"]
  ];
  const uf = ufNuevo(8);
  const pasos = [{ uf: copiaUF(uf), resalta: [], op: "inicio", nota: "Al inicio cada sede es su propio grupo: padre[i] = i y rango[i] = 0." }];
  ops.forEach(o => {
    if (o[0] === "union") {
      const a = id(o[1]), b = id(o[2]), ra = ufFind(uf, a), rb = ufFind(uf, b);
      const antes = [uf.rango[ra], uf.rango[rb]];
      ufUnion(uf, a, b);
      let nota;
      if (antes[0] === antes[1]) nota = `find(${o[1]}) = ${L[ra]} y find(${o[2]}) = ${L[rb]}, ambos con rango ${antes[0]}: empate, ${L[rb]} cuelga de ${L[ra]} y el rango de ${L[ra]} sube a ${antes[0] + 1}.`;
      else { const alto = antes[0] > antes[1] ? ra : rb, bajo = alto === ra ? rb : ra; nota = `Raíces ${L[ra]} (rango ${antes[0]}) y ${L[rb]} (rango ${antes[1]}): el árbol más bajo, ${L[bajo]}, cuelga del más alto, ${L[alto]}. La altura no crece.`; }
      pasos.push({ uf: copiaUF(uf), resalta: [a, b], op: `union(${o[1]}, ${o[2]})`, nota });
    } else {
      const x = id(o[1]), camino = [], antes = copiaUF(uf);
      ufFind(antes, x, camino);
      pasos.push({ uf: copiaUF(uf), resalta: camino, op: `find(${o[1]}): recorrido`, nota: `find(${o[1]}) sube por ${camino.map(c => L[c]).join(" → ")}: ${camino.length - 1} saltos hasta la raíz.` });
      ufFind(uf, x);
      pasos.push({ uf: copiaUF(uf), resalta: camino, op: `find(${o[1]}): compresión`, nota: `Compresión de caminos: cada nodo del recorrido ahora apunta directo a la raíz ${L[camino[camino.length - 1]]}. La próxima consulta cuesta 1 salto.` });
    }
  });
  const svg = box.querySelector("svg"), tab = box.querySelector("table.regs"), nota = box.querySelector(".note"), op = document.querySelector(".opact");
  montarSim(box, pasos, (p) => {
    const antes = pasos[pasos.indexOf(p) - 1]?.uf;       // celdas cuyo padre cambió respecto del paso anterior
    // dibujo del bosque: raíces en fila, hijos debajo
    const hijos = L.map(() => []); const raices = [];
    p.uf.padre.forEach((q, i) => q === i ? raices.push(i) : hijos[q].push(i));
    const pos = []; let x = 0;
    const ubicar = (v, d) => { if (!hijos[v].length) { pos[v] = [x++, d]; return; } hijos[v].forEach(h => ubicar(h, d + 1)); const xs = hijos[v].map(h => pos[h][0]); pos[v] = [(Math.min(...xs) + Math.max(...xs)) / 2, d]; };
    raices.forEach(r => { ubicar(r, 0); x += 0.6; });
    const W = 860, sx = (W - 120) / Math.max(1, x - 0.6 - 1 + 1), X = v => 60 + pos[v][0] * Math.min(sx, 110), Y = v => 60 + pos[v][1] * 110;
    let g = "";
    p.uf.padre.forEach((q, i) => { if (q !== i) g += `<line x1="${X(i)}" y1="${Y(i)}" x2="${X(q)}" y2="${Y(q)}" class="ufe" marker-end="url(#fl)"/>`; });
    L.forEach((c, i) => {
      const on = p.resalta.includes(i), raiz = p.uf.padre[i] === i;
      g += `<circle cx="${X(i)}" cy="${Y(i)}" r="28" class="ufn ${on ? "on" : ""} ${raiz ? "raiz" : ""}"/><text x="${X(i)}" y="${Y(i) + 9}" class="ufl">${c}</text>`;
    });
    svg.innerHTML = `<defs><marker id="fl" viewBox="0 0 10 10" refX="40" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0 0L10 5L0 10z" fill="#566476"/></marker></defs>` + g;
    tab.innerHTML = `<tr><td>i</td>${L.map(c => `<td>${c}</td>`).join("")}</tr>` +
      `<tr><td>padre</td>${p.uf.padre.map((q, i) => `<td class="${antes && antes.padre[i] !== q ? "chg" : ""}">${L[q]}</td>`).join("")}</tr>` +
      `<tr><td>rango</td>${p.uf.rango.map(r => `<td>${r}</td>`).join("")}</tr>`;
    nota.textContent = p.nota; op.textContent = p.op;
  });
})();

/* ---------- Caso Red Dorsal Nacional ---------- */
const NOMBRES = CAPITALES.map(([nombre]) => nombre === "Huanuco" ? "Huánuco" : nombre);
const rad = grados => grados * Math.PI / 180;
const formatoKm = new Intl.NumberFormat("es-PE", { minimumFractionDigits: 1, maximumFractionDigits: 1 });
const km = valor => formatoKm.format(valor).replace(/,/g, "\u202f").replace(".", ",");
function distancia([, latA, lonA], [, latB, lonB]) {
  const a = Math.sin(rad(latB - latA) / 2) ** 2
    + Math.cos(rad(latA)) * Math.cos(rad(latB)) * Math.sin(rad(lonB - lonA) / 2) ** 2;
  return 6371 * 2 * Math.asin(Math.sqrt(a));
}
const TRAMOS = [];
const pares = new Set();
CAPITALES.forEach((sede, a) => {
  const vecinos = CAPITALES.map((otra, b) => ({ b, d: distancia(sede, otra) }))
    .filter(({ b }) => a !== b).sort((x, y) => x.d - y.d || x.b - y.b).slice(0, 4);
  vecinos.forEach(({ b, d }) => {
    const clave = a < b ? a + "-" + b : b + "-" + a;
    if (pares.has(clave)) return;
    pares.add(clave);
    TRAMOS.push({ a, b, c: Math.max(0.1, Math.round(d * 10) / 10), k: TRAMOS.length });
  });
});
const TOTAL_TRAMOS = TRAMOS.reduce((total, t) => total + t.c, 0);
const xMapa = lon => 134 + (lon + 82) * 29;
const yMapa = lat => 20 - lat * 28;
const PX = v => xMapa(CAPITALES[v][2]);
const PY = v => yMapa(CAPITALES[v][1]);
// Contorno administrativo generalizado de Natural Earth, escala 1:110 millones.
const CONTORNO = [[-69.894,-4.298],[-70.795,-4.251],[-70.929,-4.402],[-71.748,-4.594],[-72.892,-5.275],[-72.965,-5.741],[-73.22,-6.089],[-73.12,-6.63],[-73.724,-6.919],[-73.723,-7.341],[-73.987,-7.524],[-73.571,-8.424],[-73.015,-9.033],[-73.227,-9.462],[-72.563,-9.52],[-72.185,-10.054],[-71.302,-10.079],[-70.482,-9.49],[-70.549,-11.009],[-70.094,-11.124],[-69.53,-10.952],[-68.665,-12.561],[-68.88,-12.9],[-68.929,-13.603],[-68.949,-14.454],[-69.34,-14.953],[-69.16,-15.324],[-69.39,-15.66],[-68.96,-16.501],[-69.59,-17.58],[-69.858,-18.093],[-70.373,-18.348],[-71.375,-17.774],[-71.462,-17.363],[-73.445,-16.359],[-75.238,-15.266],[-76.009,-14.649],[-76.423,-13.823],[-76.259,-13.535],[-77.106,-12.223],[-78.092,-10.378],[-79.037,-8.387],[-79.446,-7.931],[-79.761,-7.194],[-80.537,-6.542],[-81.25,-6.137],[-80.926,-5.691],[-81.411,-4.737],[-81.1,-4.036],[-80.303,-3.405],[-80.184,-3.821],[-80.469,-4.059],[-80.442,-4.426],[-80.029,-4.346],[-79.625,-4.454],[-79.205,-4.959],[-78.64,-4.548],[-78.451,-3.873],[-77.838,-3.003],[-76.635,-2.609],[-75.545,-1.562],[-75.234,-0.911],[-75.373,-0.152],[-75.107,-0.057],[-74.442,-0.531],[-74.122,-1.003],[-73.66,-1.26],[-73.07,-2.309],[-72.326,-2.434],[-71.775,-2.17],[-71.414,-2.343],[-70.813,-2.257],[-70.048,-2.725],[-70.693,-3.743],[-70.394,-3.767],[-69.894,-4.298]];
const COLORES = ["#8A1D2C", "#1F5FA8", "#2F7D4F", "#A87A22", "#6B3FA0", "#C0562F", "#2B8A8A", "#7A7A2A", "#A03F6B", "#3F5F2F", "#5F3F2F", "#2F3F6F"];
const AJUSTES = {
  Callao: [-37, 9], Lima: [27, 8], Huancayo: [39, 1], Huancavelica: [-63, 13],
  Ayacucho: [-5, 26], Ica: [-13, 10], Cajamarca: [-48, 9], Trujillo: [-32, 9],
  Chiclayo: [-33, -7], Chachapoyas: [13, 21], Moyobamba: [40, -9], Huánuco: [38, -10],
  "Cerro de Pasco": [62, 4], Cusco: [21, 1], Abancay: [1, -16], Arequipa: [-31, -5],
  Moquegua: [-46, 3], Tacna: [25, 4], "Puerto Maldonado": [66, -6]
};
function dibujarRed(svg, estadoTramo, colorSede) {
  const puntos = CONTORNO.map(([lon, lat]) => [xMapa(lon), yMapa(lat)].join(",")).join(" ");
  let g = '<polygon points="' + puntos + '" fill="var(--sheet)" stroke="var(--rule)" stroke-width="1.5"/>';
  const orden = [...TRAMOS].sort((a,b) => Number(!!estadoTramo[a.k]?.trim()) - Number(!!estadoTramo[b.k]?.trim()));
  orden.forEach(t => {
    const e = estadoTramo[t.k] || "";
    g += '<line x1="' + PX(t.a) + '" y1="' + PY(t.a) + '" x2="' + PX(t.b) + '" y2="' + PY(t.b) + '" class="tr ' + e + '"/>';
  });
  NOMBRES.forEach((n, v) => {
    const [dx, dy] = AJUSTES[n] || [0, -10];
    const x = PX(v), y = PY(v);
    g += '<g><title>' + n + '</title><circle cx="' + x + '" cy="' + y + '" r="4.5" class="sd" style="fill:' + (colorSede(v) || "#fff") + '"/>';
    g += '<text x="' + (x + dx) + '" y="' + (y + dy) + '" class="sdl">' + n + '</text></g>';
  });
  svg.setAttribute("viewBox", "0 0 700 560");
  svg.innerHTML = g;
}

/* ---------- 2. Simulador de Kruskal ---------- */
(() => {
  const box = document.getElementById("simK"); if (!box) return;
  const orden = [...TRAMOS].sort((x, y) => x.c - y.c || x.k - y.k);   // orden estable, como Collections.sort
  const uf = ufNuevo(NOMBRES.length), estado = {};
  let costo = 0, aceptados = 0;
  const pasos = [{ estado: {}, uf: copiaUF(uf), costo: 0, actual: -1, nota: "Se ordenan 65 tramos por distancia. Cada sede empieza como su propio grupo." }];
  for (const t of orden) {
    if (aceptados === NOMBRES.length - 1) break;
    const ra = ufFind(uf, t.a), rb = ufFind(uf, t.b);
    const ok = ufUnion(uf, t.a, t.b);
    if (ok) { estado[t.k] = "acc"; costo += t.c; aceptados++; } else estado[t.k] = "rej";
    pasos.push({
      estado: { ...estado }, uf: copiaUF(uf), costo, actual: t.k,
      nota: ok ? `${NOMBRES[t.a]} – ${NOMBRES[t.b]} (${km(t.c)} km): find da grupos distintos (${NOMBRES[ra]} y ${NOMBRES[rb]}) → ACEPTADO y se unen. Costo acumulado: ${km(costo)} km.`
        : `${NOMBRES[t.a]} – ${NOMBRES[t.b]} (${km(t.c)} km): las dos sedes ya están en el grupo de ${NOMBRES[ra]} → RECHAZADO, cerraría un ciclo.`
    });
  }
  pasos[pasos.length - 1].nota += ` Ya hay ${NOMBRES.length - 1} tramos: el árbol está completo. Longitud mínima = ${km(costo)} km (de ${km(TOTAL_TRAMOS)} km).`;
  document.querySelectorAll("[data-red]").forEach(mapa => {
    const final = Object.fromEntries(TRAMOS.map(t => [t.k, estado[t.k] === "acc" ? "acc" : mapa.dataset.red === "plan" ? "oculto" : ""]));
    dibujarRed(mapa, final, () => "#fff");
  });
  const svg = box.querySelector("svg"), lista = box.querySelector(".lista"), nota = box.querySelector(".note"), cst = box.querySelector(".costo");
  montarSim(box, pasos, p => {
    const est = {}; TRAMOS.forEach(t => { est[t.k] = (p.estado[t.k] || "") + (t.k === p.actual ? " cur" : ""); });
    const grupos = {}; p.uf.padre.forEach((_, v) => { const r = ufFind(copiaUF(p.uf), v); (grupos[r] = grupos[r] || []).push(v); });
    const colorDe = {}; let k = 0; Object.values(grupos).forEach(g => { if (g.length > 1) g.forEach(v => colorDe[v] = COLORES[k]); if (g.length > 1) k++; });
    dibujarRed(svg, est, v => colorDe[v]);
    lista.innerHTML = orden.map(t => `<div class="it ${p.estado[t.k] || ""} ${t.k === p.actual ? "cur" : ""}"><span>${km(t.c)}</span>${NOMBRES[t.a]} – ${NOMBRES[t.b]}<b>${p.estado[t.k] === "acc" ? "✔" : p.estado[t.k] === "rej" ? "✘ ciclo" : ""}</b></div>`).join("");
    const fila = lista.querySelector(".cur");
    lista.scrollTop = fila ? fila.offsetTop - lista.clientHeight / 2 + fila.clientHeight / 2 : 0;
    nota.textContent = p.nota;
    cst.innerHTML = `<div><b>${km(p.costo)}</b><span>km acumulados</span></div><div><b>${Object.keys(grupos).length}</b><span>grupos (find)</span></div>`;
  });
})();

/* ---------- 3. Simulador de Prim (versión perezosa) ---------- */
(() => {
  const box = document.getElementById("simP"); if (!box) return;
  const ady = NOMBRES.map(() => []); TRAMOS.forEach(t => { ady[t.a].push(t); ady[t.b].push(t); });
  const enArbol = Array(NOMBRES.length).fill(false), estado = {}, cola = [];
  let costo = 0, orden = 0;
  const otro = (t, v) => t.a === v ? t.b : t.a;
  const visitar = v => { enArbol[v] = true; ady[v].forEach(t => { if (!enArbol[otro(t, v)]) { cola.push({ t, o: orden++ }); if (!estado[t.k]) estado[t.k] = "cola"; } }); };
  const s0 = NOMBRES.indexOf("Lima");
  visitar(s0);
  const foto = (nota, actual) => ({ estado: { ...estado }, arbol: [...enArbol], cola: cola.map(x => x.t).sort((x, y) => x.c - y.c), costo, actual, nota });
  const pasos = [foto(`Se empieza en Lima: entra al árbol y sus ${ady[s0].length} tramos van a la cola de prioridad.`, -1)];
  while (cola.length) {
    cola.sort((x, y) => x.t.c - y.t.c || x.o - y.o);
    const { t } = cola.shift();
    if (enArbol[t.a] && enArbol[t.b]) { if (estado[t.k] === "cola") estado[t.k] = "rej"; pasos.push(foto(`Sale ${NOMBRES[t.a]} – ${NOMBRES[t.b]} (${km(t.c)} km), pero sus dos sedes ya están en el árbol: se descarta.`, t.k)); continue; }
    const nueva = enArbol[t.a] ? t.b : t.a;
    estado[t.k] = "acc"; costo += t.c; visitar(nueva);
    // los tramos que quedaron con ambos extremos dentro siguen en la cola (perezoso)
    pasos.push(foto(`Sale el más barato que llega afuera: ${NOMBRES[otro(t, nueva)]} – ${NOMBRES[nueva]} (${km(t.c)} km). ${NOMBRES[nueva]} entra al árbol y sus tramos hacia afuera van a la cola. Costo: ${km(costo)} km.`, t.k));
  }
  pasos[pasos.length - 1].nota += " La cola quedó vacía: árbol completo con " + km(costo) + " km, igual que Kruskal.";
  const svg = box.querySelector("svg"), lista = box.querySelector(".lista"), nota = box.querySelector(".note"), cst = box.querySelector(".costo");
  montarSim(box, pasos, p => {
    const est = {}; TRAMOS.forEach(t => est[t.k] = (p.estado[t.k] || "") + (t.k === p.actual ? " cur" : ""));
    dibujarRed(svg, est, v => p.arbol[v] ? "#8A1D2C" : null);
    lista.innerHTML = `<div class="tag">Cola de prioridad (mínimo arriba)</div>` + (p.cola.length ? p.cola.slice(0, 9).map((t, i) => `<div class="it ${i === 0 ? "top" : ""} ${p.arbol[t.a] && p.arbol[t.b] ? "obs" : ""}"><span>${km(t.c)}</span>${NOMBRES[t.a]} – ${NOMBRES[t.b]}</div>`).join("") + (p.cola.length > 9 ? `<div class="it mas">… y ${p.cola.length - 9} más</div>` : "") : `<div class="it mas">(vacía)</div>`);
    nota.textContent = p.nota;
    cst.innerHTML = `<div><b>${km(p.costo)}</b><span>km acumulados</span></div><div><b>${p.arbol.filter(Boolean).length} / ${NOMBRES.length}</b><span>sedes en el árbol</span></div>`;
  });
})();

/* ---------- gráficos del benchmark ---------- */
(() => {
  const g1 = document.getElementById("grafMST"), g2 = document.getElementById("grafUF");
  if (g1) grafico(g1, [
    { name: "Kruskal · densa", color: "#8A1D2C", pts: BENCH.mst.filter(r => r[0] === "densa").map(r => [r[1], r[3]]) },
    { name: "Prim · densa", color: "#1F5FA8", pts: BENCH.mst.filter(r => r[0] === "densa").map(r => [r[1], r[4]]) },
    { name: "Kruskal · dispersa", color: "#8A1D2C", dash: 1, pts: BENCH.mst.filter(r => r[0] === "dispersa").map(r => [r[1], r[3]]) },
    { name: "Prim · dispersa", color: "#1F5FA8", dash: 1, pts: BENCH.mst.filter(r => r[0] === "dispersa").map(r => [r[1], r[4]]) },
  ], { xLabel: "n (sedes)", yLabel: "tiempo (ms)", ticksX: [1000, 10000, 20000, 50000] });
  if (g2) grafico(g2, [
    { name: "Ingenuo (sin optimizar)", color: "#8D9AA8", dash: 1, pts: BENCH.uf.filter(r => r[3] !== null).map(r => [r[0], r[3]]) },
    { name: "Compresión + unión por rango", color: "#8A1D2C", pts: BENCH.uf.map(r => [r[0], r[2]]) },
  ], { xLabel: "n (elementos)", yLabel: "nanosegundos por operación", logX: 1, logY: 1 });
})();
