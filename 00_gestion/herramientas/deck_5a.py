# -*- coding: utf-8 -*-
"""
Genera el deck HTML de 5.a FibraNet (estilo Laboratorio 2), los prompts de
imagen (04_slides/Lxx.txt) y los guiones (05_guiones/*.md) desde una sola
fuente: la lista LAMINAS de este archivo.

Uso (desde la carpeta 00_gestion/herramientas):
    python deck_5a.py
"""
import json, os, re, sys

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.normpath(os.path.join(AQUI, "..", ".."))
TEMA = os.path.join(RAIZ, "5a_FibraNet")
COD = os.path.join(TEMA, "06_codigo")
leer = lambda p: open(p, encoding="utf-8").read()

EQUIPO = ["Acosta Simon, Christopher Ricardo", "Gavilano Kanashiro, Santiago",
          "Chionglaw Kong Tarrillo, Javier", "Cerron Arica, Gianpierre Eyner",
          "Guzman Lopez, Raul Antonio", "Loli Aldunate, Cesar Antoni"]
CORTO = ["Christopher", "Santiago", "Javier", "Gianpierre", "Raul", "Cesar"]
CURSO = "SW305 · 2026-2"
INST = "Universidad Nacional de Ingeniería · FIIS · SW305 Algoritmos Avanzados · Ciclo 2026-2"
DOCENTE = "Ing. Jorge Medianero Acosta"

BLOQUES = [
    dict(name="El problema", desc="Qué es FibraNet, qué pide el cliente y qué hace el programa.", range="01–04", owner=1),
    dict(name="Teoría del MST", desc="Grafo ponderado, árbol de expansión mínima, corte, ciclo y complejidad.", range="05–07", owner=2),
    dict(name="Motor 1 · Union-Find", desc="padre[], rango[], find con compresión y union por rango.", range="08–10", owner=3, motor=1),
    dict(name="Motor 2 · Kruskal", desc="Ordenar tramos y aceptar solo los que unen grupos distintos.", range="11–13", owner=4, motor=1),
    dict(name="Motor 3 · Prim", desc="Hacer crecer un árbol con una cola de prioridad.", range="14–16", owner=5, motor=1),
    dict(name="Resultados", desc="Arquitectura, demo, benchmark contra la teoría y conclusiones.", range="17–22", owner=6),
]

# ---------------------------------------------------------------------------
# Fragmentos de código (copiados del motor real, recortados para la lámina)
# ---------------------------------------------------------------------------
def fragmento(ruta, desde, hasta):
    """Devuelve las líneas entre la que contiene 'desde' y la que contiene 'hasta' (incluidas)."""
    ls = leer(ruta).split("\n")
    i = next(k for k, l in enumerate(ls) if desde in l)
    j = next(k for k in range(i, len(ls)) if hasta in ls[k])
    out = []
    for l in ls[i:j + 1]:
        t = l.strip()
        if t.startswith(("/**", "*", "*/")) or not t:
            continue                                   # se omite el javadoc
        if "//" in l and not t.startswith("//"):
            l = l[:l.index("//")].rstrip()             # se omite el comentario al final
        out.append(l)
    sangria = min(len(l) - len(l.lstrip()) for l in out if l.strip())
    return "\n".join(l[sangria:] for l in out)

M = os.path.join(COD, "src", "fibranet", "motor")
CODIGO = {
    "uf": fragmento(os.path.join(M, "UnionFind.java"), "public int find", "grupos--;") + "\n    return true;\n}",
    "kruskal": fragmento(os.path.join(M, "Kruskal.java"), "List<Arista> ordenadas", "resultado.agregar(arista);") + "\n    }\n    // (si pasoAPaso, guarda la decisión para la tabla)\n}",
    "prim": fragmento(os.path.join(M, "Prim.java"), "while (!cola.isEmpty())", "visitar(grafo, nueva, enArbol, cola);") + "\n}",
    "visitar": fragmento(os.path.join(M, "Prim.java"), "private static void visitar", "cola.add(arista);").replace("int sede, boolean[] enArbol,", "int sede,\n                            boolean[] enArbol,") + "\n        }\n    }\n}",
}

# ---------------------------------------------------------------------------
# Datos del benchmark: los CSV versionados en 5a_FibraNet/06_codigo/benchmark/
# (corrida en la PC del grupo). Para probar otra corrida sin tocar esos CSV,
# se copian a 00_gestion/herramientas/bench5a/ y se marcan como preliminares.
# ---------------------------------------------------------------------------
def cargar_bench():
    import csv
    prueba = os.path.join(AQUI, "bench5a")
    base = prueba if os.path.isdir(prueba) else os.path.join(COD, "benchmark")
    with open(os.path.join(base, "resultados_mst.csv"), encoding="utf-8", newline="") as archivo:
        filas_mst = list(csv.DictReader(archivo))
    mst = [[r["densidad"], int(r["V"]), int(r["E"]), float(r["kruskal_ms"]), float(r["prim_ms"])]
           for r in filas_mst]
    uf = [[int(r["n"]), int(r["operaciones"]), float(r["optimizado_ns_por_op"]),
           float(r["ingenuo_ns_por_op"]) if r["ingenuo_ns_por_op"] else None]
          for r in csv.DictReader(open(os.path.join(base, "resultados_unionfind.csv")))]
    return dict(mst=mst, uf=uf, estadisticas_mst=filas_mst, preliminar=(base == prueba))

BENCH = cargar_bench()
# rango de ns/op del Union-Find optimizado desde n = 5 000 (antes el JIT aún no se estabiliza)
_UF_OPT = [f[2] for f in BENCH["uf"] if f[0] >= 5000]
UF_MIN, UF_MAX = round(min(_UF_OPT)), round(max(_UF_OPT))
_UF_20K = next(f for f in BENCH["uf"] if f[0] == 20000)
UF_VECES = int(round(_UF_20K[3] / _UF_20K[2], -2))
UF_VECES_TXT = f"{UF_VECES:,}".replace(",", " ")

def mapa(k, alt, modo):
    return (f'<div class="mapa-caso"><svg data-red="{modo}" role="img" aria-label="{alt}"></svg>'
            f'{img(k, alt)}</div>')


def img(k, alt):
    return f'<div class="imgbox"><img data-img="{k}" alt="{alt}"></div>'

def num(x, d=1):
    """1234.5 -> '1 234,5' (miles con espacio, decimales con coma)."""
    return f"{x:,.{d}f}".replace(",", " ").replace(".", ",")

def ent(x):
    return f"{x:,}".replace(",", " ")

def tabla_mst():
    detallado = "kruskal_desv" in BENCH["estadisticas_mst"][0]
    filas = []
    for r in BENCH["estadisticas_mst"]:
        if int(r["V"]) not in (1000, 10000, 50000):
            continue
        def medida(alg):
            media = num(float(r[alg + "_ms"]), 2)
            desv = r.get(alg + "_desv")
            return media + (" ± " + num(float(desv), 2) if desv else "")
        teorico = (num(float(r["kruskal_teorico_ms"]), 2) + " / " +
                   num(float(r["prim_teorico_ms"]), 2)) if detallado else "—"
        valores = (r["densidad"], ent(int(r["V"])), medida("kruskal"), medida("prim"), teorico)
        filas.append("<tr>" + "".join(f'<td class="m">{v}</td>' for v in valores) + "</tr>")
    return ('<table class="compact bench-mst"><tr><th>Red</th><th>V</th><th>Kruskal (ms)</th>'
            '<th>Prim (ms)</th><th>Ajuste K / P (ms)</th></tr>' + "".join(filas) + "</table>")


def resumen_mst():
    from statistics import mean
    densas = [r for r in BENCH["estadisticas_mst"] if r["densidad"] == "densa"]
    mayor = max(densas, key=lambda r: int(r["V"]))
    tiempos = (num(float(mayor["kruskal_ms"]), 2), num(float(mayor["prim_ms"]), 2))
    if "kruskal_teorico_ms" not in mayor:
        return tiempos, "Comparamos el crecimiento con E log₂V.", ""
    error = {a: mean(abs(float(r[a + "_ms"]) / float(r[a + "_teorico_ms"]) - 1)
                     for r in densas) * 100 for a in ("kruskal", "prim")}
    contraste = (f"En densas, error relativo medio del ajuste c·E·log₂V: "
                 f"<b>{num(error['kruskal'])} %</b> Kruskal, <b>{num(error['prim'])} %</b> Prim.")
    oral = (f"En densas, el error relativo medio del ajuste fue {num(error['kruskal'])} por ciento para Kruskal "
            f"y {num(error['prim'])} para Prim. Alternamos qué algoritmo empieza cada corrida.")
    return tiempos, contraste, oral


MST_TIEMPOS, MST_CONTRASTE, MST_ORAL = resumen_mst()


