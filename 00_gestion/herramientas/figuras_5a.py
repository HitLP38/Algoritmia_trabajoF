# Figuras del informe de FibraNet (5.a). Uso: python figuras_5a.py [carpeta_csv] [carpeta_salida]
import csv, math, os, sys, matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch
plt.rcParams.update({'font.family':'DejaVu Serif','font.size':8,'axes.grid':True,'grid.alpha':0.3})
RAIZ = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', '5a_FibraNet')
B = sys.argv[1] if len(sys.argv) > 1 else os.path.join(RAIZ, '06_codigo', 'benchmark')
O = sys.argv[2] if len(sys.argv) > 2 else os.path.join(RAIZ, '02_informe', 'figuras')
os.makedirs(O, exist_ok=True)
rows=list(csv.DictReader(open(os.path.join(B,'resultados_mst.csv'))))
# Fig: tiempo vs E log V
fig,ax=plt.subplots(figsize=(3.4,2.4))
st={'dispersa':'-','densa':'--'}
for d in ['dispersa','densa']:
    r=[x for x in rows if x['densidad']==d]
    X=[int(x['E'])*math.log2(int(x['V']))/1e6 for x in r]
    # media ± desviación estándar de las corridas (si el CSV la trae)
    for alg,mk,col in (('kruskal','o','black'),('prim','s','gray')):
        Y=[float(x[alg+'_ms']) for x in r]
        err=[float(x[alg+'_desv']) for x in r] if alg+'_desv' in r[0] else None
        ax.errorbar(X,Y,yerr=err,fmt=mk,ls='none',color=col,ms=3,capsize=2,lw=0.7,label=f'{alg.capitalize()} ({d})')
        if alg+'_teorico_ms' in r[0]:   # curva teórica c·E log V ajustada
            ax.plot(X,[float(x[alg+'_teorico_ms']) for x in r],st[d],color=col,lw=0.8)
ax.set_xlabel('E·log₂V (millones)'); ax.set_ylabel('Tiempo (ms)'); ax.legend(fontsize=6)
ax.text(0.98,0.03,'líneas: c·E·log₂V ajustada',transform=ax.transAxes,fontsize=5.5,ha='right',va='bottom',style='italic')
fig.tight_layout(); fig.savefig(os.path.join(O,'fig_mst.png'),dpi=300)
# Fig: UF ns/op
u=list(csv.DictReader(open(os.path.join(B,'resultados_unionfind.csv'))))
fig,ax=plt.subplots(figsize=(3.4,2.4))
ax.loglog([int(x['n']) for x in u],[float(x['optimizado_ns_por_op']) for x in u],'o-',color='black',ms=3,label='Optimizado (compresión + rango)')
ui=[x for x in u if x['ingenuo_ns_por_op']]
ax.loglog([int(x['n']) for x in ui],[float(x['ingenuo_ns_por_op']) for x in ui],'s--',color='gray',ms=3,label='Ingenuo')
ax.set_xlabel('n (elementos)'); ax.set_ylabel('ns por operación'); ax.legend(fontsize=6.5)
fig.tight_layout(); fig.savefig(os.path.join(O,'fig_uf.png'),dpi=300)
# Fig: arquitectura
fig,ax=plt.subplots(figsize=(3.4,2.5)); ax.axis('off'); ax.set_xlim(0,12); ax.set_ylim(0,8.4)
def box(x,y,w,h,t,fc):
    ax.add_patch(FancyBboxPatch((x,y),w,h,boxstyle='round,pad=0.05',fc=fc,ec='black',lw=0.8))
    ax.text(x+w/2,y+h/2,t,ha='center',va='center',fontsize=5.6)
