# CHATGPT_EJECUTOR · Proyecto final SW305 · Grupo 5

> Documento maestro para ChatGPT. Léelo completo **una vez** al inicio de cada chat.
> Después, Raul solo te escribirá un ID, por ejemplo **`ejecuta P5A-01`**. Busca ese ID en la tabla, abre su archivo de `prompts/` y hazlo.

## 1. Quiénes somos y qué hay que entregar

- Curso: SW305 Algoritmos Avanzados (UNI · FIIS), ciclo 2026-2. Docente: Ing. Jorge Medianero Acosta.
- Grupo 5: Christopher Ricardo Acosta Simon (C1), Santiago Gavilano Kanashiro (C2), Javier Chionglaw Kong Tarrillo (C3), Gianpierre Eyner Cerron Arica (C4), Raul Antonio Guzman Lopez (C5), Cesar Antoni Loli Aldunate (C6).
- **5.a FibraNet** (Prim, Kruskal y Union-Find). Caso real: **Red Dorsal Nacional de Fibra Óptica**. Expo: martes 08/12/2026.
- **5.b RedLuz** (Link-Cut Trees). Caso real: red eléctrica de un distrito de Lima con cortes programados. Expo: jueves 10/12/2026.
- Por tema: informe UNI/IEEE (.docx), deck HTML estilo Laboratorio 2, prompts de imagen por lámina (`04_slides`), guiones y división de trabajo. La rúbrica está en `rubrica de evaluacion/doc de rubrica.pdf`.

## 2. Reparto entre las IA

| Quién | Hace | No toca |
|---|---|---|
| **Claude** | Código Java, pruebas, benchmark, datos del caso real (`06_codigo/`), comandos git | Informe, deck, guiones (salvo que Raul lo pida) |
| **ChatGPT (tú)** | Informe, deck HTML, imágenes, prompts de láminas, guiones, revisión contra rúbrica | **Nada dentro de `06_codigo/`** ni de `.git/` |

Si encuentras un error en el código, no lo corrijas: escríbelo en tu respuesta para que Raul se lo pase a Claude.

## 3. Reglas

1. Español, claro, con voz de buen estudiante. Frases cortas.
2. **No inventes números.** Todo dato sale de los CSV, del README del código o de la salida del programa. Si falta algo, déjalo resaltado en amarillo (en .docx) o como `[[pendiente: …]]`.
3. Fuentes externas solo si son reales y verificables, citadas en formato IEEE.
4. Nunca sobrescribas un entregable: guarda con sufijo de versión (`_v2`, `_v3`).
5. No escribas contraseñas, tokens ni datos personales (los códigos de alumno los escribe Raul).
6. Al terminar un ID: (a) di qué archivos creaste o cambiaste, con su ruta; (b) cambia su **Estado** en la tabla de abajo a ✅ (o 🔄 si quedó a medias); (c) agrega una línea a la bitácora.
7. Si el **hash** de un prompt no coincide con el que recuerdas, el prompt cambió: vuelve a leerlo antes de seguir.

## 4. Mapa de carpetas (raíz: `proyecto final/`)

```
00_gestion/PCB.md                 estado general del proyecto (lo mantiene Claude)
00_gestion/chatgpt/               este documento, indice.py y prompts/
00_gestion/herramientas/          generadores: informe_5a.js, informe_5b.js, deck_5a.py,
                                  deck_motor.js, sims_5a.js, figuras_5a.py, figuras_5b.py
5a_FibraNet/  y  5b_RedLuz/       01_requerimientos · 02_informe · 03_ppt · 04_slides ·
                                  05_guiones · 06_codigo (Java; solo Claude)
rubrica de evaluacion/            rúbrica y Laboratorio2_Slides_v2.html (estilo de referencia)
```

## 5. Estado del código (lo actualiza Claude)

| Tema | Estado | Datos clave |
|---|---|---|
| 5.a | ✅ caso real listo (opción 10 del menú, 41 pruebas OK) | 25 capitales, 65 tramos candidatos (k = 4), plan 24 tramos = **4 081,4 km** vs 16 975,1 km (76,0 % menos) |
| 5.b | 🔄 código listo para datos reales (coordenadas, km por haversine, evento AFECTADOS; 50 pruebas OK). Faltan los datos: P5B-00 | LCT 100 000 ops en 0,34 s; 209× más rápido que BFS con N = 5 000 (preliminar) |

