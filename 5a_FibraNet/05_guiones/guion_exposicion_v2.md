# Guion de exposición · 5.a FibraNet

Cada bloque indica quién habla, el tiempo objetivo y el texto sugerido. No hay que memorizarlo palabra por palabra: la idea es decir esto con sus palabras.

## C1 · Acosta Simon, Christopher Ricardo · El problema (3:10)

### Lámina 01 · FibraNet: red de fibra de costo mínimo (0:40)

Buenos días. Somos el Grupo 5. Presentamos FibraNet, un modelo inspirado en la Red Dorsal Nacional de Fibra Óptica. En el mapa vemos 24 capitales departamentales y el Callao. Queremos conectarlas con la menor longitud total dentro del grafo de candidatos. Para resolverlo usamos Prim, Kruskal y Union-Find. Primero veremos cómo construimos los candidatos. Después mostraremos el motor y los resultados.

### Lámina 02 · Agenda (0:30)

La exposición tiene seis bloques. Primero el problema y los requerimientos. Luego la teoría del árbol de expansión mínima. Después vienen los tres bloques del motor, marcados en granate: Union-Find, Kruskal y Prim, cada uno con su código y un simulador paso a paso. Cerramos con la arquitectura, la demo, el benchmark contra la teoría y las conclusiones.

### Lámina 03 · El problema de FibraNet (1:00)

Cada sede propone enlaces a sus cuatro vecinas más cercanas. Medimos la distancia con haversine. Al juntar las propuestas, contamos cada enlace una sola vez. Así quedan 65 tramos que suman 16 975,1 kilómetros. En el mapa, los tramos en granate forman el árbol. Ese árbol conecta las 25 sedes con 24 tramos y suma 4 081,4 kilómetros. Es un 76 por ciento menos que sumar todos los candidatos. Son distancias geodésicas del modelo. Para estimar el tendido real habría que estudiar las rutas de obra.

### Lámina 04 · Requerimientos y alcance (1:00)

Antes de programar levantamos los requerimientos. Son 14 funcionales: los más importantes son cargar la red desde un CSV o por teclado, calcular el plan con Kruskal y con Prim, compararlos, consultar si dos sedes quedan conectadas y exportar el plan. Si la red no es conexa, el programa no falla: entrega un bosque, un árbol por cada grupo. Los no funcionales piden consola, Java estándar y buen tiempo con redes grandes. Y dejamos claro lo que no hacemos, como una interfaz gráfica o redundancia.

*Pase:* "Ahora Santiago les explica teoría del mst."

## C2 · Gavilano Kanashiro, Santiago · Teoría del MST (3:30)

### Lámina 05 · Modelo: grafo ponderado y MST (1:10)

Para resolverlo, primero modelamos. Cada sede es un vértice, cada tramo posible es una arista y su costo es el peso. Un plan de tendido válido es un árbol de expansión: toca todos los vértices, no tiene ciclos y por eso tiene exactamente V menos 1 aristas. Buscamos el de menor suma de pesos. A la derecha está el ejemplo que resolvimos a mano y que también es una prueba automática del programa: seis vértices, ocho aristas, y el árbol mínimo, en granate, cuesta 17.

### Lámina 06 · Propiedades de corte y de ciclo (1:10)

¿Por qué podemos ser voraces y elegir siempre lo más barato sin arrepentirnos? Por dos propiedades. La propiedad de corte: si partimos los vértices en dos grupos, la arista más barata que cruza entre ellos siempre está en algún árbol mínimo. Prim la usa en cada paso: su árbol es un lado del corte. La propiedad de ciclo: en cualquier ciclo, la arista más cara sobra. Kruskal la usa cuando rechaza una arista porque cerraría un ciclo, ya que esa arista es la más cara del ciclo que formaría.

### Lámina 07 · Especificación y complejidad (1:10)

Formalmente, la entrada es un grafo con pesos no negativos y la salida un subconjunto de aristas de costo mínimo, sin ciclos. Si el grafo es conexo da V menos 1 aristas; si no, da un árbol por componente. El invariante que garantiza la correctitud es que el conjunto elegido siempre está contenido en algún bosque de expansión mínima; si la red es conexa, ese bosque es un árbol. En costo: los dos pagan V por recorrer las sedes; Kruskal suma E log E por el ordenamiento y Prim con montículo E log V. Si no hay tramos repetidos entre el mismo par de sedes, E es a lo más V al cuadrado y log E es a lo más 2 log V, así que son del mismo orden. Union-Find optimizado cuesta alfa de n amortizado, que en la práctica es una constante menor o igual a 4.

