"""Genera el deck de RedLuz y sus materiales a partir de una sola lista de láminas."""

import csv
from html import escape
import json
from pathlib import Path
import re
import sys

AQUI = Path(__file__).resolve().parent
TEMA = AQUI.parent.parent / "5b_RedLuz"
CODIGO_JAVA = TEMA / "06_codigo/src/redluz/motor/LinkCutTree.java"
CURSO = "SW305 · 2026-2"
EQUIPO = [
    "Acosta Simon, Christopher Ricardo", "Gavilano Kanashiro, Santiago",
    "Chionglaw Kong Tarrillo, Javier", "Cerron Arica, Gianpierre Eyner",
    "Guzman Lopez, Raul Antonio", "Loli Aldunate, Cesar Antoni",
]
BLOQUES = [
    dict(owner=3, name="Problema", range="01–04", desc="Caso y requisitos."),
    dict(owner=4, name="Modelo y teoría", range="05–07", desc="Bosques, FUENTE y complejidad."),
    dict(owner=1, name="Motor · Splay", range="08–10", desc="Caminos y rotaciones.", motor=True),
    dict(owner=2, name="Motor · Access", range="11–13", desc="Exponer e invertir caminos.", motor=True),
    dict(owner=6, name="Motor · Link y cut", range="14–17", desc="Actualizar y consultar.", motor=True),
    dict(owner=5, name="Resultados", range="18–22", desc="Eventos, mediciones y cierre."),
]
ESTILO = ("Ilustración técnica horizontal 16:9, fondo claro, granate #8A1D2C, "
          "dorado #A87A22 y azul #1F5FA8. Geometría limpia y texto solo si se indica.")
FUENTE_PLUZ = ("https://www.pluz.pe/content/dam/distribuidora-per%C3%BA-rebranding/"
               "documentos-pluz/ayuda/cortes/Cortes%20programados%20-%2016.11%20-%2022.11.pdf")


def leer(path):
    return path.read_text(encoding="utf-8")


def fragmentos():
    java = leer(CODIGO_JAVA)
    firmas = {
        "rotar": "private void rotar(", "splay": "private void splay(",
        "access": "public void access(", "makeRoot": "public void makeRoot(",
        "link": "public boolean link(", "cut": "public boolean cut(",
    }
    out = {}
    for clave, firma in firmas.items():
        inicio = java.index(firma)
        metodo = java[inicio:].split("\n    }", 1)[0] + "\n}"
        lineas = [line.split("//", 1)[0].rstrip() for line in metodo.splitlines()]
        out[clave] = "\n".join(line[4:] if line.startswith("    ") else line for line in lineas)
    return out


def datos_benchmark():
    ruta = TEMA / "06_codigo/benchmark/resultados_lct.csv"
    preliminar = not ruta.exists()
    if preliminar:
        # Datos de la tabla de mediciones preliminares del informe base.
        filas = [(1000, 2.73, 44.38), (2000, 3.41, 176.89), (5000, 9.02, 1884.73),
                 (10000, 18.99, None), (20000, 45.98, None),
                 (50000, 115.72, None), (100000, 336.38, None)]
    else:
        with ruta.open(encoding="utf-8", newline="") as archivo:
            filas = [(int(r["N"]), float(r["lct_ms"]), float(r["bfs_ms"]) if r["bfs_ms"] else None)
                     for r in csv.DictReader(archivo)]
    return filas, preliminar


def numero(x, decimales=0):
    return f"{x:,.{decimales}f}".replace(",", "\u202f").replace(".", ",")


def panel(titulo, texto, clase=""):
    return f'<div class="panel {clase}"><h3>{titulo}</h3><p>{texto}</p></div>'


def fila(*paneles):
    return f'<div class="tarjetas" style="--n:{len(paneles)}">{"".join(paneles)}</div>'


def cajas(*etiquetas):
    cajas_html = ('<span class="flecha">→</span>').join(f'<span class="caja">{e}</span>' for e in etiquetas)
    return f'<div class="flujo">{cajas_html}</div>'