def tabla_uf():
    filas = "".join(
        f'<tr><td class="m">{ent(r[0])}</td><td class="m">{num(r[2])}</td><td class="m">{num(r[3], 0) if r[3] else "—"}</td><td class="m"><b>{num(r[3] / r[2], 0) + "×" if r[3] else "—"}</b></td></tr>'
        for r in BENCH["uf"])
    return f'<table class="compact"><tr><th>n</th><th>Optimizado ns/op</th><th>Ingenuo ns/op</th><th>Mejora</th></tr>{filas}</table>'

AVISO = ('<div class="aviso">Datos preliminares (entorno de desarrollo). Se reemplazan con la corrida en la PC del grupo.</div>'
         if BENCH["preliminar"] else "")

# ---------------------------------------------------------------------------
# LÁMINAS: html + guion (lo que se dice) + prompt de imagen
# ---------------------------------------------------------------------------
L = []
def lam(owner, block, title, time, html, notes, prompt, cls="", nocaj=False):
    L.append(dict(owner=owner, block=block, title=title, time=time, html=html, notes=notes,
                  prompt=prompt, cls=cls, nocaj=nocaj))

ESTILO_IMG = ("Estilo: ilustración plana de arquitectura técnica, fondo blanco con cuadrícula gris muy tenue, "
              "colores granate #8A1D2C, dorado #A87A22, azul #1F5FA8 y gris pizarra #152131, líneas limpias, "
              "sin sombras exageradas, sin texto incrustado salvo el que se indique, formato horizontal 16:9, alta resolución.")

# 1 ─ Portada
lam(1, "Portada", "FibraNet: red de fibra de costo mínimo", "0:40", f'''
  <div class="inst">{INST}</div>
  <div style="display:grid;grid-template-columns:1fr 1fr;gap:44px;align-items:stretch;flex:1;min-height:0">
    <div style="display:flex;flex-direction:column;gap:18px;justify-content:center">
      <div class="eyebrow"><b>PROYECTO FINAL · TEMA 5.a</b> Grupo 5</div>
      <h1>FibraNet</h1>
      <p class="lead" style="font-size:28px">Un plan de fibra de <b style="color:var(--granate)">longitud mínima</b> inspirado en la Red Dorsal Nacional de Fibra Óptica.</p>
    </div>
    {mapa("L01", "Plan de fibra entre capitales del Perú", "plan")}
  </div>
  <div style="display:grid;grid-template-columns:1.4fr 1fr;gap:40px;align-items:end">
    <div class="team teamlist"></div>
    <div style="font-size:18px;color:var(--muted)">Docente: <b style="color:var(--ink)">{DOCENTE}</b><br>Lima, martes 8 de diciembre de 2026</div>
  </div>''',
    "Buenos días. Somos el Grupo 5. Presentamos FibraNet, un modelo inspirado en la Red Dorsal Nacional de Fibra Óptica. "
    "En el mapa vemos 24 capitales departamentales y el Callao. Queremos conectarlas con la menor longitud total dentro del grafo de candidatos. "
    "Para resolverlo usamos Prim, Kruskal y Union-Find. Primero veremos cómo construimos los candidatos. Después mostraremos el motor y los resultados.",
    "Mapa del Perú con las 25 sedes y los 24 enlaces del plan. Usa el grafo de FibraNet como referencia. " + ESTILO_IMG,
    cls="cover", nocaj=True)

# 2 ─ Agenda
lam(1, "Portada", "Agenda", "0:30", '''
  <div class="eyebrow"><b>00</b> Hoja de ruta</div>
  <h2>Agenda de la exposición</h2>
  <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:22px;flex:1" id="agenda"></div>''',
    "La exposición tiene seis bloques. Primero el problema y los requerimientos. Luego la teoría del árbol de expansión mínima. "
    "Después vienen los tres bloques del motor, marcados en granate: Union-Find, Kruskal y Prim, cada uno con su código y un simulador paso a paso. "
    "Cerramos con la arquitectura, la demo, el benchmark contra la teoría y las conclusiones.",
    "Ilustración de una hoja de ruta: seis estaciones numeradas unidas por un cable de fibra óptica que avanza de izquierda a derecha; "
    "las estaciones 3, 4 y 5 resaltadas en granate (motor del algoritmo) y las demás en gris pizarra. " + ESTILO_IMG)

# 3 ─ Problema
lam(1, "El problema", "El problema de FibraNet", "1:00", f'''
  <div class="eyebrow"><b>01</b> El problema</div>
  <h2>Red Dorsal Nacional de Fibra Óptica</h2>
  <div class="cols c46">
    <div style="display:flex;flex-direction:column;gap:20px;justify-content:center">
      <div class="panel acc"><span class="tag">Modelo</span><p style="font-size:24px">Cuatro vecinas por sede. Cada tramo se pesa por su distancia haversine.</p></div>
      <div class="stat"><div><b>25</b><span>sedes</span></div><div><b>65</b><span>candidatos</span></div><div><b>16&#8239;975,1</b><span>km en candidatos</span></div></div>
      <div class="stat"><div style="border-color:var(--ok)"><b style="color:var(--ok)">4&#8239;081,4</b><span>km en el plan</span></div><div style="border-color:var(--ok)"><b style="color:var(--ok)">76 %</b><span>menos km</span></div><div><b>24</b><span>tramos del plan</span></div></div>
    </div>
    {mapa("L03", "Tramos candidatos y plan de FibraNet en Perú", "candidatos")}
  </div>''',
    "Cada sede propone enlaces a sus cuatro vecinas más cercanas. Medimos la distancia con haversine. Al juntar las propuestas, contamos cada enlace una sola vez. "
    "Así quedan 65 tramos que suman 16 975,1 kilómetros. En el mapa, los tramos en granate forman el árbol. "
    "Ese árbol conecta las 25 sedes con 24 tramos y suma 4 081,4 kilómetros. Es un 76 por ciento menos que sumar todos los candidatos. "
    "Son distancias geodésicas del modelo. Para estimar el tendido real habría que estudiar las rutas de obra.",
    "Mapa del Perú con 25 sedes. Destaca los 24 tramos del árbol en granate y los demás candidatos en gris punteado. Sigue el grafo del simulador. " + ESTILO_IMG)


# 4 ─ Requerimientos
lam(1, "El problema", "Requerimientos y alcance", "1:00", '''
  <div class="eyebrow"><b>02</b> El problema</div>
  <h2>Requerimientos y alcance</h2>
  <div class="cols c3" style="grid-template-columns:1.25fr 1fr 1fr">
    <div class="panel acc"><span class="tag">Funcionales (14 RF)</span><ul class="clean">
      <li><b>RF-01/02</b> Cargar la red por CSV o teclado</li><li><b>RF-05</b> Plan con Kruskal</li><li><b>RF-06</b> Plan con Prim</li>
      <li><b>RF-07</b> Comparar ambos</li><li><b>RF-08</b> ¿Dos sedes quedan unidas?</li><li><b>RF-10</b> Bosque si la red no es conexa</li><li><b>RF-12</b> Exportar plan a CSV</li></ul></div>
    <div class="panel"><span class="tag">No funcionales (10 RNF)</span><ul class="clean">
      <li>Solo consola, salida en tablas</li><li>Java 11+, sin librerías externas</li><li>100 000 sedes en segundos</li><li>Código comentado bloque a bloque</li><li>Errores de CSV no detienen el programa</li></ul></div>
    <div class="panel gold"><span class="tag">Fuera de alcance</span><ul class="clean">
      <li>Interfaz gráfica</li><li>Mapa real o GPS</li><li>Costos que cambian en el tiempo</li><li>Redundancia (anillos de respaldo)</li></ul>
      <p style="font-size:17px;color:var(--muted);margin-top:12px">Detalle en <span class="m">01_requerimientos</span>: 8 casos de uso y trazabilidad.</p></div>
  </div>''',
    "Antes de programar levantamos los requerimientos. Son 14 funcionales: los más importantes son cargar la red desde un CSV o por teclado, "
    "calcular el plan con Kruskal y con Prim, compararlos, consultar si dos sedes quedan conectadas y exportar el plan. "
    "Si la red no es conexa, el programa no falla: entrega un bosque, un árbol por cada grupo. "
    "Los no funcionales piden consola, Java estándar y buen tiempo con redes grandes. Y dejamos claro lo que no hacemos, como una interfaz gráfica o redundancia.",
    "Ilustración de un portapapeles técnico con tres columnas de casillas marcadas: requisitos funcionales (granate), no funcionales (azul) "
    "y fuera de alcance (dorado, con casillas tachadas). A un lado, una terminal de consola con una tabla de texto. " + ESTILO_IMG)