*Pase:* "Ahora Javier les explica union-find."

## C3 · Chionglaw Kong Tarrillo, Javier · Motor 1 · Union-Find (4:00)

### Lámina 08 · Union-Find: la idea (1:00)

Empieza el motor. Union-Find guarda grupos de elementos que no se mezclan. Cada grupo es un árbol y su raíz es el representante. En Java son solo dos arreglos de enteros: padre y rango. Si padre de i es i, i es una raíz. Find sube por los padres hasta la raíz, y de regreso hace que cada nodo apunte directo a ella: eso es compresión de caminos. Union busca las dos raíces y cuelga el árbol más bajo del más alto: eso es unión por rango. Con las dos optimizaciones, cada operación cuesta casi constante.

### Lámina 09 · Union-Find en Java (1:20)

Este es el código real, del archivo UnionFind.java. En find, la línea resaltada hace todo el trabajo: llama a find del padre, que devuelve la raíz, y la guarda en padre de x. Así, al terminar, todos los nodos del camino apuntan a la raíz. En union, primero se buscan las dos raíces. Si son la misma, devuelve false: eso significa que a y b ya estaban conectados, y Kruskal lo interpreta como ciclo. Si no, compara rangos: el más bajo va debajo. Solo en empate el rango sube en uno. Al final descontamos un grupo.

### Lámina 10 · Simulador de Union-Find (1:40)

Ahora lo vemos en acción. Tenemos ocho elementos, de A a H, y voy a avanzar paso a paso. Primero union de A y B: empate de rangos, B cuelga de A y el rango de A sube a 1. Igual con C y D. Luego union de A y C: dos raíces con rango 1, otro empate, y A queda con rango 2. Fíjense en la tabla: cada vez que cambia un padre, la casilla se resalta. Al final hacemos find de H: subimos varios saltos, y en el paso siguiente vemos la compresión: todo el camino queda colgado directo de la raíz.

*Pase:* "Ahora Gianpierre les explica kruskal."

## C4 · Cerron Arica, Gianpierre Eyner · Motor 2 · Kruskal (4:00)

### Lámina 11 · Kruskal: la idea (1:00)

Kruskal piensa en todo el mapa a la vez. Paso uno: ordena los tramos del más barato al más caro. Paso dos: cada sede empieza en su propio grupo. Paso tres: recorre los tramos en orden y para cada uno pregunta a Union-Find si sus dos sedes están en grupos distintos. Si es así, lo acepta y une los grupos. Si ya estaban en el mismo grupo, ese tramo cerraría un ciclo y se rechaza. Cuando el plan llega a n menos 1 tramos se detiene. Y si al final quedan varios grupos, sabemos que la red no es conexa.

### Lámina 12 · Kruskal en Java (1:20)

Este es el núcleo de Kruskal.java. Primero se copia la lista de aristas y se ordena con Collections.sort; la clase Arista compara por costo. Luego se crea un Union-Find con n sedes. En el bucle, la línea clave es grupos punto union: en una sola llamada pregunta si están en grupos distintos y, si lo están, los une. Si devuelve true, el tramo entra al plan. Si devuelve false, se descarta por ciclo. Y la condición del inicio corta el bucle apenas tenemos n menos 1 tramos.

### Lámina 13 · Simulador de Kruskal (1:40)

Empezamos con 25 grupos y los 65 candidatos ordenados por distancia. Pulso Paso. El enlace Callao–Lima, de 8,3 kilómetros, une dos grupos y entra al árbol. Avanzo hasta que se rechace un enlace. Sus extremos ya están conectados. Si lo agregáramos, cerraríamos un ciclo. Pulso Todo. El plan tiene 24 tramos, un solo grupo y 4 081,4 kilómetros. Ahora veremos cómo Prim llega al mismo costo.

*Pase:* "Ahora Raul les explica prim."

## C5 · Guzman Lopez, Raul Antonio · Motor 3 · Prim (4:00)

### Lámina 14 · Prim: la idea (1:00)