**Entorno de la PC del grupo (para la tabla "Entorno de ejecución" de los informes):** Intel Core i3-1005G1 @ 1,20 GHz · 16 GB de RAM · OpenJDK 21.0.12.1 (Eclipse Temurin) · Windows 10 · 4 núcleos · JVM con 4 042 MB. **5.a ya tiene tiempos reales** (26/11): `5a_FibraNet/06_codigo/benchmark/resultados_mst.csv` y `resultados_unionfind.csv` → ejecuta P5A-02. 5.b sigue con datos preliminares hasta P5B-05.

## 6. Tabla de prompts

Fases: **1** = 5.a con el caso real · **2** = 5.b (requiere el código del caso real) · **3** = tiempos reales de la PC del grupo · **4** = revisión final.

<!-- TABLA -->
| Fase | ID | Hash | Archivo | Requisito | Entrega | Estado |
|---|---|---|---|---|---|---|
| 1 | **P5A-01** | `87271114` | `prompts/P5A-01_informe_red_dorsal.md` | — | Informe 5.a v2 (caso Red Dorsal) | ✅ |
| 1 | **P5A-03** | `d5c7aa56` | `prompts/P5A-03_deck_red_dorsal.md` | — | Deck HTML 5.a (caso Red Dorsal) | ✅ |
| 1 | **P5A-04** | `0f68ed34` | `prompts/P5A-04_imagenes.md` | P5A-03 | Imágenes de 5.a | ✅ |
| 1 | **P5A-05** | `5da31c52` | `prompts/P5A-05_guiones.md` | P5A-03 | Guiones y preguntas 5.a | ✅ |
| 2 | **P5B-00** | `a88754d8` | `prompts/P5B-00_datos_reales.md` | — | Datos reales del distrito y cortes programados | 🔄 |
| 2 | **P5B-01** | `6dbffb20` | `prompts/P5B-01_informe_caso_real.md` | P5B-00 validado por Claude | Informe 5.b v2 (caso real) | ⏳ |
| 2 | **P5B-02** | `eb7e6685` | `prompts/P5B-02_deck.md` | P5B-01 | Deck HTML 5.b + prompts + guiones | ⏳ |
| 2 | **P5B-03** | `937e3ce4` | `prompts/P5B-03_imagenes.md` | P5B-02 | Imágenes de 5.b | ⏳ |
| 2 | **P5B-04** | `c9262df5` | `prompts/P5B-04_guiones.md` | P5B-02 | Guiones y preguntas 5.b | ⏳ |
| 3 | **P5A-02** | `84b0cba1` | `prompts/P5A-02_benchmark_pc.md` | CSV del benchmark en PC | Informe 5.a v3 con tiempos reales | ✅ |
| 3 | **P5B-05** | `67f83fcc` | `prompts/P5B-05_benchmark_pc.md` | CSV del benchmark en PC | Informe 5.b v3 con tiempos reales | ⏳ |
| 4 | **PGEN-01** | `515f3802` | `prompts/PGEN-01_revision_rubrica.md` | todo lo anterior | Revisión contra la rúbrica | ⏳ |
<!-- /TABLA -->

## 7. Bitácora (más reciente arriba)

- 26/11/2026 · P5A-05 completado. Guion, división y preguntas v2. Las 22 notas del deck v4 coinciden con el guion (24:20).
- 26/11/2026 · P5A-04 completado. Se guardaron L01, L03, L08, L11, L14 y L17 en 5a_FibraNet/03_ppt/img/.
- 26/11/2026 · P5A-03 completado. FibraNet_Slides_v3.html y sus simuladores incorporan el caso Red Dorsal Nacional.
- 26/11/2026 · P5B-00 parcial. Se entregaron cuatro archivos con avisos oficiales de Magdalena del Mar y una red radial sintética para simulación.
- 26/11/2026 · P5A-02 completado. Informe_FibraNet_v3.docx y FibraNet_Slides_v2.html incorporan el benchmark de la PC del grupo.
- 26/11/2026 · P5A-01 completado. Informe_FibraNet_v2.docx incorpora el caso Red Dorsal Nacional.
- 26/11/2026 · Raul corrió el benchmark de 5.a en su PC (Windows 10): P5A-02 listo para ejecutar.
- 26/11/2026 · Claude agregó P5B-00 (datos reales de 5.b).
- 26/11/2026 · Claude creó este documento y los 11 prompts.
