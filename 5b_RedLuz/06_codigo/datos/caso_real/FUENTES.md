# RedLuz · Avisos de Magdalena del Mar y simulación radial

Pluz Energía anunció dos ventanas de mantenimiento para el 16 de noviembre de 2026 en Magdalena del Mar. Se usan los horarios anunciados como entradas de una simulación académica.

| Fecha | Horario anunciado (Lima) | Escenario |
|---|---|---|
| 16/11/2026 | 13:30–15:00 | A |
| 16/11/2026 | 14:30–16:00 | B |

Fuente consultada el 26/11/2026. Se tomaron el distrito, la fecha programada y las dos ventanas del listado del lunes 16. [Pluz Energía, avisos de la semana del 16 al 22 de noviembre de 2026](https://www.pluz.pe/content/dam/distribuidora-per%C3%BA-rebranding/documentos-pluz/ayuda/cortes/Cortes%20programados%20-%2016.11%20-%2022.11.pdf)

## Modelo y resultados esperados

El grafo académico tiene una subestación ficticia, dos alimentadores, 16 postes simulados y 32 clientes sintéticos. Las coordenadas son una cuadrícula artificial de unos 620 m × 300 m trasladada a Magdalena del Mar (subestación ficticia en −12,0940; −77,0700). No corresponden a postes reales; se usan para calcular distancias con haversine, con R = 6 371 km. La asignación de cada aviso a un tramo es un supuesto de simulación. Los resultados describen clientes sintéticos del grafo.

Al inicio los 49 nodos forman un árbol de 48 tramos. Las cuatro consultas AFECTADOS deben devolver 12, 20, 8 y 0 clientes, respectivamente. La coincidencia parcial de horarios permite probar dos desconexiones activas a la vez y recuperar cada componente por separado. Las horas de reposición representan el término anunciado de cada ventana.

## Qué es real y qué es modelado

| Dato | Origen |
|---|---|
| Distrito, fecha y ventanas horarias de los dos cortes | Real: aviso de Pluz Energía |
| Ubicación general (Magdalena del Mar) | Real |
| Subestación, postes, clientes, tramos y coordenadas exactas | Modelado: la topología de la red de distribución no es pública |
| Qué tramo se corta en cada aviso | Supuesto de la simulación |