Prim piensa distinto: no mira todo el mapa, sino que hace crecer un solo árbol desde una sede. La sede inicial entra al árbol y todos sus tramos van a una cola de prioridad, que es un montículo binario. En cada paso sacamos el tramo más barato. Si llega a una sede que todavía está afuera, esa sede entra y encolamos sus tramos. Si las dos puntas ya están adentro, lo descartamos: por eso se llama versión perezosa, porque no borramos esos tramos de la cola, solo los ignoramos al salir. Esto es justo la propiedad de corte: el árbol es un lado del corte y siempre tomamos el cruce más barato.

### Lámina 15 · Prim en Java (1:20)

Este es el núcleo de Prim.java. Mientras la cola no esté vacía, sacamos el mínimo con poll. Revisamos con el arreglo enArbol si las dos puntas ya están dentro; si es así, continue, se descarta. Si no, la punta que está afuera es la sede nueva: agregamos el tramo al plan y llamamos a visitar. Visitar, abajo, marca la sede y mete en la cola solo los tramos que van hacia sedes de afuera. Si la red no es conexa, el bucle externo arranca otro árbol desde una sede no visitada, así Prim también entrega el bosque.

### Lámina 16 · Simulador de Prim (1:40)

Prim empieza en Lima. En la cola se ve el tramo más barato arriba. Pulso Paso. Callao entra por el enlace de 8,3 kilómetros. Cada sede nueva añade a la cola sus conexiones hacia las sedes de afuera. Si sale un enlace con ambos extremos dentro, se descarta. Pulso Todo. Ahora las 25 sedes están en el árbol. El total es 4 081,4 kilómetros, igual que en Kruskal.

*Pase:* "Ahora Cesar les explica resultados."

## C6 · Loli Aldunate, Cesar Antoni · Resultados (5:40)

### Lámina 17 · Arquitectura y demo por consola (1:20)

En la consola elijo la opción diez. LectorCoordenadas lee las latitudes y longitudes del CSV. Calcula las distancias y arma la lista de candidatos. El motor recibe ese mismo grafo para Kruskal y Prim. A la derecha vemos el resultado de los dos: 24 enlaces y 4 081,4 kilómetros. La versión actual pasó 47 verificaciones. Entre ellas está la comparación de los dos algoritmos en este caso. La demo de consola entra en este tiempo. Si falla la apertura, la lámina ya contiene la salida resumida.

### Lámina 18 · Benchmark: Kruskal vs Prim (1:20)

Este benchmark se hizo con grafos sintéticos, de mil a cincuenta mil sedes. Para cada tamaño promediamos cinco corridas, después de dos de calentamiento. En el grafo denso de cincuenta mil sedes, Kruskal tardó 284,19 milisegundos y Prim 918,52. Al dividir por E log V, las razones varían según el tamaño y la densidad. Esta medición compara el rendimiento en nuestra PC.

### Lámina 19 · Benchmark: Union-Find (1:10)

Aquí comparamos Union-Find sin optimizar contra el optimizado, en escala logarítmica en los dos ejes. La versión ingenua crece en línea recta: cada vez que n se duplica, el costo por operación también, porque los árboles se vuelven listas largas. La versión con compresión y unión por rango se mantiene casi plana, entre 5 y 8 nanosegundos por operación desde cinco mil hasta un millón de elementos. Con veinte mil elementos ya es unas 2 900 veces más rápida. Eso confirma en la práctica que alfa de n se comporta como una constante.

### Lámina 20 · Conclusiones (1:00)

El plan conecta las 25 sedes con 24 tramos. Suma 4 081,4 kilómetros, frente a 16 975,1 de los candidatos. Kruskal y Prim obtienen la misma longitud mínima. Se diferencian en cómo eligen el siguiente enlace. Kruskal ordena toda la lista. Prim mantiene una cola que crece con el árbol. En el grafo denso más grande de nuestra prueba, Kruskal fue más rápido. Para acercarnos a un diseño de obra, falta evaluar rutas, costos por tramo y enlaces de respaldo.

### Lámina 21 · Referencias (0:20)

Estas son las referencias en formato IEEE, las mismas del informe. Las principales son el libro de Cormen, los artículos originales de Kruskal y de Prim, y el artículo de Tarjan que demuestra la cota de alfa de n para Union-Find.

### Lámina 22 · Preguntas y gracias (0:30)

Con esto terminamos. Dejamos tres ideas para recordar: un árbol con n sedes tiene n menos 1 tramos; si union devuelve false, el tramo cerraría un ciclo; y Kruskal y Prim dan el mismo costo en tiempo E log V. Muchas gracias. Quedamos atentos a sus preguntas; cualquiera de nosotros puede responder sobre el motor.