# 5 ─ Modelo
lam(2, "Teoría del MST", "Modelo: grafo ponderado y MST", "1:10", '''
  <div class="eyebrow"><b>03</b> Teoría del MST</div>
  <h2>Modelo: la red es un grafo ponderado</h2>
  <div class="cols c46">
    <div style="display:flex;flex-direction:column;gap:16px">
      <div class="panel"><span class="tag">Traducción</span><table class="compact">
        <tr><td>Sede</td><td>vértice <span class="fx">v ∈ V</span></td></tr>
        <tr><td>Tramo posible</td><td>arista <span class="fx">(u, v) ∈ E</span></td></tr>
        <tr><td>Costo del tramo</td><td>peso <span class="fx">w(u, v)</span></td></tr>
        <tr><td>Plan de tendido</td><td>árbol <span class="fx">T ⊆ E</span>, |T| = |V| − 1</td></tr></table></div>
      <div class="panel acc"><span class="tag">Árbol de expansión mínima</span><p class="fx" style="font-size:26px">T* = arg min Σ<sub>(u,v)∈T</sub> w(u, v)</p><p style="font-size:18px;color:var(--muted)">conecta todos los vértices, sin ciclos, con la menor suma de pesos.</p></div>
    </div>
    <div class="panel" style="display:flex;flex-direction:column"><span class="tag">Ejemplo resuelto a mano (prueba automática)</span>
      <svg viewBox="0 0 640 400" class="mini" style="flex:1">
        <g class="e"><line x1="90" y1="90" x2="320" y2="90"/><text x="205" y="78">4</text></g>
        <g class="e t"><line x1="320" y1="90" x2="550" y2="90"/><text x="435" y="78">2</text></g>
        <g class="e t"><line x1="90" y1="90" x2="90" y2="310"/><text x="66" y="205">1</text></g>
        <g class="e t"><line x1="320" y1="90" x2="90" y2="310"/><text x="186" y="186">3</text></g>
        <g class="e t"><line x1="320" y1="90" x2="320" y2="310"/><text x="338" y="205">5</text></g>
        <g class="e t"><line x1="550" y1="90" x2="550" y2="310"/><text x="568" y="205">6</text></g>
        <g class="e"><line x1="90" y1="310" x2="320" y2="310"/><text x="205" y="340">7</text></g>
        <g class="e"><line x1="320" y1="310" x2="550" y2="310"/><text x="435" y="340">8</text></g>
        <g class="v"><circle cx="90" cy="90" r="26"/><text x="90" y="99">A</text><circle cx="320" cy="90" r="26"/><text x="320" y="99">B</text><circle cx="550" cy="90" r="26"/><text x="550" y="99">C</text>
        <circle cx="90" cy="310" r="26"/><text x="90" y="319">D</text><circle cx="320" cy="310" r="26"/><text x="320" y="319">E</text><circle cx="550" cy="310" r="26"/><text x="550" y="319">F</text></g>
      </svg>
      <p style="font-size:20px"><b style="color:var(--granate)">MST = 1 + 2 + 3 + 5 + 6 = 17</b> · 6 vértices → 5 aristas</p></div>
  </div>''',
    "Para resolverlo, primero modelamos. Cada sede es un vértice, cada tramo posible es una arista y su costo es el peso. "
    "Un plan de tendido válido es un árbol de expansión: toca todos los vértices, no tiene ciclos y por eso tiene exactamente V menos 1 aristas. "
    "Buscamos el de menor suma de pesos. A la derecha está el ejemplo que resolvimos a mano y que también es una prueba automática del programa: "
    "seis vértices, ocho aristas, y el árbol mínimo, en granate, cuesta 17.",
    "Diagrama limpio que muestra la traducción de un problema real a un grafo: a la izquierda edificios de oficinas conectados por cables; "
    "una flecha central; a la derecha los mismos puntos como círculos (vértices) unidos por líneas con números (pesos), "
    "con el árbol mínimo resaltado en granate. " + ESTILO_IMG)

# 6 ─ Corte y ciclo
lam(2, "Teoría del MST", "Propiedades de corte y de ciclo", "1:10", '''
  <div class="eyebrow"><b>04</b> Teoría del MST</div>
  <h2>Por qué funciona la estrategia voraz</h2>
  <div class="cols">
    <div class="panel acc" style="display:flex;flex-direction:column"><span class="tag">Propiedad de corte → la usa Prim</span>
      <svg viewBox="0 0 600 300" class="mini" style="flex:1">
        <path d="M300 10 C 250 100, 350 200, 300 290" class="corte"/>
        <g class="e"><line x1="110" y1="80" x2="490" y2="70"/><text x="380" y="62">9</text></g>
        <g class="e t"><line x1="130" y1="210" x2="470" y2="200"/><text x="250" y="190">3</text></g>
        <g class="e"><line x1="110" y1="80" x2="470" y2="200"/><text x="370" y="130">7</text></g>
        <g class="v"><circle cx="110" cy="80" r="20"/><circle cx="130" cy="210" r="20"/><circle cx="490" cy="70" r="20"/><circle cx="470" cy="200" r="20"/></g>
        <text x="120" y="280" class="lbl">S</text><text x="470" y="280" class="lbl">V − S</text>
      </svg>
      <p>En todo corte (S, V−S), la arista <b>más barata que lo cruza</b> pertenece a algún MST.</p></div>
    <div class="panel" style="display:flex;flex-direction:column"><span class="tag">Propiedad de ciclo → la usa Kruskal</span>
      <svg viewBox="0 0 600 300" class="mini" style="flex:1">
        <g class="e t"><line x1="150" y1="70" x2="450" y2="70"/><text x="300" y="58">2</text></g>
        <g class="e t"><line x1="450" y1="70" x2="300" y2="240"/><text x="400" y="170">4</text></g>
        <g class="e x"><line x1="150" y1="70" x2="300" y2="240"/><text x="200" y="170">8</text></g>
        <g class="v"><circle cx="150" cy="70" r="20"/><circle cx="450" cy="70" r="20"/><circle cx="300" cy="240" r="20"/></g>
        <text x="300" y="290" class="lbl">la arista 8 cerraría el ciclo → se descarta</text>
      </svg>
      <p>En todo ciclo, la arista <b>más cara</b> no está en ningún MST (si los pesos son distintos).</p></div>
  </div>''',
    "¿Por qué podemos ser voraces y elegir siempre lo más barato sin arrepentirnos? Por dos propiedades. "
    "La propiedad de corte: si partimos los vértices en dos grupos, la arista más barata que cruza entre ellos siempre está en algún árbol mínimo. Prim la usa en cada paso: su árbol es un lado del corte. "
    "La propiedad de ciclo: en cualquier ciclo, la arista más cara sobra. Kruskal la usa cuando rechaza una arista porque cerraría un ciclo, ya que esa arista es la más cara del ciclo que formaría.",
    "Ilustración conceptual dividida en dos: a la izquierda un grafo cortado por una línea curva punteada dorada en dos regiones S y V−S, "
    "con tres cables que cruzan el corte y el más delgado y barato resaltado en granate; a la derecha un triángulo de tres cables "
    "donde el más caro aparece tachado con una X roja. " + ESTILO_IMG)

# 7 ─ Especificación y complejidad
lam(2, "Teoría del MST", "Especificación y complejidad", "1:10", '''
  <div class="eyebrow"><b>05</b> Teoría del MST</div>
  <h2>Especificación formal y complejidad</h2>
  <div class="cols c46">
    <div class="panel"><span class="tag">Especificación (AS&amp;ST)</span><table class="compact grande">
      <tr><td><b>Entrada</b></td><td>G = (V, E, w), w : E → ℝ⁺</td></tr>
      <tr><td><b>Salida</b></td><td>T ⊆ E de costo mínimo</td></tr>
      <tr><td><b>Pre</b></td><td>|V| ≥ 1, pesos ≥ 0</td></tr>
      <tr><td><b>Post</b></td><td>T acíclico; si G es conexo, |T| = |V| − 1; si no, un árbol por componente</td></tr>
      <tr><td><b>Invariante</b></td><td>T siempre está contenido en algún bosque de expansión mínima (un árbol si G es conexo)</td></tr></table></div>
    <div class="panel acc"><span class="tag">Costo (V sedes, E tramos)</span><table class="compact grande">
      <tr><th>Operación</th><th>Peor O</th><th>Mejor Ω</th><th>Típico Θ</th></tr>
      <tr><td>Kruskal</td><td class="m">V + E log E</td><td class="m">V + E</td><td class="m">V + E log E</td></tr>
      <tr><td>Prim (heap)</td><td class="m">V + E log E</td><td class="m">V + E</td><td class="m">V + E log E</td></tr>
      <tr><td>find / union</td><td class="m">α(n)*</td><td class="m">1</td><td class="m">α(n)*</td></tr>
      <tr><td>UF ingenuo</td><td class="m">n</td><td class="m">1</td><td class="m">n</td></tr>
      <tr><td>Memoria</td><td colspan="3" class="m">O(V + E)</td></tr></table>
      <p style="font-size:16px;color:var(--muted);margin-top:8px">* amortizado. α(n) ≤ 4 para todo n práctico (inversa de Ackermann).</p></div>
  </div>''',
    "Formalmente, la entrada es un grafo con pesos no negativos y la salida un subconjunto de aristas de costo mínimo, sin ciclos. "
    "Si el grafo es conexo da V menos 1 aristas; si no, da un árbol por componente. "
    "El invariante que garantiza la correctitud es que el conjunto elegido siempre está contenido en algún bosque de expansión mínima; si la red es conexa, ese bosque es un árbol. "
    "En costo: los dos pagan V por recorrer las sedes; Kruskal ordena E aristas y el montículo de Prim perezoso puede guardar O(E) entradas. La cota general de los dos es O(V + E log E). "
    "Si no hay tramos repetidos entre el mismo par de sedes, E es a lo más V al cuadrado y log E es a lo más 2 log V, así que son del mismo orden. "
    "Union-Find optimizado cuesta alfa de n amortizado, que en la práctica es una constante menor o igual a 4.",
    "Ilustración de una pizarra académica con fórmulas de complejidad O(E log V) y α(n), y una gráfica pequeña de curvas que crecen "
    "a distinto ritmo (lineal, n log n, casi constante). Estilo limpio de libro técnico. " + ESTILO_IMG)

