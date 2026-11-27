# P5B-06 · Llevar el informe y el deck de 5.b a "Sobresaliente"

**Requisito:** Raul volvió a correr el benchmark de 5.b en su PC (ya en `main`) y existen en `5b_RedLuz/06_codigo/benchmark/`:
`resultados_lct.csv` (perfil A) y `resultados_lct_perfiles.csv` (perfiles A, B y C). Las figuras se generan con `python 00_gestion/herramientas/figuras_5b.py` (guarda `fig_lct.png`, `fig_ratio.png` y la nueva `fig_perfiles.png` en `5b_RedLuz/02_informe/figuras/`).

**Lee:** el informe más reciente (`Informe_RedLuz_v3.docx`), esos CSV y el código de `5b_RedLuz/06_codigo/src/redluz/motor/LinkCutTree.java` y `modelo/RedElectrica.java` (método `clientesSinLuz`).

**Qué hay de nuevo en los CSV:**
- Media, desviación estándar (`_desv`), mínimo y máximo de 5 corridas, para LCT y BFS.
- Curva teórica: `lct_teorico_ms` = c · M log₂N y `bfs_teorico_ms` = c · M · N (c ajustada por mínimos cuadrados; para N > 5 000 la de BFS es proyección).
- Tres perfiles, para ver de qué depende la ventaja: **A** árbol profundo + mezcla 40/30/30 (LUZ/CORTAR/CONECTAR), **B** árbol aleatorio (bajo) + la misma mezcla, **C** árbol profundo + 80/10/10. B cambia solo la forma y C solo la mezcla.

**Haz (sin inventar números: todo sale de los CSV y del código):**
1. **Tabla del benchmark:** media ± desv de LCT y BFS, columnas teóricas y BFS/LCT (perfil A). Agrega una tabla corta de perfiles: para N = 1 000 y 2 000 (BFS se mide hasta 2 000 en B y C, y hasta 5 000 en A), BFS/LCT en A, B y C, y el cociente ns/(M log₂N) del LCT con N = 100 000.
2. **Figuras:** `fig_lct.png` (barras de error + líneas teóricas) y `fig_perfiles.png` (nueva). Pie: "Media ± desviación estándar de 5 corridas; líneas c·M·log₂N y c·M·N ajustadas".
3. **Discusión:** 4 a 6 frases. Responde qué cambia la ventaja del LCT, la forma del árbol (A vs B) o la mezcla de eventos (A vs C), y cuánto varían las corridas.
4. **Análisis amortizado de `access` (sección teórica):** explica en 2 o 3 párrafos, con fórmulas:
   - potencial Φ = Σ r(x), r(x) = log₂(tamaño del subárbol de x en su árbol splay);
   - lema del acceso para splay: costo amortizado ≤ 3(r(raíz) − r(x)) + 1;
   - en `access` los splays sucesivos se encadenan: la suma telescópica deja O(log n) más 1 por cada cambio de *preferred child*;
   - cambios de *preferred child*: con la descomposición heavy-light hay a lo más log n aristas livianas por acceso y las pesadas se pagan con un segundo potencial, así que suman O(log n) amortizado;
   - resultado: `access` O(log n) amortizado, y por eso `link`, `cut`, `connected` y la ruta también (Sleator y Tarjan, 1983; Tarjan, *Data Structures and Network Algorithms*, 1983).
5. **Costo de `AFECTADOS`:** `clientesSinLuz()` llama `tieneLuz` a cada uno de los C clientes y ordena la lista resultante de k clientes sin luz. Costo O(C log n) amortizado más O(k log k), frente a O(C · n) con BFS por cliente, u O(n) con un solo BFS desde FUENTE (que es lo que hace `RedIngenua`). Explica cuándo conviene cada uno.
6. **Corrige "45 verificaciones" → "50 verificaciones"** en el resumen, en la sección de pruebas y en la figura de arquitectura. La figura de arquitectura ya dice 50: solo regenérala con `figuras_5b.py`.
7. **Anexo:** el ZIP se arma con `python 00_gestion/herramientas/empaquetar_codigo.py` (queda en `5b_RedLuz/02_informe/RedLuz_codigo_fuente.zip`).
8. **Deck:** actualiza la lámina del benchmark y su guion con la tabla de perfiles, y guarda una versión nueva del HTML sin pisar la anterior.

**Entrega:** `5b_RedLuz/02_informe/Informe_RedLuz_v4.docx` y el deck de 5.b con una versión nueva. Los códigos de alumno siguen pendientes (los pone Raul).
