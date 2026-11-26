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
      `<tr><td>padre</td>${p.uf.padre.map((q, i) => `<td class="${p.resalta.includes(i) ? "chg" : ""}">${L[q]}</td>`).join("")}</tr>` +
      `<tr><td>rango</td>${p.uf.rango.map(r => `<td>${r}</td>`).join("")}</tr>`;
    nota.textContent = p.nota; op.textContent = p.op;
  });
})();

/* ---------- Red de ejemplo de Lima (datos/red_ejemplo.csv) ---------- */
const SEDES = { "Sede Central": [4, 5], "San Isidro": [3.4, 3.8], "Miraflores": [3.6, 2.6], "Barranco": [3.2, 1.5], "Chorrillos": [4.2, 0.7], "Surco": [5, 2.2], "La Molina": [6.6, 2.6], "Ate": [6.8, 4], "SJL": [6.2, 5.6], "Comas": [4.4, 7], "Los Olivos": [2.6, 6.6], "Callao": [1.4, 5] };
const NOMBRES = Object.keys(SEDES);
const TRAMOS = [["Sede Central", "San Isidro", 4], ["Sede Central", "Miraflores", 7], ["Sede Central", "Callao", 12], ["San Isidro", "Miraflores", 3], ["San Isidro", "Surco", 9], ["Miraflores", "Surco", 5], ["Miraflores", "Barranco", 2], ["Barranco", "Chorrillos", 3], ["Surco", "Chorrillos", 6], ["Surco", "La Molina", 8], ["Callao", "Los Olivos", 10], ["Los Olivos", "Comas", 4], ["Comas", "SJL", 11], ["Sede Central", "SJL", 14], ["SJL", "Ate", 6], ["Ate", "La Molina", 5], ["Ate", "Sede Central", 13], ["Callao", "San Isidro", 15]]
  .map(([a, b, c], k) => ({ a: NOMBRES.indexOf(a), b: NOMBRES.indexOf(b), c, k }));
const PX = v => 50 + (SEDES[NOMBRES[v]][0] - 1.2) * 108, PY = v => 34 + (7.3 - SEDES[NOMBRES[v]][1]) * 74;
const COLORES = ["#8A1D2C", "#1F5FA8", "#2F7D4F", "#A87A22", "#6B3FA0", "#C0562F", "#2B8A8A", "#7A7A2A", "#A03F6B", "#3F5F2F", "#5F3F2F", "#2F3F6F"];
function dibujarRed(svg, estadoTramo, colorSede, etiquetas = true) {
  let g = "";
  TRAMOS.forEach((t, i) => {
    const e = estadoTramo[i] || "";
    g += `<line x1="${PX(t.a)}" y1="${PY(t.a)}" x2="${PX(t.b)}" y2="${PY(t.b)}" class="tr ${e}"/>`;
    g += `<g class="cl ${e}"><rect x="${(PX(t.a) + PX(t.b)) / 2 - 15}" y="${(PY(t.a) + PY(t.b)) / 2 - 13}" width="30" height="24" rx="4"/><text x="${(PX(t.a) + PX(t.b)) / 2}" y="${(PY(t.a) + PY(t.b)) / 2 + 5}">${t.c}</text></g>`;
  });
  NOMBRES.forEach((n, v) => {
    const col = colorSede(v);
    g += `<circle cx="${PX(v)}" cy="${PY(v)}" r="15" class="sd" style="fill:${col || "#fff"}"/>`;
    if (etiquetas) g += `<text x="${PX(v)}" y="${PY(v) - 22}" class="sdl">${n}</text>`;
  });
  svg.setAttribute("viewBox", "0 0 700 560"); svg.innerHTML = g;
}

