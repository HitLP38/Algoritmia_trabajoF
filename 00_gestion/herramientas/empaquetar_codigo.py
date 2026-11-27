# Arma el ZIP del código fuente para el anexo del informe (5.a y 5.b).
# Uso (desde la carpeta raíz del proyecto):  python 00_gestion/herramientas/empaquetar_codigo.py
# Salida: 5a_FibraNet/02_informe/FibraNet_codigo_fuente.zip y 5b_RedLuz/02_informe/RedLuz_codigo_fuente.zip
import os, zipfile

RAIZ = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
TEMAS = [('5a_FibraNet', 'FibraNet'), ('5b_RedLuz', 'RedLuz')]
CARPETAS = ['src', 'pruebas', 'benchmark', 'datos']           # lo que se entrega de 06_codigo
EXCLUIR_DIR = {'out', '.idea', '.vscode', '__pycache__'}
EXCLUIR_EXT = {'.class'}

def empaquetar(tema, nombre):
    codigo = os.path.join(RAIZ, tema, '06_codigo')
    salida = os.path.join(RAIZ, tema, '02_informe', f'{nombre}_codigo_fuente.zip')
    os.makedirs(os.path.dirname(salida), exist_ok=True)
    archivos = []
    for c in CARPETAS:
        for base, dirs, nombres in os.walk(os.path.join(codigo, c)):
            dirs[:] = sorted(d for d in dirs if d not in EXCLUIR_DIR)
            for n in sorted(nombres):
                if os.path.splitext(n)[1] not in EXCLUIR_EXT:
                    archivos.append(os.path.join(base, n))
    leeme = os.path.join(codigo, 'README.md')
    if os.path.exists(leeme):
        archivos.insert(0, leeme)
    with zipfile.ZipFile(salida, 'w', zipfile.ZIP_DEFLATED) as z:
        for a in archivos:
            # dentro del ZIP todo queda bajo <Nombre>/ con rutas relativas a 06_codigo
            z.write(a, os.path.join(nombre, os.path.relpath(a, codigo)).replace(os.sep, '/'))
    java = sum(a.endswith('.java') for a in archivos)
    print(f'{os.path.relpath(salida, RAIZ)}: {len(archivos)} archivos ({java} .java), '
          f'{os.path.getsize(salida) / 1024:.1f} KB')

if __name__ == '__main__':
    for tema, nombre in TEMAS:
        empaquetar(tema, nombre)