# 8 ─ Union-Find idea
lam(3, "Motor 1 · Union-Find", "Union-Find: la idea", "1:00", f'''
  <div class="eyebrow"><b>06</b> Motor 1 · Union-Find</div>
  <h2>Union-Find: cada grupo es un árbol</h2>
  <div class="cols c46">
    <div style="display:flex;flex-direction:column;gap:16px">
      <div class="panel acc"><span class="tag">Dos arreglos, nada más</span><p class="m" style="font-size:22px">int[] padre · int[] rango</p><p style="font-size:19px">Si <span class="m">padre[i] == i</span>, i es la <b>raíz</b>: el representante del grupo.</p></div>
      <div class="flow" style="flex-direction:column;gap:10px">
        <div class="st"><b>find(x)</b><p style="font-size:21px">Sube hasta la raíz. Al volver, cuelga cada nodo directo de ella (compresión de caminos).</p></div>
        <div class="st"><b>union(a, b)</b><p style="font-size:21px">Une dos raíces: el árbol más bajo va debajo del más alto (unión por rango). Devuelve false si ya estaban juntos.</p></div>
      </div>
    </div>
    {img("L08", "Bosque de árboles Union-Find")}
  </div>''',
    "Empieza el motor. Union-Find guarda grupos de elementos que no se mezclan. Cada grupo es un árbol y su raíz es el representante. "
    "En Java son solo dos arreglos de enteros: padre y rango. Si padre de i es i, i es una raíz. "
    "Find sube por los padres hasta la raíz, y de regreso hace que cada nodo apunte directo a ella: eso es compresión de caminos. "
    "Union busca las dos raíces y cuelga el árbol más bajo del más alto: eso es unión por rango. Con las dos optimizaciones, cada operación cuesta casi constante.",
    "Ilustración de un bosque con cuatro árboles pequeños hechos de nodos circulares unidos por flechas que apuntan hacia arriba, hacia la raíz; "
    "cada raíz tiene una corona dorada pequeña. Un árbol muestra un camino largo y, al lado, el mismo árbol 'aplanado' con todos los nodos "
    "apuntando directo a la raíz (compresión de caminos). Debajo, dos filas de casillas tipo arreglo etiquetadas 'padre' y 'rango'. " + ESTILO_IMG)

# 9 ─ Código UF
lam(3, "Motor 1 · Union-Find", "Union-Find en Java", "1:20", '''
  <div class="eyebrow"><b>07</b> Motor 1 · Union-Find</div>
  <h2>find y union en Java</h2>
  <div class="cols c64">
    <pre class="code" data-java="uf" data-mark="padre[x] = find|rango[raizA] <|rango[raizA]++|return false"></pre>
    <div style="display:flex;flex-direction:column;gap:14px">
      <div class="panel acc"><span class="tag">Compresión de caminos</span><p>La llamada recursiva devuelve la raíz y se guarda en <span class="m">padre[x]</span>: el camino queda plano.</p></div>
      <div class="panel"><span class="tag">Unión por rango</span><p>El más bajo se cuelga del más alto. El rango solo sube cuando hay empate.</p></div>
      <div class="panel gold"><span class="tag">Detecta ciclos</span><p><span class="m">return false</span>: ya estaban en el mismo grupo. Kruskal lo usa para detectar ciclos.</p></div>
    </div>
  </div>''',
    "Este es el código real, del archivo UnionFind.java. En find, la línea resaltada hace todo el trabajo: llama a find del padre, que devuelve la raíz, y la guarda en padre de x. "
    "Así, al terminar, todos los nodos del camino apuntan a la raíz. "
    "En union, primero se buscan las dos raíces. Si son la misma, devuelve false: eso significa que a y b ya estaban conectados, y Kruskal lo interpreta como ciclo. "
    "Si no, compara rangos: el más bajo va debajo. Solo en empate el rango sube en uno. Al final descontamos un grupo.",
    "Imagen de apoyo opcional: una lupa sobre un fragmento de código Java en un editor de tema claro, con dos líneas resaltadas en granate; "
    "al lado, un diagrama de flechas que muestra un camino de nodos que se acorta. " + ESTILO_IMG)

# 10 ─ Simulador UF
lam(3, "Motor 1 · Union-Find", "Simulador de Union-Find", "1:40", '''
  <div class="shd"><div class="eyebrow"><b>08</b> Motor 1 · Union-Find</div><h2 style="font-size:40px">Simulador: 7 uniones y 2 consultas</h2><span class="opact"></span></div>
  <div class="sim" id="simUF"><div class="bar"></div>
    <div class="body" style="grid-template-columns:1fr 470px">
      <svg viewBox="0 0 860 420" style="width:100%;height:100%;background:#fff;border:1.5px solid var(--rule)"></svg>
      <div style="display:flex;flex-direction:column;gap:12px"><span class="tag">Arreglos en memoria</span><table class="regs ufregs"></table><div class="note"></div></div>
    </div>
  </div>''',
    "Ahora lo vemos en acción. Tenemos ocho elementos, de A a H, y voy a avanzar paso a paso. "
    "Primero union de A y B: empate de rangos, B cuelga de A y el rango de A sube a 1. Igual con C y D. "
    "Luego union de A y C: dos raíces con rango 1, otro empate, y A queda con rango 2. "
    "Fíjense en la tabla: cada vez que cambia un padre, la casilla se resalta. "
    "Al final hacemos find de H: subimos varios saltos, y en el paso siguiente vemos la compresión: todo el camino queda colgado directo de la raíz.",
    "Esta lámina es un simulador interactivo hecho en HTML; no necesita imagen. Imagen opcional de apoyo: ocho fichas circulares con letras "
    "A a H que se agrupan en árboles mediante flechas, en tres momentos (separadas, en pares, en un solo árbol). " + ESTILO_IMG)

# 11 ─ Kruskal idea
lam(4, "Motor 2 · Kruskal", "Kruskal: la idea", "1:00", f'''
  <div class="eyebrow"><b>09</b> Motor 2 · Kruskal</div>
  <h2>Kruskal: del tramo más barato al más caro</h2>
  <div class="flow">
    <div class="st"><b>1 · Ordenar</b><h4>Tramos por costo</h4><p><span class="m">Collections.sort</span>, O(E log E)</p></div><div class="ar">→</div>
    <div class="st"><b>2 · Iniciar</b><h4>n grupos</h4><p>Cada sede sola: <span class="m">new UnionFind(n)</span></p></div><div class="ar">→</div>
    <div class="st"><b>3 · Probar</b><h4>union(u, v)</h4><p>¿Están en grupos distintos?</p></div><div class="ar">→</div>
    <div class="st" style="border-color:var(--ok)"><b style="color:var(--ok)">Sí → aceptar</b><h4>Entra al plan</h4><p>Dos grupos se vuelven uno.</p></div><div class="ar">·</div>
    <div class="st" style="border-color:#C0562F"><b style="color:#C0562F">No → rechazar</b><h4>Cerraría ciclo</h4><p>Se descarta.</p></div>
  </div>
  <div class="cols c64" style="min-height:0">
    {img("L11", "Kruskal como bosques que se fusionan")}
    <div class="panel acc"><span class="tag">Se detiene antes</span><p>Cuando el plan tiene <b>n − 1 tramos</b> el árbol está completo y no se revisan los demás.</p><p style="margin-top:10px">Si al final quedan <b>varios grupos</b>, la red no es conexa: el resultado es un bosque.</p></div>
  </div>''',
    "Kruskal piensa en todo el mapa a la vez. Paso uno: ordena los tramos del más barato al más caro. Paso dos: cada sede empieza en su propio grupo. "
    "Paso tres: recorre los tramos en orden y para cada uno pregunta a Union-Find si sus dos sedes están en grupos distintos. "
    "Si es así, lo acepta y une los grupos. Si ya estaban en el mismo grupo, ese tramo cerraría un ciclo y se rechaza. "
    "Cuando el plan llega a n menos 1 tramos se detiene. Y si al final quedan varios grupos, sabemos que la red no es conexa.",
    "Ilustración de Kruskal como islas que se fusionan: varios grupos pequeños de nodos de colores distintos que se van uniendo con puentes "
    "de fibra granate, empezando por los puentes más cortos; un puente que uniría dos nodos del mismo color aparece tachado. "
    "Al costado, una pila de tarjetas ordenadas por número (costos). " + ESTILO_IMG)