def codigo(clave, marcas, *paneles):
    return (f'<div class="cols c64"><pre class="code" data-java="{clave}" data-mark="{marcas}"></pre>'
            f'<div class="pila">{"".join(paneles)}</div></div>')


def imagen(clave, alternativa, contenido):
    return (f'<div class="arte">{contenido}<div class="foto">'
            f'<img data-img="{clave}" alt="{escape(alternativa, quote=True)}"></div></div>')


def arbol(nodos, conexiones, etiqueta):
    trazos = []
    for a, b, clase in conexiones:
        x, y = nodos[a]
        u, v = nodos[b]
        trazos.append(f'<line class="{clase}" x1="{x}" y1="{y}" x2="{u}" y2="{v}"/>')
    vertices = [f'<g><circle cx="{x}" cy="{y}" r="29"/><text x="{x}" y="{y+7}">{n}</text></g>'
                for n, (x, y) in nodos.items()]
    return (f'<svg class="arbol" viewBox="0 0 650 360" role="img" aria-label="{etiqueta}">'
            f'{"".join(trazos)}{"".join(vertices)}</svg>')


RED = arbol(
    {"FUENTE": (325, 50), "S1": (220, 120), "S2": (465, 120), "P1": (135, 214),
     "P2": (300, 214), "P3": (465, 214), "C1": (135, 311), "C2": (300, 311), "C3": (465, 311)},
    [("FUENTE", "S1", "virtual"), ("FUENTE", "S2", "virtual"), ("S1", "P1", "preferido"),
     ("S1", "P2", ""), ("S2", "P3", ""), ("P1", "C1", "preferido"), ("P2", "C2", ""), ("P3", "C3", "")],
    "Dos subestaciones unidas a FUENTE y tres ramales de clientes")
PREFERIDO = arbol(
    {"F": (325, 50), "S1": (325, 130), "P1": (208, 220), "P2": (443, 220),
     "C1": (208, 310), "C2": (443, 310)},
    [("F", "S1", "preferido"), ("S1", "P1", "preferido"), ("P1", "C1", "preferido"),
     ("S1", "P2", "virtual"), ("P2", "C2", "")],
    "Camino preferido hacia C1 y ramal alterno por P2")
ANTES = arbol({"p": (325, 65), "x": (225, 170), "c": (432, 170), "a": (142, 282), "b": (307, 282)},
             [("p", "x", "preferido"), ("p", "c", ""), ("x", "a", ""), ("x", "b", "")], "Antes de rotar")
DESPUES = arbol({"x": (325, 65), "a": (219, 170), "p": (435, 170), "b": (350, 282), "c": (515, 282)},
               [("x", "p", "preferido"), ("x", "a", ""), ("p", "b", ""), ("p", "c", "")], "Después de rotar")

LAMINAS = []


def lam(titulo, tiempo, cuerpo, guion, prompt, imagen_usada=False):
    i = len(LAMINAS) + 1
    bloque = next(b for b in BLOQUES if int(b["range"][:2]) <= i <= int(b["range"][-2:]))
    LAMINAS.append(dict(title=titulo, time=tiempo, owner=bloque["owner"], block=bloque["name"],
                        html=cuerpo, notes=guion, prompt=prompt + " " + ESTILO,
                        imagen=imagen_usada))


