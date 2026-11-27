# Guion de exposición · RedLuz

Duración prevista **24:20**. Usa la tecla N del deck para ver estas mismas notas.

## Chionglaw Kong Tarrillo, Javier · Problema (3:20)

### Lámina 01 · RedLuz (0:45)

Presentamos RedLuz. Cuando se corta un tramo, cambia qué clientes reciben electricidad. El programa actualiza un bosque y responde consultas de suministro y distancia. Veremos el modelo, el motor y los resultados.

### Lámina 02 · Agenda (0:30)

Primero veremos el problema y la teoría. Christopher, Santiago y Cesar presentan el motor. Al final revisamos la salida de Java y el benchmark.

### Lámina 03 · Dos ventanas de mantenimiento (1:05)

Pluz Energía anunció dos ventanas en Magdalena del Mar. Coinciden entre las dos y media y las tres. Usamos esas horas para simular cambios sobre dos alimentadores. La subestación, los postes, los clientes y los tramos son ficticios.

### Lámina 04 · Qué resuelve RedLuz (1:00)

Un corte elimina un tramo existente. Una conexión agrega un tramo entre componentes. Las consultas preguntan si el cliente alcanza la fuente y cuánto mide el camino. Las aristas que cerrarían un ciclo se rechazan.

## Cerron Arica, Gianpierre Eyner · Modelo y teoría (3:15)

### Lámina 05 · Una FUENTE para el bosque (1:10)

Cada subestación se conecta a FUENTE con un enlace virtual. Un cliente aparece con luz si comparte componente con FUENTE. Dos zonas alimentadas ya están conectadas a través de ese nodo. Unirlas cerraría un ciclo en el modelo.

### Lámina 06 · El peso vive en el nodo-tramo (1:05)

En el ejemplo didáctico, cada arista física se convierte en un nodo-tramo del LCT. El nodo del cliente aporta cero. Al exponer el camino podemos leer la suma y el conteo sin recorrer los enlaces uno por uno.

### Lámina 07 · Costo de consultar el bosque (1:00)

Una operación aislada del LCT puede ser costosa. La garantía es amortizada sobre una secuencia. BFS vuelve a recorrer el componente en cada consulta. AFECTADOS revisa varios clientes y no tiene el costo de una única consulta.

## Acosta Simon, Christopher Ricardo · Motor · Splay (3:45)

### Lámina 08 · Caminos preferidos (1:10)

Descomponemos el árbol real en caminos preferidos. Cada camino vive en un splay ordenado por profundidad. Los otros ramales se conectan mediante path-parent. Las operaciones pueden cambiar qué camino es preferido.

### Lámina 09 · Rotar conserva el orden (1:10)

Aquí x es el hijo izquierdo de p. La rotación sube x, baja p a su derecha y mueve b a la izquierda de p. El recorrido en orden se mantiene. El código actualiza primero los agregados de p y luego los de x.

### Lámina 10 · Splay aplica rotaciones (1:25)

El código primero apila los ancestros para empujar las inversiones pendientes. Si x y el padre están del mismo lado, rota al padre y luego a x. Si están en lados distintos, rota x dos veces. Al terminar, x es la raíz de su splay.

## Gavilano Kanashiro, Santiago · Motor · Access (3:40)

### Lámina 11 · Access expone un camino (1:05)

Access reconstruye el camino preferido hasta x. Recorre los path-parent, reorganiza los árboles auxiliares y suelta las ramas que dejan de ser preferidas. Luego x queda como raíz del splay que contiene el camino.

### Lámina 12 · Access en Java (1:20)

En cada vuelta hacemos splay de z. Reemplazamos su hijo derecho con el segmento y, que viene de abajo. Recalculamos los agregados y seguimos al siguiente path-parent. Al final hacemos splay de x.

### Lámina 13 · MakeRoot e inversión perezosa (1:15)

MakeRoot llama a access y marca el camino como invertido. Con esa inversión x pasa a ser la raíz del árbol representado. Al empujar la marca se intercambian hijos y se propaga el cambio. No hace falta visitar todo el camino al marcarlo.

## Loli Aldunate, Cesar Antoni · Motor · Link y cut (5:05)

### Lámina 14 · Link une dos árboles (1:10)

Primero convertimos x en raíz. Buscamos la raíz de y. Si es x, ambos ya estaban en el mismo componente y rechazamos el enlace. Si son árboles distintos, x pasa a colgar de y por un path-parent.

### Lámina 15 · Cut separa vecinos directos (1:15)

Exponemos el camino entre x e y. La condición del código comprueba que tiene exactamente la arista solicitada. Quitamos el hijo izquierdo de y, limpiamos el padre de x y recalculamos los agregados.

### Lámina 16 · Conectividad y ruta (1:00)

Connected compara la raíz representada de los dos nodos. Con suministro, camino expone el trayecto desde FUENTE y el splay permite leer la distancia y el número de tramos. La unión virtual se cuenta como cero.

### Lámina 17 · Simulador de corte y reconexión (1:40)

Partimos de FUENTE, S1, P1 y C1. El primer paso consulta luz y ruta. Cortamos el tramo P1–C1 y comprobamos que C1 queda sin suministro. Luego reconectamos ese tramo y repetimos la consulta. La traza muestra operaciones del LCT.

## Guzman Lopez, Raul Antonio · Resultados (5:15)

### Lámina 18 · Del CSV a la consola (1:10)

Main lee el menú. El lector registra nodos y tramos y el procesador alimenta el motor con los eventos. RedElectrica traduce las operaciones del dominio a LCT. RedIngenua mantiene la referencia con BFS para comparar.

### Lámina 19 · Salida del caso de Magdalena del Mar (1:10)

Tras el primer corte hay doce clientes simulados sin luz. Con ambos cortes activos hay veinte. Al reponer A quedan ocho en B. Al reponer B quedan cero. Se confirma además la vuelta de las rutas para los clientes consultados.

### Lámina 20 · Benchmark · Link-Cut Tree frente a BFS (1:20)

Este gráfico muestra los tiempos medidos del perfil A. Con mil nodos, cambiar a un árbol aleatorio eleva la relación BFS sobre LCT de 26,11 a 57,13. Con más consultas y el mismo árbol profundo, sube a 125,02. A dos mil, B y C quedan más cerca. Para cien mil solo comparamos el LCT, normalizado por M log₂ N. En A tardó 620,36 milisegundos de media, con desviación estándar de 72,59.

### Lámina 21 · Qué muestra RedLuz (1:00)

RedLuz mantiene el bosque al modificar tramos. En el caso simulado recupera las rutas tras las dos reposiciones. El benchmark compara el costo computacional con BFS. Para un modelo eléctrico más completo quedarían por estudiar capacidad y restricciones de operación.

### Lámina 22 · Referencias y preguntas (0:35)

Estas son las referencias del motor y el aviso del caso. Gracias. Podemos volver a la lámina del simulador para revisar cualquier paso.
