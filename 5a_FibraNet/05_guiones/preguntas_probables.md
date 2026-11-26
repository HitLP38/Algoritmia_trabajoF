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
   Dan el mismo **costo**. Los tramos pueden variar si hay empates de costo (en nuestra red hay varios tramos de costo 3, 4, 5 y 6).

8. **¿Por qué E log E es lo mismo que E log V?** (Santiago)
   Porque E ≤ V², entonces log E ≤ 2 log V.

## Sobre el diseño y los resultados

9. **¿Por qué Java y no C++? La rúbrica habla de punteros.** (Cesar)
   Java es el equivalente que usamos: los arreglos `int[] padre` y `int[] rango` son memoria contigua indexada como en C, y medimos memoria con `Runtime`. Las referencias entre objetos cumplen el papel de los punteros.

10. **¿Cómo midieron los tiempos?** (Cesar)
    Semilla fija 2026, 2 corridas de calentamiento para el JIT, promedio de 5 corridas con `System.nanoTime()`, y redes de 1 000 a 50 000 sedes, dispersas y densas.

11. **¿Por qué Prim sale más lento en redes densas?** (Cesar / Raul)
    Hace muchas inserciones y extracciones en el montículo (`PriorityQueue`), mientras que Kruskal ordena un arreglo una sola vez, operación muy optimizada en Java.

12. **¿Cómo prueban que el programa es correcto?** (Christopher)
    31 pruebas automáticas: ejemplo resuelto a mano (costo 17), Prim desde dos sedes distintas, red no conexa, CSV con errores, y Kruskal = Prim en 200 redes aleatorias.

13. **¿Qué pasa si el CSV tiene una línea mal escrita?** (Christopher)
    Se descarta esa línea, se muestra el motivo y el programa sigue con las demás (RNF de robustez).