def laminas():
    filas, preliminar = datos_benchmark()
    ultimo, comparable = filas[-1], next(r for r in reversed(filas) if r[2] is not None)
    estado = "Datos preliminares del informe base" if preliminar else "Corrida en la PC del grupo"
    ms_ultimo = numero(ultimo[1], 2)
    razon = numero(comparable[2] / comparable[1], 2) if preliminar else numero(
        float(next(r["veces_mas_rapido"] for r in csv.DictReader(
            leer(TEMA / "06_codigo/benchmark/resultados_lct.csv").splitlines())
            if int(r["N"]) == comparable[0])), 2)
    lam("RedLuz", "0:45",
        '<div class="inst">UNIVERSIDAD NACIONAL DE INGENIERÍA · FIIS · SW305 · 2026-2</div>'
        '<div class="cols c46 portada"><div><div class="eyebrow"><b>PROYECTO FINAL · TEMA 5.b</b> Grupo 5</div>'
        '<h1>RedLuz</h1><p class="lead">Conectividad dinámica con Link-Cut Trees</p></div>' +
        imagen("L01", "Red de distribución radial", RED) + '</div>'
        '<div class="portapie"><div class="team teamlist"></div><p>Ing. Jorge Medianero Acosta<br>10 de diciembre de 2026</p></div>',
        "Presentamos RedLuz. Cuando se corta un tramo, cambia qué clientes reciben electricidad. El programa actualiza un bosque y responde consultas de suministro y distancia. Veremos el modelo, el motor y los resultados.",
        "Red radial esquemática con una fuente virtual, subestaciones y clientes. Sigue la topología genérica de la lámina.", True)
    lam("Agenda", "0:30", '<div class="agenda" id="agenda"></div>',
        "Primero veremos el problema y la teoría. Christopher, Santiago y Cesar presentan el motor. Al final revisamos la salida de Java y el benchmark.",
        "Seis bloques conectados por un recorrido, con los tres bloques del motor en granate.")
    lam("Dos ventanas de mantenimiento", "1:05",
        fila(panel("A · 13:30–15:00", "Apertura y reposición del tramo A.", "acc"),
             panel("B · 14:30–16:00", "Apertura y reposición del tramo B.", "gold")) +
        '<div class="panel"><h3>Magdalena del Mar · 16 de noviembre de 2026</h3>'
        '<p>Avisos de Pluz Energía. El grafo de distribución es una simulación académica.</p></div>',
        "Pluz Energía anunció dos ventanas en Magdalena del Mar. Coinciden entre las dos y media y las tres. Usamos esas horas para simular cambios sobre dos alimentadores. La subestación, los postes, los clientes y los tramos son ficticios.",
        "Línea de tiempo de dos intervalos parcialmente superpuestos, uno azul y otro granate.")
    lam("Qué resuelve RedLuz", "1:00",
        fila(panel("Actualizar", "Conectar o cortar un tramo.", "acc"),
             panel("Consultar", "Suministro, distancia y clientes sin luz.", "gold"),
             panel("Conservar", "La red debe seguir siendo radial.")),
        "Un corte elimina un tramo existente. Una conexión agrega un tramo entre componentes. Las consultas preguntan si el cliente alcanza la fuente y cuánto mide el camino. Las aristas que cerrarían un ciclo se rechazan.",
        "Tres diagramas pequeños de corte, consulta y rechazo de anillo.")
    lam("Una FUENTE para el bosque", "1:10",
        f'<div class="cols c46"><div class="pila">{panel("FUENTE virtual", "Enlace de 0 km a cada subestación.", "acc")}'
        f'{panel("Consulta", "C1 tiene luz si alcanza a FUENTE.", "gold")}</div>{RED}</div>',
        "Cada subestación se conecta a FUENTE con un enlace virtual. Un cliente aparece con luz si comparte componente con FUENTE. Dos zonas alimentadas ya están conectadas a través de ese nodo. Unirlas cerraría un ciclo en el modelo.",
        "Bosque radial de dos subestaciones con una fuente virtual y conexiones de 0 km diferenciadas.")
    lam("El peso vive en el nodo-tramo", "1:05",
        cajas("S1", "Tramo · 0,8 km", "P1", "Tramo · 0,2 km", "C1") +
        fila(panel("Suma del camino", "1,0 km", "acc"), panel("Conteo", "2 tramos físicos", "gold")),
        "En el ejemplo didáctico, cada arista física se convierte en un nodo-tramo del LCT. El nodo del cliente aporta cero. Al exponer el camino podemos leer la suma y el conteo sin recorrer los enlaces uno por uno.",
        "Cadena alternada de vértices circulares y nodos-tramo cuadrados con pesos 0,8 y 0,2 km.")
    lam("Costo de consultar el bosque", "1:00",
        fila(panel("Link-Cut Tree", "O(log n) amortizado por actualización o consulta de camino.", "acc"),
             panel("Referencia BFS", "O(n) por consulta en el bosque.", "gold")) +
        '<div class="panel"><p>AFECTADOS recorre la lista de clientes y consulta su conectividad.</p></div>',
        "Una operación aislada del LCT puede ser costosa. La garantía es amortizada sobre una secuencia. BFS vuelve a recorrer el componente en cada consulta. AFECTADOS revisa varios clientes y no tiene el costo de una única consulta.",
        "Comparación conceptual de una operación sobre un camino y un recorrido por un componente.")
    lam("Caminos preferidos", "1:10",
        '<div class="cols c46"><div class="pila">' +
        panel("Camino preferido", "Cada nodo tiene a lo sumo un hijo preferido.", "acc") +
        panel("Árbol auxiliar", "Un splay guarda cada camino en orden de profundidad.") +
        '</div>' + imagen("L08", "Camino preferido hacia C1", PREFERIDO) + '</div>',
        "Descomponemos el árbol real en caminos preferidos. Cada camino vive en un splay ordenado por profundidad. Los otros ramales se conectan mediante path-parent. Las operaciones pueden cambiar qué camino es preferido.",
        "Árbol de seis nodos. Resalta F–S1–P1–C1 y deja P2–C2 como ramal alterno.", True)
    lam("Rotar conserva el orden", "1:10",
        '<div class="cols"><div class="panel"><h3>Antes</h3>'+ANTES+'</div>'
        '<div class="panel"><h3>Después</h3>'+DESPUES+'</div></div>',
        "Aquí x es el hijo izquierdo de p. La rotación sube x, baja p a su derecha y mueve b a la izquierda de p. El recorrido en orden se mantiene. El código actualiza primero los agregados de p y luego los de x.",
        "Dos árboles binarios antes y después de una rotación derecha; mismos nodos a, x, b, p y c.")
    lam("Splay aplica rotaciones", "1:25",
        codigo("splay", "zigZig|rotar|empujar",
               panel("Zig", "Una rotación si el padre es la raíz."),
               panel("Zig-zig / zig-zag", "Dos rotaciones según el lado.", "acc"),
               panel("Marcas pendientes", "Aplicarlas de arriba hacia abajo.")),
        "El código primero apila los ancestros para empujar las inversiones pendientes. Si x y el padre están del mismo lado, rota al padre y luego a x. Si están en lados distintos, rota x dos veces. Al terminar, x es la raíz de su splay.",
        "Tres esquemas de zig, zig-zig y zig-zag, con el nodo que sube resaltado.")
    lam("Access expone un camino", "1:05",
        cajas("Raíz real", "S1", "P1", "C1") +
        fila(panel("Exponer", "De la raíz real al nodo consultado.", "acc"),
             panel("Leer", "La raíz auxiliar concentra los agregados.", "gold")),
        "Access reconstruye el camino preferido hasta x. Recorre los path-parent, reorganiza los árboles auxiliares y suelta las ramas que dejan de ser preferidas. Luego x queda como raíz del splay que contiene el camino.",
        "Camino raíz–S1–P1–C1 resaltado, con una rama desprendida del camino preferido.")
    lam("Access en Java", "1:20",
        codigo("access", "splay|z.der|actualizar",
               panel("Subir", "Recorrer con z.padre.", "acc"),
               panel("Pegar", "z.der = y reemplaza el sufijo auxiliar.", "gold")),
        "En cada vuelta hacemos splay de z. Reemplazamos su hijo derecho con el segmento y, que viene de abajo. Recalculamos los agregados y seguimos al siguiente path-parent. Al final hacemos splay de x.",
        "Diagrama de tres pasos de access: elevar, sustituir el hijo derecho y actualizar.")
    lam("MakeRoot e inversión perezosa", "1:15",
        codigo("makeRoot", "access|invertido",
               panel("Exponer", cajas("Raíz", "x"), "acc"),
               panel("Invertir", cajas("x", "Raíz"), "gold")),
        "MakeRoot llama a access y marca el camino como invertido. Con esa inversión x pasa a ser la raíz del árbol representado. Al empujar la marca se intercambian hijos y se propaga el cambio. No hace falta visitar todo el camino al marcarlo.",
        "Un camino lineal antes y después de invertir su orientación, con una marca de inversión.")
    lam("Link une dos árboles", "1:10",
        codigo("link", "makeRoot|findRoot|padre",
               panel("Precondición", "Componentes distintos.", "acc"),
               panel("Cambio", "El nuevo path-parent enlaza los árboles.")),
        "Primero convertimos x en raíz. Buscamos la raíz de y. Si es x, ambos ya estaban en el mismo componente y rechazamos el enlace. Si son árboles distintos, x pasa a colgar de y por un path-parent.",
        "Dos árboles separados que quedan unidos por un enlace nuevo destacado.")
    lam("Cut separa vecinos directos", "1:15",
        codigo("cut", "makeRoot|access|y.izq|padre",
               panel("Exponer x → y", "MakeRoot en x y access en y.", "acc"),
               panel("Separar", "Quitar el enlace solo si son vecinos directos.", "gold")),
        "Exponemos el camino entre x e y. La condición del código comprueba que tiene exactamente la arista solicitada. Quitamos el hijo izquierdo de y, limpiamos el padre de x y recalculamos los agregados.",
        "Camino con el enlace x–y marcado para corte y dos componentes resultantes.")
    lam("Conectividad y ruta", "1:00",
        fila(panel("Conectividad", "Comparar raíces de FUENTE y C1.", "acc"),
             panel("Ruta", "Exponer el camino y leer km y tramos.", "gold")) +
        cajas("FUENTE", "S1", "P1", "C1"),
        "Connected compara la raíz representada de los dos nodos. Con suministro, camino expone el trayecto desde FUENTE y el splay permite leer la distancia y el número de tramos. La unión virtual se cuenta como cero.",
        "Cadena de cuatro nodos con enlace virtual a S1 y dos enlaces físicos hasta C1.")
    lam("Simulador de corte y reconexión", "1:40",
        '<div id="simRed"><div class="controles"><button data-paso="inicio">Reiniciar</button>'
        '<button data-paso="atras">Atrás</button><button data-paso="siguiente">Paso</button>'
        '<button data-paso="fin">Todo</button><span id="simPaso" aria-live="polite"></span></div>'
        '<div class="cols c64"><svg id="simGrafo" viewBox="0 0 710 310" role="img" aria-label="Red simulada"></svg>'
        '<div class="pila"><div class="panel acc" id="simEstado" aria-live="polite"></div>'
        '<div class="panel" id="simTraza" aria-live="polite"></div></div></div></div>',
        "Partimos de FUENTE, S1, P1 y C1. El primer paso consulta luz y ruta. Cortamos el tramo P1–C1 y comprobamos que C1 queda sin suministro. Luego reconectamos ese tramo y repetimos la consulta. La traza muestra operaciones del LCT.",
        "Diagrama en tres estados de una red con FUENTE, S1, P1 y C1 conectada, cortada y restaurada.")
    lam("Del CSV a la consola", "1:10",
        cajas("CSV", "Lector y eventos", "RedElectrica", "LinkCutTree") +
        '<div class="panel dorada"><p>Opción 1 · cargar red</p><p>Opción 9 · procesar eventos</p>'
        '<p>Opción 11 · activar traza</p></div>',
        "Main lee el menú. El lector registra nodos y tramos y el procesador alimenta el motor con los eventos. RedElectrica traduce las operaciones del dominio a LCT. RedIngenua mantiene la referencia con BFS para comparar.",
        "Arquitectura modular con CSV, lector, modelo y LinkCutTree conectados de izquierda a derecha.")
    lam("Salida del caso de Magdalena del Mar", "1:10",
        '<table class="compact caso"><thead><tr><th>Hora</th><th>Cambio simulado</th><th>Sin luz</th><th>LUZ / RUTA consultada</th></tr></thead>'
        '<tbody><tr><td>13:30</td><td>Corte A</td><td>12</td><td>A · Sin luz / sin ruta</td></tr>'
        '<tr><td>14:30</td><td>Corte B</td><td>20</td><td>B · Sin luz / sin ruta</td></tr>'
        '<tr><td>15:00</td><td>Reposición A</td><td>8</td><td>A · Con luz / 0,38 km, 5 tramos</td></tr>'
        '<tr><td>16:00</td><td>Reposición B</td><td>0</td><td>B · Con luz / 0,54 km, 7 tramos</td></tr></tbody></table>'
        '<p class="lead">49 nodos · 48 tramos iniciales · 16 eventos procesados sin errores</p>',
        "Tras el primer corte hay doce clientes simulados sin luz. Con ambos cortes activos hay veinte. Al reponer A quedan ocho en B. Al reponer B quedan cero. Se confirma además la vuelta de las rutas para los clientes consultados.",
        "Línea de tiempo de los cuatro cambios con 12, 20, 8 y 0 clientes simulados sin luz.")
    datos = "".join(f"<tr><td>{numero(n)}</td><td>{numero(a,2)}</td><td>{numero(b,2) if b is not None else '—'}</td></tr>"
                    for n,a,b in filas)
    lam("Benchmark · Link-Cut Tree frente a BFS", "1:20",
        '<div class="cols c64"><svg id="grafLct" class="graf" role="img" aria-label="Tiempos de LCT y BFS"></svg>'
        '<div class="pila"><div class="panel"><span class="tag">'+estado+'</span>'
        '<table class="compact"><tr><th>N = M</th><th>LCT (ms)</th><th>BFS (ms)</th></tr>'+datos+'</table></div>'
        + panel(f"{razon}×", f"Relación BFS/LCT con N = M = {numero(comparable[0])}.", "acc")+'</div></div>',
        f"Esta gráfica usa {estado.lower()}. Cada tamaño ejecuta el mismo lote para ambas versiones cuando hay medición de BFS. Con cinco mil nodos y operaciones, la relación de tiempos fue {razon} a uno. Con cien mil operaciones, LCT tardó {ms_ultimo} milisegundos.",
        "Gráfico técnico de dos series con escalas logarítmicas. Tomar los valores del CSV y respetar los puntos ausentes.")
    lam("Qué muestra RedLuz", "1:00",
        fila(panel(f"{ms_ultimo} ms", f"LCT para {numero(ultimo[0])} operaciones del benchmark.", "acc"),
             panel("Cortes y reposiciones", "Las consultas cambian con el estado de cada componente.", "gold")) +
        '<div class="panel"><p>El modelo admite conectividad y distancias. Un estudio eléctrico incluiría capacidad y restricciones operativas.</p></div>',
        "RedLuz mantiene el bosque al modificar tramos. En el caso simulado recupera las rutas tras las dos reposiciones. El benchmark compara el costo computacional con BFS. Para un modelo eléctrico más completo quedarían por estudiar capacidad y restricciones de operación.",
        "Un tramo restaurado y un gráfico de tiempos discretos, con fondo claro.")
    lam("Referencias y preguntas", "0:35",
        '<div class="cols"><div class="panel"><h3>Referencias</h3><ul>'
        '<li>Sleator y Tarjan, <i>A data structure for dynamic trees</i>, 1983.</li>'
        '<li>Sleator y Tarjan, <i>Self-adjusting binary search trees</i>, 1985.</li>'
        f'<li><a href="{FUENTE_PLUZ}">Pluz Energía, avisos del 16 al 22 de noviembre de 2026</a>.</li></ul></div>'
        '<div class="panel acc"><h3>Preguntas</h3><p>¿Cómo se corta un tramo y se actualiza la ruta?</p></div></div>',
        "Estas son las referencias del motor y el aviso del caso. Gracias. Podemos volver a la lámina del simulador para revisar cualquier paso.",
        "Esquema sencillo de un camino con un signo de interrogación discreto.")
    return filas