# 12 ─ Código Kruskal
lam(4, "Motor 2 · Kruskal", "Kruskal en Java", "1:20", '''
  <div class="eyebrow"><b>10</b> Motor 2 · Kruskal</div>
  <h2>Kruskal en Java</h2>
  <div class="cols c64">
    <pre class="code" data-java="kruskal" data-mark="Collections.sort|new UnionFind|grupos.union|== n - 1"></pre>
    <div style="display:flex;flex-direction:column;gap:14px">
      <div class="panel"><span class="tag">Ordenar · O(E log E)</span><p>Se copia la lista para no alterar la red original. <span class="m">Arista</span> implementa <span class="m">Comparable</span> por costo.</p></div>
      <div class="panel acc"><span class="tag">Una sola línea decide</span><p><span class="m">grupos.union(origen, destino)</span> responde y une a la vez: true = aceptar, false = ciclo.</p></div>
      <div class="panel gold"><span class="tag">Parada temprana</span><p>Con <span class="m">n − 1</span> tramos el árbol está completo.</p></div>
    </div>
  </div>''',
    "Este es el núcleo de Kruskal.java. Primero se copia la lista de aristas y se ordena con Collections.sort; la clase Arista compara por costo. "
    "Luego se crea un Union-Find con n sedes. En el bucle, la línea clave es grupos punto union: en una sola llamada pregunta si están en grupos distintos y, si lo están, los une. "
    "Si devuelve true, el tramo entra al plan. Si devuelve false, se descarta por ciclo. "
    "Y la condición del inicio corta el bucle apenas tenemos n menos 1 tramos.",
    "Imagen de apoyo opcional: diagrama de flujo vertical de Kruskal con cuatro cajas (ordenar, recorrer, union, aceptar o rechazar) y "
    "un rombo de decisión, en el estilo de un plano técnico. " + ESTILO_IMG)

# 13 ─ Simulador Kruskal
lam(4, "Motor 2 · Kruskal", "Simulador de Kruskal", "1:40", '''
  <div class="shd"><div class="eyebrow"><b>11</b> Motor 2 · Kruskal</div><h2 style="font-size:40px">Simulador: Kruskal en la Red Dorsal</h2></div>
  <div class="sim" id="simK"><div class="bar"></div>
    <div class="body" style="grid-template-columns:1fr 520px">
      <svg style="width:100%;height:100%;background:#fff;border:1.5px solid var(--rule)"></svg>
      <div style="display:flex;flex-direction:column;gap:10px;min-height:0"><div class="stat costo"></div><div class="lista"></div><div class="note"></div></div>
    </div>
  </div>''',
    "Empezamos con 25 grupos y los 65 candidatos ordenados por distancia. Pulso Paso. El enlace Callao–Lima, de 8,3 kilómetros, une dos grupos y entra al árbol. "
    "Avanzo hasta que se rechace un enlace. Sus extremos ya están conectados. Si lo agregáramos, cerraríamos un ciclo. "
    "Pulso Todo. El plan tiene 24 tramos, un solo grupo y 4 081,4 kilómetros. Ahora veremos cómo Prim llega al mismo costo.",
    "Simulador interactivo. Imagen opcional: tramos del árbol que se destacan en orden creciente de longitud. " + ESTILO_IMG)


# 14 ─ Prim idea
lam(5, "Motor 3 · Prim", "Prim: la idea", "1:00", f'''
  <div class="eyebrow"><b>12</b> Motor 3 · Prim</div>
  <h2>Prim: un solo árbol que crece</h2>
  <div class="cols c46">
    <div style="display:flex;flex-direction:column;gap:14px">
      <div class="flow" style="flex-direction:column;gap:10px">
        <div class="st"><b>1 · Empezar</b><p>Una sede entra al árbol. Sus tramos van a la <strong>cola de prioridad</strong>.</p></div>
        <div class="st"><b>2 · Sacar el mínimo</b><p><span class="m">cola.poll()</span>: el tramo más barato en O(log E).</p></div>
        <div class="st" style="border-color:var(--ok)"><b style="color:var(--ok)">3 · ¿Llega afuera?</b><p>Sí: la sede nueva entra y se encolan sus tramos.</p></div>
        <div class="st" style="border-color:#C0562F"><b style="color:#C0562F">· Ya está adentro</b><p>Se descarta (versión <strong>perezosa</strong>).</p></div>
      </div>
    </div>
    {img("L14", "Prim como mancha que crece desde una sede")}
  </div>''',
    "Prim piensa distinto: no mira todo el mapa, sino que hace crecer un solo árbol desde una sede. "
    "La sede inicial entra al árbol y todos sus tramos van a una cola de prioridad, que es un montículo binario. "
    "En cada paso sacamos el tramo más barato. Si llega a una sede que todavía está afuera, esa sede entra y encolamos sus tramos. "
    "Si las dos puntas ya están adentro, lo descartamos: por eso se llama versión perezosa, porque no borramos esos tramos de la cola, solo los ignoramos al salir. "
    "Esto es justo la propiedad de corte: el árbol es un lado del corte y siempre tomamos el cruce más barato.",
    "Ilustración de Prim como una mancha de tinta granate que se expande desde una sede central sobre un mapa esquemático: las sedes ya "
    "cubiertas están en granate, la frontera tiene varias líneas candidatas en azul y la más corta resaltada en dorado; "
    "a un lado, un montículo (heap) dibujado como árbol binario con el número más pequeño arriba. " + ESTILO_IMG)

# 15 ─ Código Prim
lam(5, "Motor 3 · Prim", "Prim en Java", "1:20", '''
  <div class="eyebrow"><b>13</b> Motor 3 · Prim</div>
  <h2>Prim en Java</h2>
  <div class="cols c64">
    <div style="display:flex;flex-direction:column;gap:12px;min-height:0">
      <pre class="code" data-java="prim" data-mark="cola.poll|origenDentro && destinoDentro|visitar(grafo, nueva"></pre>
      <pre class="code" data-java="visitar" data-mark="enArbol[sede] = true|cola.add"></pre>
    </div>
    <div style="display:flex;flex-direction:column;gap:14px">
      <div class="panel acc"><span class="tag">PriorityQueue = montículo</span><p><span class="m">poll()</span> y <span class="m">add()</span> cuestan O(log E). Cada tramo entra a lo más dos veces.</p></div>
      <div class="panel"><span class="tag">boolean[] enArbol</span><p>Marca en O(1) qué sedes ya están dentro. Memoria O(V).</p></div>
      <div class="panel gold"><span class="tag">Red no conexa</span><p>Si la cola se vacía y faltan sedes, se inicia otro árbol: un bosque del mismo costo que el de Kruskal (los tramos pueden variar si hay empates).</p></div>
    </div>
  </div>''',
    "Este es el núcleo de Prim.java. Mientras la cola no esté vacía, sacamos el mínimo con poll. "
    "Revisamos con el arreglo enArbol si las dos puntas ya están dentro; si es así, continue, se descarta. "
    "Si no, la punta que está afuera es la sede nueva: agregamos el tramo al plan y llamamos a visitar. "
    "Visitar, abajo, marca la sede y mete en la cola solo los tramos que van hacia sedes de afuera. "
    "Si la red no es conexa, el bucle externo arranca otro árbol desde una sede no visitada, así Prim también entrega el bosque.",
    "Imagen de apoyo opcional: un montículo binario (heap) dibujado como árbol con números, con el menor en la cima siendo extraído "
    "por una mano o pinza; junto a él una fila de casillas booleanas 'enArbol' con algunas marcadas. " + ESTILO_IMG)

# 16 ─ Simulador Prim
lam(5, "Motor 3 · Prim", "Simulador de Prim", "1:40", '''
  <div class="shd"><div class="eyebrow"><b>14</b> Motor 3 · Prim</div><h2 style="font-size:40px">Simulador: Prim desde Lima</h2></div>
  <div class="sim" id="simP"><div class="bar"></div>
    <div class="body" style="grid-template-columns:1fr 520px">
      <svg style="width:100%;height:100%;background:#fff;border:1.5px solid var(--rule)"></svg>
      <div style="display:flex;flex-direction:column;gap:10px;min-height:0"><div class="stat costo"></div><div class="lista"></div><div class="note"></div></div>
    </div>
  </div>''',
    "Prim empieza en Lima. En la cola se ve el tramo más barato arriba. Pulso Paso. Callao entra por el enlace de 8,3 kilómetros. "
    "Cada sede nueva añade a la cola sus conexiones hacia las sedes de afuera. Si sale un enlace con ambos extremos dentro, se descarta. "
    "Pulso Todo. Ahora las 25 sedes están en el árbol. El total es 4 081,4 kilómetros, igual que en Kruskal.",
    "Simulador interactivo. Imagen opcional: secuencia del crecimiento del árbol desde Lima. " + ESTILO_IMG)


