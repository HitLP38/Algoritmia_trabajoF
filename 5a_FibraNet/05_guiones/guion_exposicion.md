# Guion de exposición · 5.a FibraNet

Cada bloque indica quién habla, el tiempo objetivo y el texto sugerido. No hay que memorizarlo palabra por palabra: la idea es decir esto con sus palabras.

## C1 · Acosta Simon, Christopher Ricardo · El problema (3:10)

### Lámina 01 · FibraNet: red de fibra de costo mínimo (0:40)

Buenos días. Somos el Grupo 5 y presentamos FibraNet, el tema 5.a del proyecto final. El problema es muy concreto: una empresa quiere unir todas sus sedes de Lima con fibra óptica gastando lo menos posible. Para resolverlo usamos tres herramientas del curso: el algoritmo de Kruskal, el algoritmo de Prim y la estructura Union-Find. Cada integrante va a explicar una parte, y los tres bloques del motor los explicamos Javier, Gianpierre y Raul.

### Lámina 02 · Agenda (0:30)

La exposición tiene seis bloques. Primero el problema y los requerimientos. Luego la teoría del árbol de expansión mínima. Después vienen los tres bloques del motor, marcados en granate: Union-Find, Kruskal y Prim, cada uno con su código y un simulador paso a paso. Cerramos con la arquitectura, la demo, el benchmark contra la teoría y las conclusiones.

### Lámina 03 · El problema de FibraNet (1:00)

Este es el caso. FibraNet tiene 12 sedes en Lima y un estudio de campo que dio 18 tramos posibles, cada uno con su costo en miles de soles. Si tendiéramos todos los tramos gastaríamos 137. Pero para que todas las sedes se comuniquen basta un árbol: 11 tramos, que es n menos 1. El mejor árbol cuesta 61, un ahorro de 55,5 por ciento. Ese árbol se llama árbol de expansión mínima, y es lo que calcula nuestro programa.

### Lámina 04 · Requerimientos y alcance (1:00)

Antes de programar levantamos los requerimientos. Son 14 funcionales: los más importantes son cargar la red desde un CSV o por teclado, calcular el plan con Kruskal y con Prim, compararlos, consultar si dos sedes quedan conectadas y exportar el plan. Si la red no es conexa, el programa no falla: entrega un bosque, un árbol por cada grupo. Los no funcionales piden consola, Java estándar y buen tiempo con redes grandes. Y dejamos claro lo que no hacemos, como una interfaz gráfica o redundancia.

*Pase:* "Ahora Santiago les explica teoría del mst."

## C2 · Gavilano Kanashiro, Santiago · Teoría del MST (3:30)

### Lámina 05 · Modelo: grafo ponderado y MST (1:10)

Para resolverlo, primero modelamos. Cada sede es un vértice, cada tramo posible es una arista y su costo es el peso. Un plan de tendido válido es un árbol de expansión: toca todos los vértices, no tiene ciclos y por eso tiene exactamente V menos 1 aristas. Buscamos el de menor suma de pesos. A la derecha está el ejemplo que resolvimos a mano y que también es una prueba automática del programa: seis vértices, ocho aristas, y el árbol mínimo, en granate, cuesta 17.

### Lámina 06 · Propiedades de corte y de ciclo (1:10)

¿Por qué podemos ser voraces y elegir siempre lo más barato sin arrepentirnos? Por dos propiedades. La propiedad de corte: si partimos los vértices en dos grupos, la arista más barata que cruza entre ellos siempre está en algún árbol mínimo. Prim la usa en cada paso: su árbol es un lado del corte. La propiedad de ciclo: en cualquier ciclo, la arista más cara sobra. Kruskal la usa cuando rechaza una arista porque cerraría un ciclo, ya que esa arista es la más cara del ciclo que formaría.

### Lámina 07 · Especificación y complejidad (1:10)

Formalmente, la entrada es un grafo con pesos no negativos y la salida un subconjunto de aristas de costo mínimo, sin ciclos. Si el grafo es conexo da V menos 1 aristas; si no, da un árbol por componente. El invariante que garantiza la correctitud es que el conjunto elegido siempre está contenido en algún árbol mínimo. En costo: Kruskal es E log E por el ordenamiento, Prim con montículo es E log V, y como E es a lo más V al cuadrado, log E es a lo más 2 log V, así que son del mismo orden. Union-Find optimizado cuesta alfa de n amortizado, que en la práctica es una constante menor o igual a 4.

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

Este es Kruskal con nuestra red real de 12 sedes. A la derecha está la lista de tramos ya ordenada. Voy paso a paso. Miraflores–Barranco, costo 2: grupos distintos, se acepta. Siguen San Isidro–Miraflores y Barranco–Chorrillos, ambos con costo 3. Los colores de las sedes muestran los grupos de Union-Find. Fíjense en Surco–Chorrillos, costo 6: las dos sedes ya están en el mismo grupo, así que se rechaza por ciclo. Lo mismo pasa con Sede Central–Miraflores y San Isidro–Surco. En el paso 14 tenemos 11 tramos y un solo grupo: costo 61.

*Pase:* "Ahora Raul les explica prim."