CSS = """
.portada{align-items:center}.portada h1{margin:24px 0}.portapie{display:grid;grid-template-columns:1.5fr 1fr;gap:32px}.portapie p{font-size:20px;color:var(--muted);align-self:end}
.tarjetas{display:grid;grid-template-columns:repeat(var(--n),minmax(0,1fr));gap:22px}.tarjetas .panel{min-height:140px}
.agenda{display:grid;grid-template-columns:repeat(3,1fr);gap:20px}.pila{display:flex;flex-direction:column;gap:16px;min-height:0}
.flujo{display:flex;flex-wrap:wrap;align-items:center;gap:14px;padding:22px 0}.panel .flujo{padding:4px 0}
.caja{border:2px solid var(--granate);background:var(--granate-soft);padding:18px 26px;font:650 22px var(--f-mono)}.flecha{font-size:33px;color:var(--granate)}
.arbol,#simGrafo,.graf{width:100%;height:100%;min-height:0;background:white;border:1px solid var(--rule)}
.arbol line,#simGrafo line{stroke:var(--g2);stroke-width:5}.arbol .virtual,#simGrafo .virtual{stroke-dasharray:9 7}
.arbol .preferido{stroke:var(--granate);stroke-width:7}.arbol circle,#simGrafo circle{fill:white;stroke:var(--ink);stroke-width:2}
.arbol text,#simGrafo text{text-anchor:middle;font:650 15px var(--f-mono);fill:var(--ink)}.panel .arbol{height:410px}
.arte{min-height:0;min-width:0;position:relative;align-self:stretch}.arte .foto{position:absolute;inset:0;background:#fff}.arte .foto:not(:has(img[src])){display:none}
.arte img{height:100%;width:100%;object-fit:contain;cursor:zoom-in}
.code{font-size:17px;line-height:1.25;align-self:start;max-height:100%}.code .l{white-space:pre-wrap;overflow-wrap:anywhere}
.controles{display:flex;align-items:center;gap:12px}.controles span{margin-left:auto;font:16px var(--f-mono)}.controles button{padding:8px 14px;border:1px solid var(--ink);background:white;font:16px var(--f-mono);cursor:pointer}
#simRed{display:flex;flex:1;min-height:0;flex-direction:column;gap:14px}#simGrafo .cortado{stroke:var(--granate);stroke-dasharray:10 8}
#simGrafo .sin-luz{fill:var(--granate-soft);stroke:var(--granate)}#simTraza ol{margin:6px 0 0;padding-left:26px;font:15px/1.38 var(--f-mono)}
.ax{font:500 14px var(--f-mono);fill:var(--muted)}.axl,.lg{font:600 15px var(--f-body);fill:var(--ink)}
.caso td,.caso th{font-size:21px}.dorada{border-color:var(--oro);background:var(--oro-soft)}
"""