# 17 ─ Arquitectura y demo
lam(6, "Resultados", "Arquitectura y demo por consola", "1:20", '''
  <div class="eyebrow"><b>15</b> Resultados</div>
  <h2>Arquitectura y demo por consola</h2>
  <div class="cols" style="grid-template-columns:1fr 1.1fr">
    <div style="display:flex;flex-direction:column;gap:10px">
      <div class="pk"><b>fibranet.Main</b><span>menú 0–10 · Scanner</span></div>
      <div class="pkrow"><div class="pk"><b>io</b><span>LectorCoordenadas · LectorCsv</span></div><div class="pk acc"><b>motor</b><span>UnionFind · Kruskal · Prim · ResultadoMST</span></div></div>
      <div class="pk"><b>modelo</b><span>Grafo (listas de adyacencia) · Arista (Comparable)</span></div>
      <div class="pkrow"><div class="pk gold"><b>pruebas</b><span>47 verificaciones · OK/FALLA</span></div><div class="pk gold"><b>benchmark</b><span>semilla 2026 · CSV</span></div></div>
      <p style="font-size:17px;color:var(--muted)">Solo librería estándar · <span class="m">javac</span> sin Maven · Java 11+ · repositorio <span class="m">HitLP38/Algoritmia_trabajoF</span></p>
    </div>
    <div class="dos"><span class="p">Opción 10 · Red Dorsal Nacional</span>
capitales_peru.csv
25 sedes · 65 candidatos · k = 4

<span class="p">Comparación del plan</span>
Kruskal       24 tramos      4 081,4 km
Prim          24 tramos      4 081,4 km
Candidatos    65 tramos     16 975,1 km

<span class="p">76 % menos kilómetros en el modelo</span></div>
  </div>''',
    "En la consola elijo la opción diez. LectorCoordenadas lee las latitudes y longitudes del CSV. Calcula las distancias y arma la lista de candidatos. "
    "El motor recibe ese mismo grafo para Kruskal y Prim. A la derecha vemos el resultado de los dos: 24 enlaces y 4 081,4 kilómetros. "
    "La versión actual pasó 47 verificaciones. Entre ellas está la comparación de los dos algoritmos en este caso. "
    "La demo de consola entra en este tiempo. Si falla la apertura, la lámina ya contiene la salida resumida.",
    "Diagrama de Main, LectorCoordenadas y Kruskal/Prim junto a la salida resumida de la opción 10. " + ESTILO_IMG)


# 18 ─ Benchmark MST
lam(6, "Resultados", "Benchmark: Kruskal vs Prim", "1:20", f'''
  <div class="eyebrow"><b>16</b> Resultados</div>
  <h2>Benchmark: Kruskal vs Prim</h2>
  <div class="cols" style="grid-template-columns:1.15fr 1fr">
    <svg id="grafMST" style="width:100%;height:100%;min-height:0"></svg>
    <div style="display:flex;flex-direction:column;gap:12px">
      {tabla_mst()}
      <div class="panel acc"><span class="tag">Contraste con la teoría</span><p>{MST_CONTRASTE}</p></div>
      <p style="font-size:16px;color:var(--muted)">Semilla 2026 · 2 calentamientos · 5 corridas · ± desviación estándar.</p>
      {AVISO}
    </div>
  </div>''',
    "Este benchmark se hizo con grafos sintéticos, de mil a cincuenta mil sedes. Para cada tamaño promediamos cinco corridas, después de dos de calentamiento. "
    f"En el grafo denso de cincuenta mil sedes, Kruskal promedió {MST_TIEMPOS[0]} milisegundos y Prim {MST_TIEMPOS[1]}. "
    f"{MST_ORAL}",
    "Imagen de apoyo opcional: un cronómetro estilizado junto a dos curvas ascendentes, una granate y otra azul, que compiten en una carrera "
    "sobre una cuadrícula técnica. " + ESTILO_IMG)

# 19 ─ Benchmark UF
lam(6, "Resultados", "Benchmark: Union-Find", "1:10", f'''
  <div class="eyebrow"><b>17</b> Resultados</div>
  <h2>Benchmark: Union-Find optimizado vs ingenuo</h2>
  <div class="cols" style="grid-template-columns:1.15fr 1fr">
    <svg id="grafUF" style="width:100%;height:100%;min-height:0"></svg>
    <div style="display:flex;flex-direction:column;gap:12px">
      {tabla_uf()}
      <div class="panel acc"><span class="tag">Contraste con la teoría</span><p>Ingenuo: ns/op crece <b>lineal con n</b> (árboles en forma de lista). Optimizado: ns/op <b>casi plano</b> hasta 1 000 000, como predice α(n).</p></div>
      {AVISO}
    </div>
  </div>''',
    "Aquí comparamos Union-Find sin optimizar contra el optimizado, en escala logarítmica en los dos ejes. "
    "La versión ingenua aumenta su costo por operación de forma general, aunque los puntos no crecen de manera uniforme. La secuencia de uniones forma cadenas largas. "
    f"La versión con compresión y unión por rango se mantiene casi plana, entre {UF_MIN} y {UF_MAX} nanosegundos por operación desde cinco mil hasta un millón de elementos. "
    f"Con veinte mil elementos ya es unas {UF_VECES_TXT} veces más rápida. "
    "El crecimiento observado es compatible con el costo amortizado de la estructura optimizada.",
    "Imagen de apoyo opcional: comparación visual de dos árboles, uno alto y delgado como una escalera (ingenuo) y otro bajo y ancho como un "
    "arbusto (optimizado), con un velocímetro a cada lado. " + ESTILO_IMG)

# 20 ─ Conclusiones
lam(6, "Resultados", "Conclusiones", "1:00", f'''
  <div class="eyebrow"><b>18</b> Resultados</div>
  <h2>Conclusiones y trabajo futuro</h2>
  <div class="cols c3 concl">
    <div class="panel acc"><span class="tag">1 · Resultado</span><b class="kpi">4&#8239;081,4 <small>km</small></b><p>24 tramos para 25 sedes. <b>76 % menos km</b> frente a los 16&#8239;975,1 km candidatos.</p></div>
    <div class="panel acc"><span class="tag">2 · Benchmark</span><b class="kpi">E log V</b><p>Kruskal y Prim dan el mismo costo. En el benchmark denso de 50 000 sedes, Kruskal promedió {MST_TIEMPOS[0]} ms y Prim {MST_TIEMPOS[1]} ms.</p></div>
    <div class="panel acc"><span class="tag">3 · Cómo eligen</span><b class="kpi">K <small>o</small> P</b><p><b>Kruskal</b> ordena los candidatos. <b>Prim</b> elige desde la frontera del árbol.</p></div>
  </div>
  <div class="panel gold"><span class="tag">Trabajo futuro</span><p>Evaluar trazados de obra, costos por tramo y enlaces de respaldo.</p></div>''',
    "El plan conecta las 25 sedes con 24 tramos. Suma 4 081,4 kilómetros, frente a 16 975,1 de los candidatos. "
    "Kruskal y Prim obtienen la misma longitud mínima. Se diferencian en cómo eligen el siguiente enlace. Kruskal ordena toda la lista. Prim mantiene una cola que crece con el árbol. "
    "En el grafo denso más grande de nuestra prueba, Kruskal fue más rápido. Para acercarnos a un diseño de obra, falta evaluar rutas, costos por tramo y enlaces de respaldo.",
    "Ilustración de cierre: una red de fibra granate completa sobre un mapa esquemático del Perú, con tres medallas o insignias "
    "que representan ahorro, teoría comprobada y elección del algoritmo. " + ESTILO_IMG)

# 21 ─ Referencias
lam(6, "Resultados", "Referencias", "0:20", '''
  <div class="eyebrow"><b>19</b> Resultados</div>
  <h2>Referencias</h2>
  <ol class="refs">
    <li>T. H. Cormen, C. E. Leiserson, R. L. Rivest y C. Stein, <i>Introduction to Algorithms</i>, 4th ed. Cambridge, MA, USA: MIT Press, 2022.</li>
    <li>J. B. Kruskal, "On the shortest spanning subtree of a graph and the traveling salesman problem," <i>Proc. Amer. Math. Soc.</i>, vol. 7, no. 1, pp. 48–50, 1956.</li>
    <li>R. C. Prim, "Shortest connection networks and some generalizations," <i>Bell Syst. Tech. J.</i>, vol. 36, no. 6, pp. 1389–1401, 1957.</li>
    <li>R. E. Tarjan, "Efficiency of a good but not linear set union algorithm," <i>J. ACM</i>, vol. 22, no. 2, pp. 215–225, 1975.</li>
    <li>R. Sedgewick y K. Wayne, <i>Algorithms</i>, 4th ed. Boston, MA, USA: Addison-Wesley, 2011.</li>
    <li>S. S. Skiena, <i>The Algorithm Design Manual</i>, 3rd ed. Cham, Switzerland: Springer, 2020.</li>
    <li>Oracle, "PriorityQueue (Java SE 11 &amp; JDK 11)," <i>Java Platform API Specification</i>, 2018.</li>
  </ol>''',
    "Estas son las referencias en formato IEEE, las mismas del informe. Las principales son el libro de Cormen, los artículos originales de Kruskal y de Prim, "
    "y el artículo de Tarjan que demuestra la cota de alfa de n para Union-Find.",
    "Imagen de apoyo opcional: una pila de libros técnicos de algoritmos con lomos en granate, dorado y azul, junto a una taza y una laptop. " + ESTILO_IMG)

