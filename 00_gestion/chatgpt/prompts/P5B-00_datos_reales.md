# P5B-00 · Datos reales para 5.b (red de un distrito de Lima + cortes programados)

**Por qué tú:** Claude no tiene acceso a internet desde su entorno; tú sí puedes buscar.

**Lee:** `5b_RedLuz/06_codigo/README.md` (sección "Caso real") y los CSV de ejemplo de `5b_RedLuz/06_codigo/datos/` (formato).

**Haz:**
1. Busca en la web de **Luz del Sur** o **Enel Distribución Perú** los **cortes programados** publicados para un distrito de Lima en noviembre o diciembre de 2026 (o el mes más reciente disponible). Elige un distrito con varios cortes; propón uno a Raul si dudas. Anota la URL y la fecha de consulta.
2. Toma de **OpenStreetMap** (overpass-turbo.eu, `power=substation` y calles del distrito) una subestación real del distrito o la más cercana, y las calles afectadas por esos cortes, con coordenadas de sus intersecciones.
3. Arma una red **radial** y plausible (la red real de postes no es pública): la subestación, 1 a 3 alimentadores que siguen las calles reales, nodos POSTE en las intersecciones y nodos CLIENTE (manzanas o predios representativos) al final de cada ramal. Unos 40 a 120 nodos. Dilo así en el informe: "red modelada sobre calles reales".
4. Escribe en `5b_RedLuz/07_datos_reales/` (crea la carpeta; NO escribas en `06_codigo/`):
   - `nodos.csv` → `id,tipo,latitud,longitud` (ids cortos sin espacios, p. ej. `P_GRAU_AYACUCHO`)
   - `tramos.csv` → `origen,destino,` (km vacío: el programa lo calcula con haversine)
   - `eventos.csv` → `evento,a,b,km,nota`: por cada corte programado, `CORTAR` del tramo que alimenta la zona con la nota "Corte programado <fecha> <horario> (<distribuidora>)", luego `AFECTADOS,,,`, algunas `LUZ`/`RUTA` de clientes, y al final `CONECTAR` para reponer (km vacío no vale aquí: pon el km que calculaste o déjalo para que Claude lo complete).
   - `FUENTES.md` → URLs, fecha de consulta y qué se tomó de cada una.

**Entrega:** los 4 archivos. Raul se los pasará a Claude para validarlos y copiarlos a `06_codigo/datos/caso_real/`.