## C5 · Guzman Lopez, Raul Antonio · Motor 3 · Prim (4:00)

### Lámina 14 · Prim: la idea (1:00)

Prim piensa distinto: no mira todo el mapa, sino que hace crecer un solo árbol desde una sede. La sede inicial entra al árbol y todos sus tramos van a una cola de prioridad, que es un montículo binario. En cada paso sacamos el tramo más barato. Si llega a una sede que todavía está afuera, esa sede entra y encolamos sus tramos. Si las dos puntas ya están adentro, lo descartamos: por eso se llama versión perezosa, porque no borramos esos tramos de la cola, solo los ignoramos al salir. Esto es justo la propiedad de corte: el árbol es un lado del corte y siempre tomamos el cruce más barato.

### Lámina 15 · Prim en Java (1:20)

Este es el núcleo de Prim.java. Mientras la cola no esté vacía, sacamos el mínimo con poll. Revisamos con el arreglo enArbol si las dos puntas ya están dentro; si es así, continue, se descarta. Si no, la punta que está afuera es la sede nueva: agregamos el tramo al plan y llamamos a visitar. Visitar, abajo, marca la sede y mete en la cola solo los tramos que van hacia sedes de afuera. Si la red no es conexa, el bucle externo arranca otro árbol desde una sede no visitada, así Prim también entrega el bosque.

### Lámina 16 · Simulador de Prim (1:40)

Ahora Prim con la misma red, empezando en Sede Central. Las sedes en granate ya están en el árbol y los tramos azules están en la cola. A la derecha se ve la cola de prioridad con el mínimo arriba. Primer paso: sale Sede Central–San Isidro, costo 4, y San Isidro entra. Luego San Isidro–Miraflores con 3, luego Miraflores–Barranco con 2. Cuando sale un tramo con las dos puntas adentro, como Sede Central–Miraflores, se descarta. Al vaciarse la cola tenemos las 12 sedes y el mismo costo: 61. Los dos algoritmos coinciden.

*Pase:* "Ahora Cesar les explica resultados."

## C6 · Loli Aldunate, Cesar Antoni · Resultados (5:40)

### Lámina 17 · Arquitectura y demo por consola (1:20)

El programa está en Java, por consola, en cuatro paquetes. Main tiene el menú. El paquete io lee y escribe CSV y dibuja las tablas. El paquete motor tiene los tres algoritmos que acabamos de ver. Y modelo guarda el grafo como listas de adyacencia. Aparte están las pruebas, 31 verificaciones que pasan todas, y el benchmark. A la derecha, la opción 6 del menú compara Kruskal y Prim sobre la red de Lima: los dos dan 61 con 11 tramos. Si da tiempo, hacemos la demo en vivo con el archivo red_ejemplo.csv.

### Lámina 18 · Benchmark: Kruskal vs Prim (1:20)

Para el benchmark generamos redes al azar con semilla fija 2026, desde mil hasta cincuenta mil sedes, dispersas y densas. Cada medición tiene dos corridas de calentamiento y es el promedio de cinco corridas. Las dos curvas crecen casi en línea con E log V, como predice la teoría: dividimos el tiempo entre E log V y el cociente se mantiene estable. En redes densas Kruskal sale más rápido, porque ordenar un arreglo es muy eficiente en Java y Prim hace muchas operaciones en el montículo.

### Lámina 19 · Benchmark: Union-Find (1:10)

Aquí comparamos Union-Find sin optimizar contra el optimizado, en escala logarítmica en los dos ejes. La versión ingenua crece en línea recta: cada vez que n se duplica, el costo por operación también, porque los árboles se vuelven listas largas. La versión con compresión y unión por rango se mantiene casi plana, entre 3 y 5 nanosegundos por operación, incluso con un millón de elementos. Con veinte mil elementos ya es unas tres mil veces más rápida. Eso confirma en la práctica que alfa de n se comporta como una constante.

### Lámina 20 · Conclusiones (1:00)

Cerramos con tres conclusiones. Primera: para FibraNet el plan cuesta 61 en vez de 137, un ahorro de más de la mitad. Segunda: los experimentos confirman la teoría: Kruskal y Prim encuentran el mismo costo, crecen como E log V, y Union-Find optimizado es prácticamente constante. Tercera: Kruskal conviene cuando los tramos llegan como una lista, como en nuestro CSV; Prim conviene con redes muy densas ya guardadas como adyacencias. Como trabajo futuro: Prim con montículo indexado, un árbol de respaldo y redes que cambian en el tiempo, que es justo nuestro tema 5.b.

### Lámina 21 · Referencias (0:20)

Estas son las referencias en formato IEEE, las mismas del informe. Las principales son el libro de Cormen, los artículos originales de Kruskal y de Prim, y el artículo de Tarjan que demuestra la cota de alfa de n para Union-Find.

### Lámina 22 · Preguntas y gracias (0:30)

Con esto terminamos. Dejamos tres ideas para recordar: un árbol con n sedes tiene n menos 1 tramos; si union devuelve false, el tramo cerraría un ciclo; y Kruskal y Prim dan el mismo costo en tiempo E log V. Muchas gracias. Quedamos atentos a sus preguntas; cualquiera de nosotros puede responder sobre el motor.

