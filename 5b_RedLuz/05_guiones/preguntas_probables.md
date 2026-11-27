# Preguntas probables del jurado · RedLuz

Cada integrante debe poder responder las doce preguntas. Entre paréntesis se indica quién puede empezar.

## Motor Link-Cut Tree

1. **¿Qué hace `access(x)`?** (Santiago)  
   Expone el camino entre la raíz del árbol representado y `x`. Sube por los enlaces `path-parent`, reorganiza los splays y reemplaza el hijo derecho de cada nodo visitado con el tramo de camino que viene de abajo. Al final, `x` queda como raíz auxiliar y sus agregados describen el camino expuesto.

2. **¿Qué significa que splay tenga un costo amortizado de O(log n)?** (Christopher)  
   Una operación aislada puede recorrer un camino de longitud lineal. En una secuencia, el análisis potencial con el logaritmo del tamaño de los subárboles acota el costo amortizado. En el Link-Cut Tree, el análisis de `access` también toma en cuenta los cambios entre caminos preferidos.

3. **¿Qué diferencia hay entre un zig-zig y un zig-zag?** (Christopher)  
   En zig-zig, el nodo y su padre son hijos del mismo lado. Se rota primero el padre y luego el nodo. En zig-zag están en lados opuestos, así que se rota el nodo dos veces. Las rotaciones conservan el orden de profundidad dentro del splay.

4. **¿Por qué `makeRoot` usa una inversión perezosa?** (Santiago)  
   Tras `access(x)`, invertir el camino expuesto convierte a `x` en la raíz del árbol representado. En el momento de la inversión se cambia una marca. Cuando el código necesita recorrer el subárbol, intercambia sus hijos y propaga la marca. Marcar cuesta O(1) adicional después de `access`.

5. **¿Cómo verifica `cut(x, y)` que hay una arista directa?** (Cesar)  
   Hace `makeRoot(x)` y `access(y)`. Para que el camino expuesto tenga solo los dos extremos, `x` debe ser el hijo izquierdo de `y` y carecer de hijo derecho. Si se cumple, se quitan las referencias entre ambos y se actualizan los agregados. En el modelo eléctrico, quitar un tramo físico requiere separar su nodo-tramo de los dos extremos.

## Modelo eléctrico

6. **¿Por qué agregar una FUENTE virtual?** (Gianpierre)  
   Todas las subestaciones se unen a una misma referencia con enlaces de 0 km. Así, preguntar si un cliente tiene luz se reduce a comprobar si comparte componente con FUENTE. Los enlaces virtuales también aportan cero al número de tramos físicos.

7. **¿Por qué representar cada tramo como un nodo?** (Gianpierre)  
   Los agregados del splay se calculan sobre nodos. Al intercalar un nodo-tramo, ese nodo aporta su longitud y suma uno al conteo. Los postes y clientes aportan cero. Con el camino expuesto se leen ambos totales.

8. **¿Qué pasa si se intenta cerrar un anillo?** (Cesar)  
   La conexión se rechaza si los extremos ya están en el mismo árbol, porque ya existe un camino entre ellos. Esto incluye dos zonas energizadas que ya se conectan mediante FUENTE. El rechazo conserva el bosque del modelo.

9. **¿Por qué no basta Union-Find?** (Javier)  
   Union-Find permite unir componentes y consultar si dos nodos están en el mismo conjunto. Un corte puede separar un componente, y la estructura estándar carece de una operación para mantener esa separación. RedLuz también necesita consultar agregados sobre el camino.

## Medición e implementación

10. **¿Cómo compararon Link-Cut Tree con BFS?** (Raul)  
    Ambas versiones recibieron el mismo lote para cada tamaño comparable. Se promediaron tres corridas después de una de calentamiento. Con 5 000 nodos y operaciones, LCT tardó 14,57 ms y BFS 2 690,04 ms, una relación BFS/LCT de 184,67 a 1. LCT tardó 572,24 ms con 100 000 operaciones. BFS se midió hasta el tamaño de 5 000.

11. **¿Cómo manejaron los punteros en Java?** (Christopher)  
    Cada objeto `Nodo` usa referencias a su hijo izquierdo, hijo derecho y padre. La referencia `padre` también puede apuntar al `path-parent` cuando el nodo es una raíz auxiliar. La implementación usa una pila explícita para propagar inversiones antes de hacer las rotaciones.

12. **¿Qué es real y qué representa la simulación de Magdalena del Mar?** (Javier)  
    Pluz Energía publicó la fecha y las ventanas horarias. Los 49 nodos, los 48 tramos iniciales y el enlace que se interviene en cada escenario pertenecen al modelo académico. Los conteos de 12, 20, 8 y 0 son clientes simulados que reportó Java al aplicar los eventos.

