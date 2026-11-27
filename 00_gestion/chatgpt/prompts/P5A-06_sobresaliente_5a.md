# P5A-06 · Llevar el informe y el deck de 5.a a "Sobresaliente"

**Requisito:** Raul volvió a correr el benchmark de 5.a en su PC (ya en `main`) y existen en `5a_FibraNet/06_codigo/benchmark/`:
`resultados_mst.csv`, `resultados_mst_corridas.csv` y `resultados_unionfind.csv`. Las figuras nuevas se generan con `python 00_gestion/herramientas/figuras_5a.py`, que las guarda en `5a_FibraNet/02_informe/figuras/`.

**Lee:** el informe más reciente (`Informe_FibraNet_v3.docx`), esos CSV, `5a_FibraNet/06_codigo/README.md` (tabla de complejidad) y `5a_FibraNet/05_guiones/preguntas_probables.md` (pregunta 8).

**Qué hay de nuevo en los CSV:**
- `resultados_mst.csv`: por cada red, media, desviación estándar (`_desv`), mínimo y máximo de las 5 corridas de Kruskal y de Prim, `memoria_mb` y la **curva teórica** `kruskal_teorico_ms` / `prim_teorico_ms` = c · E log₂V, con c ajustada por mínimos cuadrados a cada serie.
- `resultados_mst_corridas.csv`: cada corrida por separado y qué algoritmo fue primero (se alternan para que el orden no favorezca a ninguno).
- `resultados_unionfind.csv`: lo mismo para Union-Find, con `ingenuo_teorico_ms` = c · n² (para n grandes es una proyección: el ingenuo no se ejecuta).

**Haz (sin inventar números: todo sale de los CSV):**
1. **Tablas del benchmark:** reemplázalas con media ± desviación estándar, la columna teórica y la columna **Memoria (MB)** (`memoria_mb`). Si decides no mostrar memoria, quita también toda mención a esa columna.
2. **Figuras:** usa `fig_mst.png` y `fig_uf.png` regeneradas (barras de error + línea teórica). Pie de figura: "Media ± desviación estándar de 5 corridas; la línea es c·E·log₂V ajustada por mínimos cuadrados".
3. **Discusión experimental:** 3 a 5 frases. Di qué serie queda pegada a su curva teórica y cuál se separa, y cuánto varían las corridas (desviación relativa = desv / media). Menciona que Kruskal y Prim se alternan en cada corrida.
4. **Pseudocódigo de Prim:** cópialo de `00_gestion/herramientas/informe_5a.js` (bloque `PRIM(G, s)`): ya recorre todas las componentes con el bucle `para cada r en (s, 0, 1, …, n−1)` y devuelve un bosque.
5. **Cotas:** distingue (a) grafo simple, sin dos tramos entre el mismo par: E ≤ V(V−1)/2, log E ≤ 2 log V y O(V + E log E) = O(V + E log V); (b) con tramos paralelos (nuestro lector los acepta): E ya no está acotado por V², así que la cota correcta es O(V + E log E). Las redes del proyecto no tienen pares repetidos.
6. **Nota del 76 %:** aclara que compara los 4 081,4 km del árbol con la **suma de los 65 tramos candidatos del modelo** (16 975,1 km, k = 4 vecinos más cercanos), no con la red real de la RDNFO.
7. **Anexo:** el ZIP del código se arma con `python 00_gestion/herramientas/empaquetar_codigo.py` (queda en `5a_FibraNet/02_informe/FibraNet_codigo_fuente.zip`). Cita ese nombre en el anexo.
8. **Deck:** en `00_gestion/herramientas/deck_5a.py` actualiza la lámina del benchmark MST (tabla y panel "Contraste con la teoría") con los números nuevos y regenera con `python 00_gestion/herramientas/deck_5a.py FibraNet_Slides_v5.html`. Si cambia el guion de esa lámina, edita sus notas ahí mismo.

**Entrega:** `5a_FibraNet/02_informe/Informe_FibraNet_v4.docx` y `5a_FibraNet/03_ppt/FibraNet_Slides_v5.html`. Los códigos de alumno siguen pendientes (los pone Raul).