/* ---------- 2. Simulador de Kruskal ---------- */
(() => {
  const box = document.getElementById("simK"); if (!box) return;
  const orden = [...TRAMOS].sort((x, y) => x.c - y.c || x.k - y.k);   // orden estable, como Collections.sort
  const uf = ufNuevo(NOMBRES.length), estado = {};
  let costo = 0, aceptados = 0;
  const pasos = [{ estado: {}, uf: copiaUF(uf), costo: 0, actual: -1, nota: "Se ordenan los 18 tramos por costo. Cada sede empieza como su propio grupo (12 grupos)." }];
  for (const t of orden) {
    if (aceptados === NOMBRES.length - 1) break;
    const ra = ufFind(uf, t.a), rb = ufFind(uf, t.b);
    const ok = ufUnion(uf, t.a, t.b);
    if (ok) { estado[t.k] = "acc"; costo += t.c; aceptados++; } else estado[t.k] = "rej";
    pasos.push({
      estado: { ...estado }, uf: copiaUF(uf), costo, actual: t.k,
      nota: ok ? `${NOMBRES[t.a]} – ${NOMBRES[t.b]} (${t.c}): find da grupos distintos (${NOMBRES[ra]} y ${NOMBRES[rb]}) → ACEPTADO y se unen. Costo acumulado: ${costo}.`
        : `${NOMBRES[t.a]} – ${NOMBRES[t.b]} (${t.c}): las dos sedes ya están en el grupo de ${NOMBRES[ra]} → RECHAZADO, cerraría un ciclo.`
    });
  }
  pasos[pasos.length - 1].nota += ` Ya hay ${NOMBRES.length - 1} tramos: el árbol está completo. Costo mínimo = ${costo} (de 137).`;
  const svg = box.querySelector("svg"), lista = box.querySelector(".lista"), nota = box.querySelector(".note"), cst = box.querySelector(".costo");
  montarSim(box, pasos, p => {
    const est = {}; TRAMOS.forEach(t => { est[t.k] = (p.estado[t.k] || "") + (t.k === p.actual ? " cur" : ""); });
    const grupos = {}; p.uf.padre.forEach((_, v) => { const r = ufFind(copiaUF(p.uf), v); (grupos[r] = grupos[r] || []).push(v); });
    const colorDe = {}; let k = 0; Object.values(grupos).forEach(g => { if (g.length > 1) g.forEach(v => colorDe[v] = COLORES[k]); if (g.length > 1) k++; });
    dibujarRed(svg, est, v => colorDe[v]);
    lista.innerHTML = orden.map(t => `<div class="it ${p.estado[t.k] || ""} ${t.k === p.actual ? "cur" : ""}"><span>${t.c}</span>${NOMBRES[t.a]} – ${NOMBRES[t.b]}<b>${p.estado[t.k] === "acc" ? "✔" : p.estado[t.k] === "rej" ? "✘ ciclo" : ""}</b></div>`).join("");
    nota.textContent = p.nota;
    cst.innerHTML = `<div><b>${p.costo}</b><span>costo acumulado</span></div><div><b>${Object.keys(grupos).length}</b><span>grupos (find)</span></div>`;
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
  const s0 = NOMBRES.indexOf("Sede Central");
  visitar(s0);
  const foto = (nota, actual) => ({ estado: { ...estado }, arbol: [...enArbol], cola: cola.map(x => x.t).sort((x, y) => x.c - y.c), costo, actual, nota });
  const pasos = [foto(`Se empieza en Sede Central: entra al árbol y sus ${ady[s0].length} tramos van a la cola de prioridad.`, -1)];
  while (cola.length) {
    cola.sort((x, y) => x.t.c - y.t.c || x.o - y.o);
    const { t } = cola.shift();
    if (enArbol[t.a] && enArbol[t.b]) { if (estado[t.k] === "cola") estado[t.k] = "rej"; pasos.push(foto(`Sale ${NOMBRES[t.a]} – ${NOMBRES[t.b]} (${t.c}), pero sus dos sedes ya están en el árbol: se descarta.`, t.k)); continue; }
    const nueva = enArbol[t.a] ? t.b : t.a;
    estado[t.k] = "acc"; costo += t.c; visitar(nueva);
    // los tramos que quedaron con ambos extremos dentro siguen en la cola (perezoso)
    pasos.push(foto(`Sale el más barato que llega afuera: ${NOMBRES[otro(t, nueva)]} – ${NOMBRES[nueva]} (${t.c}). ${NOMBRES[nueva]} entra al árbol y sus tramos hacia afuera van a la cola. Costo: ${costo}.`, t.k));
  }
  pasos[pasos.length - 1].nota += " La cola quedó vacía: árbol completo con costo 61, igual que Kruskal.";
  const svg = box.querySelector("svg"), lista = box.querySelector(".lista"), nota = box.querySelector(".note"), cst = box.querySelector(".costo");
  montarSim(box, pasos, p => {
    const est = {}; TRAMOS.forEach(t => est[t.k] = (p.estado[t.k] || "") + (t.k === p.actual ? " cur" : ""));
    dibujarRed(svg, est, v => p.arbol[v] ? "#8A1D2C" : null);
    lista.innerHTML = `<div class="tag">Cola de prioridad (mínimo arriba)</div>` + (p.cola.length ? p.cola.slice(0, 9).map((t, i) => `<div class="it ${i === 0 ? "top" : ""} ${p.arbol[t.a] && p.arbol[t.b] ? "obs" : ""}"><span>${t.c}</span>${NOMBRES[t.a]} – ${NOMBRES[t.b]}</div>`).join("") + (p.cola.length > 9 ? `<div class="it mas">… y ${p.cola.length - 9} más</div>` : "") : `<div class="it mas">(vacía)</div>`);
    nota.textContent = p.nota;
    cst.innerHTML = `<div><b>${p.costo}</b><span>costo acumulado</span></div><div><b>${p.arbol.filter(Boolean).length} / 12</b><span>sedes en el árbol</span></div>`;
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
