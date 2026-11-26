# Figuras del informe de RedLuz (5.b). Uso: python figuras_5b.py <carpeta_csv> <carpeta_salida>
import csv, math, os, sys
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch

RAIZ = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', '5b_RedLuz')
B = sys.argv[1] if len(sys.argv) > 1 else os.path.join(RAIZ, '06_codigo', 'benchmark')
O = sys.argv[2] if len(sys.argv) > 2 else os.path.join(RAIZ, '02_informe', 'figuras')
os.makedirs(O, exist_ok=True)
plt.rcParams.update({'font.family': 'serif', 'font.size': 7})
r = list(csv.DictReader(open(os.path.join(B, 'resultados_lct.csv'))))

# Fig. 3: tiempo total LCT vs BFS (log-log)
fig, ax = plt.subplots(figsize=(3.4, 2.4))
ax.loglog([int(x['N']) for x in r], [float(x['lct_ms']) for x in r], 'o-', color='black', ms=3, label='Link-Cut Tree')
b = [x for x in r if x['bfs_ms']]
ax.loglog([int(x['N']) for x in b], [float(x['bfs_ms']) for x in b], 's--', color='gray', ms=3, label='BFS (ingenuo)')
ax.set_xlabel('N = M (nodos y operaciones)'); ax.set_ylabel('Tiempo total (ms)'); ax.legend(fontsize=6.5)
fig.tight_layout(); fig.savefig(os.path.join(O, 'fig_lct.png'), dpi=300)

# Fig. 4: tiempo normalizado por la cota
fig, ax = plt.subplots(figsize=(3.4, 2.2))
ax.semilogx([int(x['N']) for x in r], [float(x['lct_ns_por_MlogN']) for x in r], 'o-', color='black', ms=3, label='LCT: ns / (M log₂N)')
ax.semilogx([int(x['N']) for x in b], [float(x['bfs_ns_por_MN']) for x in b], 's--', color='gray', ms=3, label='BFS: ns / (M·N)')
ax.set_ylim(0, None); ax.set_xlabel('N'); ax.set_ylabel('ns por unidad de cota'); ax.legend(fontsize=6.5)
fig.tight_layout(); fig.savefig(os.path.join(O, 'fig_ratio.png'), dpi=300)

# Fig. 1: arquitectura por paquetes
fig, ax = plt.subplots(figsize=(3.4, 2.5)); ax.axis('off'); ax.set_xlim(0, 12); ax.set_ylim(0, 8.4)
def caja(x, y, w, h, t, fc='white'):
    ax.add_patch(FancyBboxPatch((x, y), w, h, boxstyle='round,pad=0.05', fc=fc, ec='black', lw=0.8))
    ax.text(x + w / 2, y + h / 2, t, ha='center', va='center', fontsize=5.2)
def flecha(a, b):
    ax.annotate('', xy=b, xytext=a, arrowprops=dict(arrowstyle='->', lw=0.7))
caja(4, 7, 4, 1.1, 'redluz.Main\n(menú 0–11)')
caja(0.1, 4.5, 3.6, 1.6, 'io\nLectorCsv\nProcesadorEventos\nMensajes · Tabla')
caja(4.2, 4.5, 3.6, 1.6, 'modelo\nRedDinamica\nRedElectrica\nRedIngenua (BFS)')
caja(4.2, 1.6, 3.6, 1.6, 'motor\nLinkCutTree · Nodo\n(splay + path-parent)', fc='#dddddd')
caja(8.3, 4.5, 3.6, 1.6, 'pruebas\n45 verificaciones')
caja(8.3, 1.6, 3.6, 1.6, 'benchmark\nLCT vs BFS')
flecha((5.2, 7), (2.4, 6.1)); flecha((6, 7), (6, 6.1)); flecha((3.7, 5.3), (4.2, 5.3)); flecha((6, 4.5), (6, 3.2))
flecha((8.3, 5.3), (7.8, 5.3)); flecha((8.3, 2.4), (7.8, 4.8))
ax.text(6, 0.6, 'Las flechas indican "usa a"', ha='center', fontsize=5.2, style='italic')
fig.tight_layout(); fig.savefig(os.path.join(O, 'fig_arq.png'), dpi=300)

# Fig. 2: modelo con FUENTE virtual y tramos como nodos
fig, ax = plt.subplots(figsize=(3.4, 2.4)); ax.axis('off'); ax.set_xlim(0, 10); ax.set_ylim(0, 7)
P = {'FUENTE': (5, 6.3), 'S1': (2.5, 4.6), 'S2': (7.5, 4.6), 'P1': (1.2, 2.4), 'P2': (3.8, 2.4), 'C1': (1.2, 0.5), 'P3': (7.5, 2.4), 'C2': (8.8, 0.5)}
E = [('FUENTE', 'S1', 'v'), ('FUENTE', 'S2', 'v'), ('S1', 'P1', 't'), ('S1', 'P2', 't'), ('P1', 'C1', 't'), ('S2', 'P3', 't'), ('P3', 'C2', 'x')]
for a, c, k in E:
    (x1, y1), (x2, y2) = P[a], P[c]
    ax.plot([x1, x2], [y1, y2], color='black' if k != 'x' else 'gray', lw=1, ls='--' if k in 'vx' else '-', zorder=1)
    if k == 't':
        ax.add_patch(plt.Rectangle(((x1 + x2) / 2 - 0.25, (y1 + y2) / 2 - 0.18), 0.5, 0.36, fc='#dddddd', ec='black', lw=0.6, zorder=2))
for n, (x, y) in P.items():
    fc = 'black' if n == 'FUENTE' else ('#bbbbbb' if n.startswith('S') else 'white')
    ax.add_patch(plt.Circle((x, y), 0.32, fc=fc, ec='black', lw=0.8, zorder=3))
    ax.text(x + 0.45, y, n, fontsize=6, va='center')
ax.text(8.9, 1.6, 'cortado', fontsize=5.5, color='gray', style='italic')
ax.text(0.1, 6.5, '- - unión virtual (0 km)\n▭ nodo-tramo (km, 1 tramo)', fontsize=5.4, va='top')
fig.tight_layout(); fig.savefig(os.path.join(O, 'fig_modelo.png'), dpi=300)
print('ok')
