# Preguntas probables del jurado · 5.a FibraNet

Cualquiera de los 6 debe poder responderlas. Entre paréntesis, quién responde primero.

## Sobre el motor

1. **¿Por qué Kruskal no forma ciclos?** (Gianpierre)
   Porque antes de aceptar un tramo pregunta a Union-Find si sus dos sedes ya están en el mismo grupo. Si lo están, ya existe un camino entre ellas y el tramo cerraría un ciclo; `union` devuelve `false` y se descarta.

2. **¿Qué hace exactamente la compresión de caminos?** (Javier)
   En `find`, la línea `padre[x] = find(padre[x])` guarda la raíz en cada nodo del camino. La próxima consulta sobre esos nodos da un solo salto.

3. **¿Para qué sirve el rango si ya hay compresión?** (Javier)
   Evita que el árbol crezca en altura al unir: el más bajo se cuelga del más alto. Solo con rango la altura ya es O(log n); con las dos optimizaciones el costo amortizado es O(α(n)).

4. **¿Qué es α(n)?** (Santiago)
   La inversa de la función de Ackermann. Crece tan lento que es ≤ 4 para cualquier n que quepa en una computadora; en la práctica es una constante.

5. **¿Por qué Prim es "perezoso"?** (Raul)
   Porque no borra de la cola los tramos que quedaron con las dos puntas adentro. Los descarta cuando salen (`continue`). Es más simple que la versión con decrease-key y tiene el mismo orden, O(E log V).

6. **¿Qué pasa si la red no es conexa?** (Raul / Gianpierre)
   Kruskal termina con más de un grupo (`numeroGrupos() > 1`). Prim, al vaciarse la cola, empieza otro árbol desde una sede no visitada. Los dos entregan un bosque, uno por componente.

7. **¿Kruskal y Prim siempre dan los mismos tramos?** (Gianpierre)
   Dan el mismo **costo**. Los tramos pueden variar si hay empates de costo. Si la red no es conexa, los dos dan un bosque del mismo costo.

8. **¿Por qué E log E es lo mismo que E log V?** (Santiago)
   Si no hay dos tramos entre el mismo par de sedes, E ≤ V², entonces log E ≤ 2 log V. Nuestro lector sí acepta tramos repetidos (dos ofertas entre las mismas sedes); en ese caso la cota correcta es O(V + E log E), porque E ya no está acotado por V². En las redes del proyecto no hay pares repetidos.

## Sobre el diseño y los resultados

9. **¿Por qué Java y no C++? La rúbrica habla de punteros.** (Cesar)
   Java es el equivalente que usamos: los arreglos `int[] padre` y `int[] rango` son memoria contigua indexada como en C, y medimos memoria con `Runtime`. Las referencias entre objetos cumplen el papel de los punteros.

10. **¿Cómo midieron los tiempos?** (Cesar)
    Semilla fija 2026, 2 corridas de calentamiento para el JIT, promedio de 5 corridas con `System.nanoTime()`, y redes de 1 000 a 50 000 sedes, dispersas y densas.

11. **¿Por qué Prim sale más lento en redes densas?** (Cesar / Raul)
    Hace muchas inserciones y extracciones en el montículo (`PriorityQueue`), mientras que Kruskal ordena un arreglo una sola vez, operación muy optimizada en Java. Con 50 000 sedes densas: Kruskal 284 ms y Prim 919 ms. El cociente tiempo / (E log V) de Prim pasa de 31,6 a 75,4 ns entre los extremos densos medidos. Las pruebas midieron el tiempo total, así que el efecto de la caché queda como hipótesis.

12. **¿Cómo prueban que el programa es correcto?** (Christopher)
    47 verificaciones automáticas: ejemplo resuelto a mano (costo 17), Prim desde dos sedes distintas, red no conexa, CSV con errores, y Kruskal = Prim en 200 redes aleatorias.

13. **¿Qué pasa si el CSV tiene una línea mal escrita?** (Christopher)
    Se descarta esa línea, se muestra el motivo y el programa sigue con las demás (RNF de robustez).

## Sobre el caso Red Dorsal

14. **¿Qué calcula haversine y por qué la usaron?** (Santiago)
    Calcula la longitud del arco más corto entre dos coordenadas sobre una esfera. Permite dar un peso comparable a cada tramo usando solo latitud y longitud. Es un mínimo geométrico en el modelo esférico. Para estimar cable de obra hacen falta rutas, permisos y relieve.

15. **¿Por qué eligieron k = 4?** (Christopher)
    Es un parámetro de diseño del caso. Cada sede propone sus cuatro vecinas más cercanas. Al quitar las propuestas duplicadas, quedan 65 tramos y, en este conjunto de coordenadas, el grafo resulta conexo. Con otro valor de k habría que comprobar la conectividad y volver a calcular el árbol.

16. **¿Por qué Iquitos queda unido por el tramo más largo del árbol?** (Gianpierre)
    Iquitos está lejos de las otras sedes del modelo. Su candidato más corto es el enlace a Moyobamba, de 483,9 km. Es el enlace más barato que cruza el corte entre Iquitos y las otras 24 sedes, por lo que pertenece al árbol mínimo. Es el mayor peso entre los 24 tramos seleccionados.

17. **¿En qué se diferencia FibraNet de la RDNFO real?** (Cesar)
    La RDNFO es infraestructura de transporte de telecomunicaciones. FibraNet usa 25 sedes y distancias geodésicas para estudiar un árbol de longitud mínima. La planificación de una red real incorpora trazados de obra, cobertura, capacidad, redundancia y costos. El 76 % compara el árbol con la suma de los candidatos del ejercicio.
