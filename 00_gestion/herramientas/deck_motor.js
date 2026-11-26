/* =====================================================================
   Motor de presentación (compartido por FibraNet y RedLuz)
   Basado en el formato del Laboratorio 2: lámina 1600×900, cajetín,
   guion (N), índice (O), pantalla completa (F) y exportar PDF (P).
   ===================================================================== */
const esc = s => String(s).replace(/&/g, "&amp;").replace(/</g, "&lt;");

/* ---------- código Java resaltado ---------- */
const KW = "public|private|protected|static|final|class|new|return|if|else|for|while|do|break|continue|boolean|int|long|double|void|null|true|false|this|import|package|throws|try|catch|switch|case|default";
function hlJava(t) {
  const i = t.indexOf("//");
  const c = i < 0 ? t : t.slice(0, i), cm = i < 0 ? "" : t.slice(i);
  // se recorre el código fuente por tokens y se escapa cada uno: así nunca se resalta dentro del HTML generado
  const tok = new RegExp('("[^"]*")|\\b(' + KW + ')\\b|\\b([A-Z][A-Za-z]+)\\b', "g");
  let h = "", ult = 0, m;
  while ((m = tok.exec(c)) !== null) {
    h += esc(c.slice(ult, m.index));
    const clase = m[1] ? "rg" : (m[2] ? "op" : "dv");
    h += `<span class="${clase}">${esc(m[0])}</span>`;
    ult = tok.lastIndex;
  }
  h += esc(c.slice(ult));
  return h + (cm ? `<span class="cm">${esc(cm)}</span>` : "");
}
document.querySelectorAll("pre[data-java]").forEach(p => {
  const src = CODIGO[p.dataset.java] || "";
  const marcas = (p.dataset.mark || "").split("|").filter(Boolean);
  p.innerHTML = src.split("\n").map((x, k) => {
    const on = marcas.some(m => x.includes(m));
    return `<span class="l${on ? " hl" : ""}"><span class="n">${k + 1}</span>${hlJava(x)}</span>`;
  }).join("");
});

/* ---------- imágenes generadas con los prompts de 04_slides ---------- */
document.querySelectorAll("img[data-img]").forEach(im => {
  const k = im.dataset.img, box = im.parentElement;
  im.onerror = () => {
    im.remove();
    box.classList.add("falta");
    box.innerHTML = `<div class="ph"><b>Imagen ${k}</b>Genere la imagen con el prompt <span class="m">04_slides\\${k}.txt</span> en ChatGPT y guárdela como <span class="m">03_ppt\\img\\${k}.png</span>. Luego recargue esta página.</div>`;
  };
  im.src = `img/${k}.png`;
  im.onclick = () => { const z = document.createElement("div"); z.id = "zoom"; z.innerHTML = `<img src="${im.src}" alt="">`; z.onclick = () => z.remove(); document.body.appendChild(z); };
});