# 22 ─ Preguntas y gracias
lam(6, "Cierre", "Preguntas y gracias", "0:30", f'''
  <div class="inst">{INST}</div>
  <div style="flex:1;display:flex;flex-direction:column;justify-content:center;align-items:center;text-align:center;gap:18px">
    <div class="eyebrow"><b>FIBRANET</b> Grupo 5</div>
    <h1 style="font-size:130px;line-height:.9">¿Preguntas?</h1>
    <p class="lead" style="font-size:28px;max-width:none">¡Gracias por su atención!</p>
    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:22px;margin-top:10px;text-align:left;width:100%">
      <div class="panel"><span class="tag">Recordar 1</span><p>Un árbol con n sedes tiene <b>n − 1</b> tramos.</p></div>
      <div class="panel"><span class="tag">Recordar 2</span><p><span class="m">union</span> devuelve false → el tramo cerraría un <b>ciclo</b>.</p></div>
      <div class="panel"><span class="tag">Recordar 3</span><p>Kruskal y Prim: mismo costo, <b>O(V + E log V)</b>.</p></div>
    </div>
  </div>
  <div style="display:grid;grid-template-columns:1.4fr 1fr;gap:40px;align-items:end">
    <div class="team teamlist"></div>
    <div style="font-size:18px;color:var(--muted);text-align:right">Docente: <b style="color:var(--ink)">{DOCENTE}</b><br>Lima, 2026</div>
  </div>''',
    "Con esto terminamos. Dejamos tres ideas para recordar: un árbol con n sedes tiene n menos 1 tramos; si union devuelve false, el tramo cerraría un ciclo; "
    "y Kruskal y Prim dan el mismo costo en tiempo E log V. Muchas gracias. Quedamos atentos a sus preguntas; cualquiera de nosotros puede responder sobre el motor.",
    "Ilustración de cierre: un cable de fibra óptica granate que forma un signo de interrogación luminoso sobre un fondo de cuadrícula "
    "técnica, con pequeños nodos dorados a lo largo del cable. " + ESTILO_IMG,
    cls="cover thanks", nocaj=True)

# ---------------------------------------------------------------------------
# CSS propio de FibraNet (se suma al del Laboratorio 2)
# ---------------------------------------------------------------------------
CSS_EXTRA = """
.bench-mst th,.bench-mst td{font-size:14px;white-space:nowrap}
.cover h1{font-size:120px}
.imgbox{flex:1;min-height:0;border:1.5px solid var(--rule);background:#fff;display:flex;align-items:center;justify-content:center;overflow:hidden}
.mapa-caso{position:relative;min-height:0;border:1.5px solid var(--rule);background:#fff;display:grid}
.mapa-caso svg{width:100%;height:100%;min-height:0}
.mapa-caso .imgbox{position:absolute;inset:0;border:0}
.mapa-caso .imgbox:not(:has(img[src])),.mapa-caso .imgbox.falta{display:none}
.imgbox img{max-width:100%;max-height:100%;object-fit:contain;cursor:zoom-in;display:block}
.imgbox.falta{border-style:dashed;background:repeating-linear-gradient(135deg,#fff 0 14px,var(--oro-soft) 14px 28px)}
.imgbox .ph{max-width:460px;text-align:center;color:var(--muted);font-size:18px;line-height:1.45;background:#fff;padding:18px 22px;border:1.5px solid var(--oro)}
.imgbox .ph b{display:block;font-family:var(--f-display);font-stretch:85%;font-size:28px;color:var(--ink);margin-bottom:6px}
.m{font-family:var(--f-mono);font-size:.88em}
.fx{font-family:var(--f-math)}
ul.clean{margin:0;padding-left:22px;font-size:22px;line-height:1.55}
table.grande{font-size:22px}table.grande td{padding:9px 10px}
.code{font-variant-ligatures:none}
.flow .st strong{font-weight:700;color:var(--ink)}
.concl .panel{display:flex;flex-direction:column;gap:12px}.concl p{font-size:24px;line-height:1.35}
.kpi{font-family:var(--f-display);font-stretch:75%;font-size:76px;line-height:1;color:var(--granate)}.kpi small{font-size:34px;color:var(--muted)}
.aviso{font:600 14px var(--f-mono);color:#8A5A00;background:var(--oro-soft);border-left:4px solid var(--oro);padding:8px 12px}
.opact{margin-left:auto;font:700 22px var(--f-mono);color:var(--granate);background:var(--granate-soft);padding:6px 14px}
.code{font-size:18px}
.flow .st p{font-size:20px;color:var(--ink)}
svg.mini line{stroke:#B8C2CC;stroke-width:3}
svg.mini .e text{font:700 22px var(--f-mono);fill:var(--muted);text-anchor:middle}
svg.mini .e.t line{stroke:var(--granate);stroke-width:7}
svg.mini .e.t text{fill:var(--granate)}
svg.mini .e.x line{stroke:#C0562F;stroke-dasharray:8 8}
svg.mini .e.x text{fill:#C0562F}
svg.mini .v circle{fill:#fff;stroke:var(--ink);stroke-width:3}
svg.mini .v text{font:700 24px var(--f-display);text-anchor:middle}
svg.mini .corte{fill:none;stroke:var(--oro);stroke-width:4;stroke-dasharray:12 8}
svg.mini .lbl{font:600 20px var(--f-body);fill:var(--muted);text-anchor:middle}
/* simuladores */
.ufe{stroke:#566476;stroke-width:2.5}
.ufn{fill:#fff;stroke:var(--ink);stroke-width:2.5}
.ufn.raiz{stroke:var(--granate);stroke-width:4.5}
.ufn.on{fill:var(--oro-soft);stroke:var(--one)}
.ufl{font:700 26px var(--f-mono);text-anchor:middle;fill:var(--ink)}
table.ufregs td{text-align:center;padding:8px 6px}
table.ufregs td.chg{background:var(--oro-soft);color:var(--granate);font-weight:700}
.tr.oculto{display:none}
.tr{stroke:#C9D1DA;stroke-width:3;stroke-dasharray:6 6}
.tr.acc{stroke:var(--granate);stroke-width:7;stroke-dasharray:none}
.tr.rej{stroke:#C0562F;stroke-width:3;stroke-dasharray:3 7;opacity:.7}
.tr.cola{stroke:var(--g2);stroke-width:3.5;stroke-dasharray:none;opacity:.75}
.tr.cur{stroke:var(--one)!important;stroke-width:10!important;opacity:1!important;stroke-dasharray:none!important}
.cl rect{fill:#fff;stroke:#C9D1DA;stroke-width:1.5}
.cl text{font:700 15px var(--f-mono);text-anchor:middle;fill:var(--muted)}
.cl.acc rect{stroke:var(--granate);fill:var(--granate-soft)}.cl.acc text{fill:var(--granate)}
.cl.cur rect{fill:var(--oro-soft);stroke:var(--one)}
.sd{stroke:var(--ink);stroke-width:2.5}
.sdl{font:600 13px var(--f-body);text-anchor:middle;fill:var(--ink);paint-order:stroke;stroke:#fff;stroke-width:5px}
.sim .stat b{font-size:38px}
.lista{position:relative;display:flex;flex-direction:column;gap:3px;font-size:15px;overflow:auto;min-height:0;flex:1}
.lista .tag{margin:0 0 2px}
.lista .it{display:grid;grid-template-columns:64px 1fr auto;gap:8px;padding:2px 8px;border:1px solid var(--grid);background:var(--sheet)}
.lista .it span{font-family:var(--f-mono);font-weight:700}
.lista .it b{font:600 13px var(--f-mono)}
.lista .it.acc{border-color:var(--granate);background:var(--granate-soft)}.lista .it.acc b{color:var(--granate)}
.lista .it.rej{color:#A0522D;background:#FBEFE8}
.lista .it.cur{box-shadow:0 0 0 3px var(--one);background:var(--oro-soft)}
.lista .it.top{border-color:var(--g2);background:#E6EEF8}
.lista .it.obs{opacity:.5}
.lista .it.mas{grid-template-columns:1fr;color:var(--muted)}
.sim .note{font-size:17px;min-height:96px}
/* arquitectura */
.pk{border:2px solid var(--ink);background:var(--sheet);padding:12px 16px;display:flex;flex-direction:column}
.pk b{font:700 20px var(--f-mono)}.pk span{font-size:17px;color:var(--muted)}
.pk.acc{border-color:var(--granate);background:var(--granate-soft)}.pk.acc b{color:var(--granate)}
.pk.gold{border-color:var(--oro);background:var(--oro-soft)}
.pkrow{display:grid;grid-template-columns:1fr 1fr;gap:10px}
.dos{font-size:17px}
.ax{font:500 14px var(--f-mono);fill:var(--muted)}.axl{font:600 15px var(--f-body);fill:var(--ink)}.lg{font:500 15px var(--f-body);fill:var(--ink)}
ol.refs{font-size:24px;line-height:1.45;margin:0;padding-left:30px;display:flex;flex-direction:column;gap:8px}
"""

