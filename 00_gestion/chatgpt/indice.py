# Regenera la tabla de prompts de CHATGPT_EJECUTOR.md con el hash de cada uno.
# Uso (desde 00_gestion/chatgpt):  python indice.py
# El hash son los 8 primeros caracteres del SHA-256 del archivo: si el prompt
# cambia, cambia el hash, y ChatGPT sabe que debe releerlo.
import hashlib, os, re

AQUI = os.path.dirname(os.path.abspath(__file__))
FASES = {  # ID: (fase, requisito, entrega corta)
    "P5A-01": ("1", "—", "Informe 5.a v2 (caso Red Dorsal)"),
    "P5A-03": ("1", "—", "Deck HTML 5.a (caso Red Dorsal)"),
    "P5A-04": ("1", "P5A-03", "Imágenes de 5.a"),
    "P5A-05": ("1", "P5A-03", "Guiones y preguntas 5.a"),
    "P5B-00": ("2", "—", "Datos reales del distrito y cortes programados"),
    "P5B-01": ("2", "P5B-00 validado por Claude", "Informe 5.b v2 (caso real)"),
    "P5B-02": ("2", "P5B-01", "Deck HTML 5.b + prompts + guiones"),
    "P5B-03": ("2", "P5B-02", "Imágenes de 5.b"),
    "P5B-04": ("2", "P5B-02", "Guiones y preguntas 5.b"),
    "P5A-02": ("3", "CSV del benchmark en PC", "Informe 5.a v3 con tiempos reales"),
    "P5B-05": ("3", "CSV del benchmark en PC", "Informe 5.b v3 con tiempos reales"),
    "PGEN-01": ("4", "todo lo anterior", "Revisión contra la rúbrica"),
}
filas = []
for nombre in sorted(os.listdir(os.path.join(AQUI, "prompts"))):
    m = re.match(r"(P[0-9A-Z]+-\d+)_", nombre)
    if not m:
        continue
    pid = m.group(1)
    h = hashlib.sha256(open(os.path.join(AQUI, "prompts", nombre), "rb").read()).hexdigest()[:8]
    fase, req, ent = FASES.get(pid, ("?", "?", "?"))
    filas.append((fase, pid, h, nombre, req, ent))
filas.sort()
tabla = ["| Fase | ID | Hash | Archivo | Requisito | Entrega | Estado |", "|---|---|---|---|---|---|---|"]
estado_previo = {}
maestro = os.path.join(AQUI, "CHATGPT_EJECUTOR.md")
texto = open(maestro, encoding="utf-8").read()
for linea in texto.splitlines():
    c = [x.strip() for x in linea.split("|")]
    if len(c) > 8 and c[2].strip("*").startswith("P"):
        estado_previo[c[2].strip("*")] = c[7]
for fase, pid, h, nombre, req, ent in filas:
    tabla.append(f"| {fase} | **{pid}** | `{h}` | `prompts/{nombre}` | {req} | {ent} | {estado_previo.get(pid, '⏳')} |")
nuevo = re.sub(r"<!-- TABLA -->.*<!-- /TABLA -->", "<!-- TABLA -->\n" + "\n".join(tabla) + "\n<!-- /TABLA -->", texto, flags=re.S)
open(maestro, "w", encoding="utf-8", newline="\n").write(nuevo)
print("\n".join(tabla))