/* ---------- gráficos de línea en SVG (benchmark) ---------- */
function grafico(svg, series, o) {
  const W = 760, H = 470, m = { l: 86, r: 24, t: 20, b: 64 };
  const lx = v => o.logX ? Math.log10(v) : v, ly = v => o.logY ? Math.log10(v) : v;
  const xs = series.flatMap(s => s.pts.map(p => lx(p[0]))), ys = series.flatMap(s => s.pts.map(p => ly(p[1])));
  let x0 = Math.min(...xs), x1 = Math.max(...xs), y0 = o.logY ? Math.floor(Math.min(...ys)) : 0, y1 = o.logY ? Math.ceil(Math.max(...ys)) : Math.max(...ys) * 1.08;
  if (o.logX) { x0 = Math.floor(x0); x1 = Math.ceil(x1); }
  const X = v => m.l + (lx(v) - x0) / (x1 - x0) * (W - m.l - m.r);
  const Y = v => H - m.b - (ly(v) - y0) / (y1 - y0) * (H - m.t - m.b);
  let g = `<rect x="${m.l}" y="${m.t}" width="${W - m.l - m.r}" height="${H - m.t - m.b}" fill="#fff" stroke="var(--rule)"/>`;
  const ticksY = o.logY ? Array.from({ length: y1 - y0 + 1 }, (_, i) => Math.pow(10, y0 + i)) : [0, 1, 2, 3, 4, 5].map(i => y1 / 5 * i);
  ticksY.forEach(t => { const y = Y(t); g += `<line x1="${m.l}" x2="${W - m.r}" y1="${y}" y2="${y}" stroke="var(--grid)"/><text x="${m.l - 10}" y="${y + 5}" text-anchor="end" class="ax">${o.logY ? "10" + "⁰¹²³⁴⁵⁶"[Math.log10(t)] : Math.round(t)}</text>`; });
  const ticksX = o.logX ? Array.from({ length: x1 - x0 + 1 }, (_, i) => Math.pow(10, x0 + i)) : o.ticksX;
  ticksX.forEach(t => { const x = X(t); g += `<line y1="${m.t}" y2="${H - m.b}" x1="${x}" x2="${x}" stroke="var(--grid)"/><text x="${x}" y="${H - m.b + 24}" text-anchor="middle" class="ax">${o.logX ? "10" + "⁰¹²³⁴⁵⁶"[Math.log10(t)] : t.toLocaleString("es-PE")}</text>`; });
  g += `<text x="${(W + m.l) / 2}" y="${H - 12}" text-anchor="middle" class="axl">${o.xLabel}</text><text transform="translate(22 ${(H - m.b + m.t) / 2}) rotate(-90)" text-anchor="middle" class="axl">${o.yLabel}</text>`;
  series.forEach(s => {
    g += `<polyline fill="none" stroke="${s.color}" stroke-width="3.5" ${s.dash ? 'stroke-dasharray="9 6"' : ""} points="${s.pts.map(p => X(p[0]) + "," + Y(p[1])).join(" ")}"/>`;
    s.pts.forEach(p => g += `<circle cx="${X(p[0])}" cy="${Y(p[1])}" r="5.5" fill="${s.color}"/>`);
  });
  g += series.map((s, i) => `<g transform="translate(${m.l + 18} ${m.t + 22 + i * 26})"><line x1="0" x2="28" y1="0" y2="0" stroke="${s.color}" stroke-width="3.5" ${s.dash ? 'stroke-dasharray="9 6"' : ""}/><text x="38" y="5" class="lg">${s.name}</text></g>`).join("");
  svg.setAttribute("viewBox", `0 0 ${W} ${H}`); svg.innerHTML = g;
}

/* ---------- portada, agenda y agradecimiento ---------- */
const nombreCorto = t => { const [ap, no] = t.split(","); return no.trim().split(" ")[0] + " " + ap.trim().split(" ")[0]; };
document.querySelectorAll(".teamlist").forEach(e => e.innerHTML = META.team.map((t, i) => `<div>${t}<span>${META.codes[i] || ""}</span></div>`).join(""));
const ag = document.getElementById("agenda");
if (ag) ag.innerHTML = META.blocks.map(b => `<div class="panel ${b.motor ? "acc" : ""}"><span class="tag">Láminas ${b.range} · ${nombreCorto(META.team[b.owner - 1])}</span><h3>${b.name}</h3><p style="font-size:19px">${b.desc}</p></div>`).join("");

