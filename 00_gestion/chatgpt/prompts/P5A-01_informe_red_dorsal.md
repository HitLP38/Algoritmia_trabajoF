# P5A-01 · Informe 5.a adaptado al caso real (Red Dorsal Nacional)

**Lee:**
- `5a_FibraNet/02_informe/Informe_FibraNet.docx` (borrador base, formato UNI/IEEE ya aprobado)
- `5a_FibraNet/06_codigo/README.md` (sección "Caso real: Red Dorsal Nacional")
- `5a_FibraNet/06_codigo/datos/capitales_peru.csv`
- `5a_FibraNet/06_codigo/src/fibranet/modelo/Geo.java` y `src/fibranet/io/LectorCoordenadas.java`
- `rubrica de evaluacion/doc de rubrica.pdf` (sección 3: formato del informe)

**Haz:**
1. Cambia el caso de estudio: de "12 sedes de Lima" a **Red Dorsal Nacional de Fibra Óptica (RDNFO)**: 24 capitales de departamento + Callao, costo = distancia haversine en km, tramos candidatos = 4 capitales más cercanas.
2. Introducción: contexto real de la RDNFO en el Perú (qué es, para qué sirve, por qué importa el costo por km). Cita fuentes reales en formato IEEE (MTC, PRONATEL u Osiptel). Si no puedes verificar un dato, no lo pongas.
3. Marco teórico: agrega la fórmula haversine y por qué es una cota inferior del cable real.
4. Resultados: usa estos números del programa (opción 10 del menú, k = 4): 25 sedes, 65 tramos candidatos, plan de 24 tramos, **4 081,4 km** frente a 16 975,1 km (76,0 % menos). Mantén las tablas del benchmark como están (siguen marcadas como preliminares).
5. Agrega una figura del mapa del Perú con las capitales y los 24 tramos del plan (puedes dibujarla con matplotlib usando el CSV y la lista de tramos de la tabla de abajo).
6. Mantén: carátula, dos columnas IEEE, Times New Roman 10, resaltado amarillo en los códigos de alumno y en lo que falte.

Tramos del plan (Kruskal, km): Callao–Lima 8,3 · Huancavelica–Huancayo 83,9 · Huanuco–Cerro de Pasco 84,1 · Ayacucho–Huancavelica 91,7 · Abancay–Cusco 99,4 · Chachapoyas–Moyobamba 101,6 · Arequipa–Moquegua 109,2 · Moquegua–Tacna 116,2 · Cajamarca–Trujillo 120,0 · Chachapoyas–Cajamarca 125,5 · Huaraz–Huanuco 147,8 · Cajamarca–Chiclayo 152,9 · Abancay–Ayacucho 154,3 · Huancavelica–Ica 164,1 · Arequipa–Puno 173,2 · Lima–Cerro de Pasco 173,5 · Piura–Tumbes 182,1 · Huancayo–Cerro de Pasco 191,8 · Chiclayo–Piura 196,0 · Huaraz–Trujillo 228,0 · Huanuco–Pucallpa 253,2 · Cusco–Puerto Maldonado 318,5 · Arequipa–Cusco 322,2 · Iquitos–Moyobamba 483,9.

**Entrega:** `5a_FibraNet/02_informe/Informe_FibraNet_v2.docx` (no sobrescribas el original).