box(4,6.9,4,1.2,'Main\n(menú por consola)','#eeeeee')
box(0.1,4.2,3.7,1.7,'io\nLectorCsv\nEscritorCsv · Tabla','white')
box(4.15,4.2,3.7,1.7,'motor\nUnionFind · Kruskal\nPrim · ResultadoMST','#d9d9d9')
box(8.2,4.2,3.7,1.7,'benchmark\nBenchmark\nUnionFindIngenuo','white')
box(0.1,1.2,3.7,1.7,'datos\nred_*.csv','white')
box(4.15,1.2,3.7,1.7,'modelo\nGrafo · Arista','white')
box(8.2,1.2,3.7,1.7,'pruebas\nPruebas (47)','white')
def arr(a,b): ax.annotate('',xy=b,xytext=a,arrowprops=dict(arrowstyle='->',lw=0.7))
arr((5,6.9),(2,5.95)); arr((6,6.9),(6,5.95)); arr((7,6.9),(10,5.95))
arr((1.95,4.2),(1.95,2.95)); arr((6,4.2),(6,2.95)); arr((3.8,4.4),(4.15,2.6))
arr((8.2,5.05),(7.9,5.05)); arr((8.6,2.9),(7.6,4.2))
ax.text(6,0.4,'Las flechas indican "usa a"',ha='center',fontsize=5.2,style='italic')
fig.tight_layout(); fig.savefig(os.path.join(O,'fig_arq.png'),dpi=300)
# Fig: red de ejemplo con MST
pos={'Sede Central':(4,5),'San Isidro':(3.4,3.8),'Miraflores':(3.6,2.6),'Barranco':(3.2,1.5),'Chorrillos':(4.2,0.7),'Surco':(5,2.2),'La Molina':(6.6,2.6),'Ate':(6.8,4),'San Juan de Lurigancho':(6.2,5.6),'Comas':(4.4,7),'Los Olivos':(2.6,6.6),'Callao':(1.4,5)}
E=[l.strip().split(',') for l in open(os.path.join(RAIZ, '06_codigo', 'datos', 'red_ejemplo.csv'), encoding='utf-8').readlines()[1:]]
mst={('Miraflores','Barranco'),('San Isidro','Miraflores'),('Barranco','Chorrillos'),('Sede Central','San Isidro'),('Los Olivos','Comas'),('Miraflores','Surco'),('Ate','La Molina'),('San Juan de Lurigancho','Ate'),('Surco','La Molina'),('Callao','Los Olivos'),('Comas','San Juan de Lurigancho')}
off={'San Isidro':(-0.25,0,'right'),'Miraflores':(-0.25,0,'right'),'Barranco':(-0.25,0,'right'),'Chorrillos':(0.25,0,'left'),'Surco':(0,-0.32,'center'),'La Molina':(0.25,0,'left'),'Ate':(0.25,0,'left'),'SJL':(0,0.32,'center'),'Callao':(0,0.32,'center'),'Sede Central':(0,0.32,'center')}
fig,ax=plt.subplots(figsize=(3.4,3.2)); ax.axis('off'); ax.set_xlim(0.3,7.9)
for a,b,c in E:
    (x1,y1),(x2,y2)=pos[a],pos[b]; m=(a,b) in mst
    ax.plot([x1,x2],[y1,y2],color='black' if m else '#bbbbbb',lw=1.8 if m else 0.8,ls='-' if m else '--',zorder=1)
    ax.text((x1+x2)/2,(y1+y2)/2,c,fontsize=6,ha='center',va='center',bbox=dict(fc='white',ec='none',pad=0.5),zorder=2)
for n,(x,y) in pos.items():
    ax.scatter(x,y,s=40,c='white',edgecolors='black',zorder=3)
    dx,dy,ha=off.get(n,(0,0.32,'center'))
    ax.text(x+dx,y+dy,n.replace('San Juan de Lurigancho','SJL'),fontsize=6,ha=ha,va='center',zorder=4,bbox=dict(fc='white',ec='none',pad=0.3))
fig.tight_layout(); fig.savefig(os.path.join(O,'fig_red.png'),dpi=300)
