# P5A-03 · Deck HTML de 5.a adaptado a la Red Dorsal Nacional

**Lee:**
- `00_gestion/herramientas/deck_5a.py` (FUENTE ÚNICA: genera el HTML, los prompts `04_slides` y los guiones)
- `00_gestion/herramientas/sims_5a.js` (simuladores de Union-Find, Kruskal y Prim)
- `5a_FibraNet/03_ppt/FibraNet_Slides.html` (resultado actual, 22 láminas, estilo Laboratorio 2)
- `5a_FibraNet/06_codigo/datos/capitales_peru.csv`

**Haz:**
1. Edita `deck_5a.py` (no el HTML a mano): láminas 1, 3, 17 y 20 pasan al caso Red Dorsal (25 capitales, 65 candidatos, 4 081,4 km vs 16 975,1 km, 76 % menos).
2. En `sims_5a.js`, cambia la red de los simuladores de Kruskal y Prim a las 25 capitales: posiciones = (longitud, latitud) escaladas al SVG de 700×560, tramos = los 65 candidatos (calcúlalos con haversine y k = 4, igual que `LectorCoordenadas.java`). Costo total esperado: 4 081,4.
3. Ejecuta `python 00_gestion/herramientas/deck_5a.py FibraNet_Slides_v2.html` (o `_v3` si ya existe) y abre ese HTML para revisar que nada se desborde. No pises `FibraNet_Slides.html`.
4. Poco texto, muy visual. No cambies el estilo (colores, tipografías, cajetín).

**Entrega:** `5a_FibraNet/03_ppt/FibraNet_Slides_v2.html` (o la versión siguiente) + `deck_5a.py` y `sims_5a.js` editados.