HUD = """<div id="hud"><button class="navbtn" id="bPrev" aria-label="Anterior">◀</button><button class="navbtn" id="bNext" aria-label="Siguiente">▶</button><button class="navbtn" id="bOv">Índice (O)</button><button class="navbtn" id="bNotes">Guion (N)</button><button class="navbtn" id="bFs">Pantalla (F)</button><button class="navbtn" id="bPdf">Exportar PDF (P)</button></div>
<div id="notes" hidden><h5 id="notesH"></h5><div id="notesB"></div></div><div id="overview" hidden></div>
<div id="printTip" hidden><div class="box"><h4>Exportar a PDF</h4><ol><li>Destino: Guardar como PDF</li><li>Márgenes: ninguno</li><li>Activar gráficos de fondo</li></ol><div class="row"><button id="ptCancel">Cancelar</button><button class="pri" id="ptGo">Abrir impresión</button></div></div></div>"""


def construir_html(filas):
    meta = dict(team=EQUIPO, codes=[""]*6, blocks=BLOQUES, curso=CURSO, tarea="Tema 5.b RedLuz",
                slides=[{k:l[k] for k in ("title","owner","block","time","notes")} for l in LAMINAS])
    sections = []
    for i,l in enumerate(LAMINAS,1):
        if i == 1:
            sections.append(f'<section class="slide cover">{l["html"]}</section>')
        else:
            sections.append(f'<section class="slide"><div class="eyebrow"><b>{i:02}</b> {l["block"]}</div>'
                            f'<h2>{l["title"]}</h2>{l["html"]}</section>')
    js = "\n".join(f"const {k} = {json.dumps(v, ensure_ascii=False)};"
                   for k,v in (("META",meta),("CODIGO",fragmentos()),("BENCH_LCT",filas)))
    style = "\n".join([leer(AQUI/"deck_fuentes.css"),leer(AQUI/"deck_base.css"),CSS])
    return (f'<!doctype html><html lang="es"><head><meta charset="utf-8"><meta name="viewport" '
            f'content="width=device-width,initial-scale=1"><title>RedLuz · Grupo 5</title><style>{style}</style></head>'
            f'<body><div id="bar"></div><div id="viewport"><div id="stage">{"".join(sections)}</div></div>{HUD}'
            f'<script>{js}\n{leer(AQUI/"deck_motor.js")}\n{leer(AQUI/"sims_5b.js")}</script></body></html>')