/* ---------- motor de presentación ---------- */
const slides = [...document.querySelectorAll(".slide")], N = slides.length, team = META.team;
slides.forEach((s, i) => {
  const m = META.slides[i] || {};
  if (i === 0 || s.dataset.nocaj) return;
  s.insertAdjacentHTML("beforeend", `<div class="cajetin"><div><em>Curso · Entregable</em><strong>${META.curso} · ${META.tarea}</strong></div><div><em>Bloque</em><strong>${m.block || ""}</strong></div><div class="own"><em>Expone</em><strong>${team[(m.owner || 1) - 1]}</strong></div><div><em>Lámina</em><strong class="num">${String(i + 1).padStart(2, "0")} / ${N}</strong></div></div>`);
});
let cur = 0;
function fit() { const st = document.getElementById("stage"), k = Math.min(innerWidth / 1600, innerHeight / 900); st.style.transform = `scale(${k})`; }
addEventListener("resize", fit); fit();
function go(i) {
  cur = Math.max(0, Math.min(N - 1, i));
  slides.forEach((s, j) => s.classList.toggle("active", j === cur));
  document.getElementById("bar").style.width = ((cur + 1) / N * 100) + "%";
  const m = META.slides[cur] || {};
  document.getElementById("notesH").textContent = `Lámina ${cur + 1} · ${team[(m.owner || 1) - 1]} · ${m.time || ""}`;
  document.getElementById("notesB").textContent = m.notes || "";
  try { history.replaceState(null, "", "#" + (cur + 1)); } catch (e) { }
}
const ov = document.getElementById("overview");
ov.innerHTML = slides.map((s, i) => { const m = META.slides[i] || {}; return `<button type="button" class="th" data-i="${i}"><b>${String(i + 1).padStart(2, "0")} · ${m.block || ""}</b><span>${m.title || ""}</span><i>${team[(m.owner || 1) - 1]}</i></button>`; }).join("");
let focoAntes = null;                                       // se devuelve el foco al cerrar el índice
const cerrarO = () => { ov.hidden = true; (focoAntes && focoAntes.isConnected && focoAntes !== document.body ? focoAntes : document.getElementById("bOv")).focus(); };
ov.addEventListener("click", e => { const t = e.target.closest(".th"); if (t) { go(+t.dataset.i); cerrarO(); } });
const tgN = () => { const n = document.getElementById("notes"); n.hidden = !n.hidden; };
const tgO = () => {
  if (!ov.hidden) { cerrarO(); return; }
  focoAntes = document.activeElement; ov.hidden = false;
  const fichas = ov.querySelectorAll(".th");
  fichas.forEach((t, i) => t.classList.toggle("cur", i === cur));
  fichas[cur]?.focus();
};
const tgF = () => { try { document.fullscreenElement ? document.exitFullscreen() : document.documentElement.requestFullscreen(); } catch (e) { } };
document.getElementById("bPrev").onclick = () => go(cur - 1);
document.getElementById("bNext").onclick = () => go(cur + 1);
const pt = document.getElementById("printTip");
const tgP = () => { pt.hidden = false; document.getElementById("ptGo").focus(); };
document.getElementById("bPdf").onclick = tgP;
document.getElementById("ptCancel").onclick = () => pt.hidden = true;
document.getElementById("ptGo").onclick = () => { pt.hidden = true; setTimeout(() => window.print(), 120); };
addEventListener("beforeprint", () => { ov.hidden = true; document.getElementById("notes").hidden = true; });
document.getElementById("bNotes").onclick = tgN;
document.getElementById("bOv").onclick = tgO;
document.getElementById("bFs").onclick = tgF;
addEventListener("keydown", e => {
  if (e.target.isContentEditable || /INPUT|TEXTAREA|SELECT/.test(e.target.tagName)) return;
  if (e.key === "Escape") document.getElementById("zoom")?.remove();
  if (!pt.hidden) { if (e.key === "Escape") pt.hidden = true; return; }
  if ((e.key === "p" || e.key === "P") && !e.ctrlKey && !e.metaKey) { tgP(); return; }
  const k = e.key;
  const enControl = e.target.closest && e.target.closest("button, a, [role=button]");
  if (k === " " && enControl) return;                       // Espacio activa el botón con foco, no cambia de lámina
  if (!ov.hidden && k !== "Escape" && k !== "o" && k !== "O") return;   // en el índice, Tab/Enter navegan las fichas
  if (["ArrowRight", "PageDown", " "].includes(k)) { e.preventDefault(); go(cur + 1); }
  else if (["ArrowLeft", "PageUp"].includes(k)) { e.preventDefault(); go(cur - 1); }
  else if (k === "Home") go(0); else if (k === "End") go(N - 1);
  else if (k === "n" || k === "N") tgN(); else if (k === "o" || k === "O") tgO();
  else if (k === "f" || k === "F") tgF(); else if (k === "Escape" && !ov.hidden) cerrarO();
});
let tx = null;
addEventListener("touchstart", e => tx = e.touches[0].clientX, { passive: true });
addEventListener("touchend", e => { if (tx === null) return; const d = e.changedTouches[0].clientX - tx; if (Math.abs(d) > 50) go(cur + (d < 0 ? 1 : -1)); tx = null; });
go(Math.max(0, (parseInt(location.hash.slice(1)) || 1) - 1));