HUD = '''<div id="hud"><button class="navbtn" id="bPrev" aria-label="Anterior">◀</button><button class="navbtn" id="bNext" aria-label="Siguiente">▶</button><button class="navbtn" id="bOv">Índice (O)</button><button class="navbtn" id="bNotes">Guion (N)</button><button class="navbtn" id="bFs">Pantalla (F)</button><button class="navbtn" id="bPdf">Exportar PDF (P)</button></div>
<div id="notes" hidden><h5 id="notesH"></h5><div id="notesB"></div></div>
<div id="overview" hidden></div>
<div id="printTip" hidden><div class="box"><h4>Exportar a PDF</h4><ol><li>Destino: <b>Guardar como PDF</b>.</li><li>Márgenes: <b>Ninguno</b>.</li><li>Activar <b>Gráficos de fondo</b>.</li></ol><div class="row"><button id="ptCancel">Cancelar</button><button class="pri" id="ptGo">Abrir impresión</button></div></div></div>'''


def construir_html():
    meta = dict(curso=CURSO, tarea="Tema 5.a FibraNet", team=EQUIPO, codes=[""] * 6, blocks=BLOQUES,
                slides=[dict(owner=l["owner"], block=l["block"], title=l["title"], time=l["time"], notes=l["notes"]) for l in L])
    secciones = "\n".join(
        f'<section class="slide {l["cls"]}"{" data-nocaj=\"1\"" if l["nocaj"] else ""}>{l["html"]}\n</section>' for l in L)
    import csv
    with open(os.path.join(COD, "datos", "capitales_peru.csv"), encoding="utf-8", newline="") as archivo:
        capitales = [[fila["capital"], float(fila["latitud"]), float(fila["longitud"])]
                     for fila in csv.DictReader(archivo)]
    js = ("const CAPITALES = " + json.dumps(capitales, ensure_ascii=False) + ";\n"
          "const META = " + json.dumps(meta, ensure_ascii=False) + ";\n"
          "// Códigos de alumno: escríbalos en META.codes, en el mismo orden que META.team.\n"
          "const CODIGO = " + json.dumps(CODIGO, ensure_ascii=False) + ";\n"
          "const BENCH = " + json.dumps(BENCH, ensure_ascii=False) + ";\n")
    return f'''<!DOCTYPE html>
<html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>FibraNet · Grupo 5</title>
<style>{leer(os.path.join(AQUI, "deck_fuentes.css"))}
{leer(os.path.join(AQUI, "deck_base.css"))}
{CSS_EXTRA}</style></head>
<body>
<div id="bar"></div>
<div id="viewport"><div id="stage">
{secciones}
</div></div>
{HUD}
<script>
{js}
{leer(os.path.join(AQUI, "deck_motor.js"))}
{leer(os.path.join(AQUI, "sims_5a.js"))}
</script>
</body></html>'''


def escribir(ruta, texto):
    os.makedirs(os.path.dirname(ruta), exist_ok=True)
    open(ruta, "w", encoding="utf-8", newline="\n").write(texto)


def prompts():
    for i, l in enumerate(L, 1):
        k = f"L{i:02d}"
        con_img = f'data-img="{k}"' in l["html"]
        uso = (f"Esta imagen SE USA en la lámina. Guárdela como:\n  5a_FibraNet\\03_ppt\\img\\{k}.png\n"
               if con_img else
               "Esta lámina ya es visual en el HTML (código, simulador, tabla o gráfico). La imagen es OPCIONAL:\n"
               f"úsela como apoyo en el informe o en un póster; si la genera, guárdela como 5a_FibraNet\\03_ppt\\img\\{k}.png.\n")
        escribir(os.path.join(TEMA, "04_slides", f"{k}.txt"),
                 f"Lámina {i:02d} · {l['title']}\nExpone: {EQUIPO[l['owner'] - 1]}\n\n{uso}\n"
                 f"PROMPT PARA CHATGPT (copiar desde aquí):\n\n{l['prompt']}\n\n"
                 "Si la imagen sale con texto deformado, pida: \"Repite la imagen sin ningún texto\".\n")


def guiones(deck="FibraNet_Slides.html", sufijo=""):
    def guardar(nombre, lineas):
        ruta = os.path.join(TEMA, "05_guiones", f"{nombre}{sufijo}.md")
        if sufijo:
            with open(ruta, "x", encoding="utf-8", newline="\n") as archivo:
                archivo.write("\n".join(lineas) + "\n")
        else:
            escribir(ruta, "\n".join(lineas) + "\n")

    total = lambda o: sum(int(l["time"].split(":")[0]) * 60 + int(l["time"].split(":")[1]) for l in L if l["owner"] == o)
    mmss = lambda s: f"{s // 60}:{s % 60:02d}"
    # 1) División de trabajo
    d = ["# División de trabajo · 5.a FibraNet (expo martes 08/12/2026)", "",
         "Regla del grupo: **todos** deben poder explicar el motor (Union-Find, Kruskal y Prim). Por eso cada integrante,"
         " además de su bloque, estudia una parte del motor y puede responder preguntas sobre ella.", "",
         "| # | Integrante | Láminas | Bloque | Tiempo | Archivos de código que domina |", "|---|---|---|---|---|---|"]
    domina = {1: "Main.java · LectorCoordenadas.java · Geo.java", 2: "Grafo.java · Arista.java (compareTo)", 3: "UnionFind.java · UnionFindIngenuo.java",
              4: "Kruskal.java · ResultadoMST.java", 5: "Prim.java · Grafo.tramosDe", 6: "Benchmark.java · Pruebas.java · Tabla.java"}
    for o in range(1, 7):
        nums = [i for i, l in enumerate(L, 1) if l["owner"] == o]
        b = next(x for x in BLOQUES if x["owner"] == o)
        d.append(f"| C{o} | {EQUIPO[o - 1]} | {nums[0]:02d}–{nums[-1]:02d} | {b['name']} | {mmss(total(o))} | {domina[o]} |")
    tot = sum(total(o) for o in range(1, 7))
    d += ["", f"Tiempo total estimado: **{mmss(tot)}** (incluye la demo breve de la lámina 17. Las preguntas van después).", "",
          "## Tareas antes de la expo", "",
          "| Fecha | Tarea | Responsable |", "|---|---|---|",
          "| 02/12 | Comprobar las imágenes y la opción 10 en el equipo de exposición | Todos |",
          "| 03/12 | Leer su guion en voz alta y cronometrarlo | Todos |",
          "| 04/12 | Repasar el motor: cada quien explica a otro un archivo que no es suyo | Todos (en parejas C1–C3, C2–C4, C5–C6) |",
          "| 06/12 | Estudio del código en grupo: recorrido completo de `UnionFind`, `Kruskal` y `Prim` | Todos |",
          "| 07/12 | Ensayo general con el deck y demo en vivo | Todos |", "",
          "## Cómo usar el deck", "",
          f"Abrir `5a_FibraNet\\03_ppt\\{deck}` en Chrome o Edge. Teclas: flechas para avanzar, **N** guion, **O** índice, **F** pantalla completa, **P** exportar PDF.",
          "En las láminas 10, 13 y 16 use los botones **Paso ▶** del simulador."]
    guardar("division_de_trabajo", d)
    # 2) Guion completo
    g = ["# Guion de exposición · 5.a FibraNet", "",
         "Cada bloque indica quién habla, el tiempo objetivo y el texto sugerido. No hay que memorizarlo palabra por palabra: la idea es decir esto con sus palabras.", ""]
    for o in range(1, 7):
        b = next(x for x in BLOQUES if x["owner"] == o)
        g += [f"## C{o} · {EQUIPO[o - 1]} · {b['name']} ({mmss(total(o))})", ""]
        for i, l in enumerate(L, 1):
            if l["owner"] == o:
                g += [f"### Lámina {i:02d} · {l['title']} ({l['time']})", "", l["notes"], ""]
        siguiente = CORTO[o] if o < 6 else None
        if siguiente:
            g += [f"*Pase:* \"Ahora {siguiente} les explica {BLOQUES[o]['name'].split('·')[-1].strip().lower()}.\"", ""]
    guardar("guion_exposicion", g)


if __name__ == "__main__":
    # python deck_5a.py FibraNet_Slides_v2.html  -> guarda una versión nueva sin pisar la anterior
    salida = sys.argv[1] if len(sys.argv) > 1 else "FibraNet_Slides.html"
    ruta = os.path.join(TEMA, "03_ppt", salida)
    with open(ruta, "x", encoding="utf-8", newline="\n") as archivo:
        archivo.write(construir_html())
    os.makedirs(os.path.join(TEMA, "03_ppt", "img"), exist_ok=True)
    if "--solo-deck" not in sys.argv[2:]:
        prompts()
        guiones()
    print("Deck generado:", len(L), "láminas")