def segundos(tiempo):
    m,s = map(int,tiempo.split(":"))
    return m*60+s


def mmss(seg):
    return f"{seg//60}:{seg%60:02}"


def generar(nombre):
    filas = laminas()
    total = sum(segundos(l["time"]) for l in LAMINAS)
    assert len(LAMINAS)==22 and total<=25*60
    deck=TEMA/"03_ppt"/nombre
    materiales={deck:construir_html(filas)}
    reparto=["# División de trabajo · RedLuz", "", f"Duración prevista **{mmss(total)}**. Exposición del 10 de diciembre de 2026.", "",
             "| Láminas | Bloque | Integrante | Tiempo |","|---|---|---|---|"]
    guion=["# Guion de exposición · RedLuz", "", f"Duración prevista **{mmss(total)}**. Usa la tecla N del deck para ver estas mismas notas."]
    for b in BLOQUES:
        integrantes=[(i,l) for i,l in enumerate(LAMINAS,1) if l["owner"]==b["owner"]]
        tiempo=mmss(sum(segundos(l["time"]) for _,l in integrantes))
        persona=EQUIPO[b["owner"]-1]
        reparto.append(f'| {b["range"]} | {b["name"]} | {persona} | {tiempo} |')
        guion.extend(["",f'## {persona} · {b["name"]} ({tiempo})'])
        for i,l in integrantes:
            guion.extend(["",f'### Lámina {i:02} · {l["title"]} ({l["time"]})',"",l["notes"]])
            img=f"L{i:02}"
            uso="Se usa en el deck. El SVG integrado sirve de respaldo." if l["imagen"] else "Imagen opcional de apoyo."
            materiales[TEMA/"04_slides"/f"{img}.txt"] = (
                f'Lámina {i:02} · {l["title"]}\nExpone: {persona}\n\n{uso}\n'
                f'Guardar como 5b_RedLuz/03_ppt/img/{img}.png\n\n{l["prompt"]}\n')
    reparto.extend(["", f"Abre {nombre} en Edge o Chrome. N muestra el guion y O abre el índice. "
                       "El simulador de la lámina 17 admite avanzar, retroceder y reiniciar."])
    materiales[TEMA/"05_guiones/guion_exposicion.md"]="\n".join(guion)+"\n"
    materiales[TEMA/"05_guiones/division_de_trabajo.md"]="\n".join(reparto)+"\n"
    for path in materiales:
        if path.exists():
            raise FileExistsError(path)
    for path,contenido in materiales.items():
        path.parent.mkdir(parents=True,exist_ok=True)
        with path.open("x",encoding="utf-8",newline="\n") as archivo:
            archivo.write(contenido)
    (TEMA/"03_ppt/img").mkdir(exist_ok=True)
    print(f"{len(LAMINAS)} láminas, {mmss(total)}. {len(materiales)} archivos generados.")


if __name__ == "__main__":
    generar(sys.argv[1] if len(sys.argv)>1 else "RedLuz_Slides.html")

