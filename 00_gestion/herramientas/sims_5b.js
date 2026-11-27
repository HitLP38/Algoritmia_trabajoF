/* Simulación didáctica del motor Java sobre una red de ejemplo. */
(() => {
  class NodoDemo {
    constructor(nombre, km = 0, tramos = 0) {
      Object.assign(this, { nombre, km, tramos, suma: km, cantidad: tramos,
        izq: null, der: null, padre: null, invertido: false });
    }

    raizSplay() {
      return !this.padre || (this.padre.izq !== this && this.padre.der !== this);
    }

    actualizar() {
      this.suma = this.km + (this.izq?.suma ?? 0) + (this.der?.suma ?? 0);
      this.cantidad = this.tramos + (this.izq?.cantidad ?? 0) + (this.der?.cantidad ?? 0);
    }

    empujar() {
      if (!this.invertido) return;
      [this.izq, this.der] = [this.der, this.izq];
      if (this.izq) this.izq.invertido = !this.izq.invertido;
      if (this.der) this.der.invertido = !this.der.invertido;
      this.invertido = false;
    }
  }

  class MotorDemo {
    traza = [];

    rotar(x) {
      const p = x.padre;
      const g = p.padre;
      const raiz = p.raizSplay();
      if (p.izq === x) {
        p.izq = x.der;
        if (p.izq) p.izq.padre = p;
        x.der = p;
      } else {
        p.der = x.izq;
        if (p.der) p.der.padre = p;
        x.izq = p;
      }
      p.padre = x;
      x.padre = g;
      if (!raiz) {
        if (g.izq === p) g.izq = x;
        else g.der = x;
      }
      p.actualizar();
      x.actualizar();
      this.traza.push("Rotar " + x.nombre + " sobre " + p.nombre);
    }

    splay(x) {
      const pila = [x];
      for (let n = x; !n.raizSplay();) {
        n = n.padre;
        pila.push(n);
      }
      while (pila.length) pila.pop().empujar();
      while (!x.raizSplay()) {
        const p = x.padre;
        if (!p.raizSplay()) {
          const g = p.padre;
          this.rotar((g.izq === p) === (p.izq === x) ? p : x);
        }
        this.rotar(x);
      }
    }

    access(x) {
      this.traza.push("access(" + x.nombre + ")");
      let abajo = null;
      for (let z = x; z; z = z.padre) {
        this.splay(z);
        z.der = abajo;
        z.actualizar();
        abajo = z;
      }
      this.splay(x);
    }

    makeRoot(x) {
      this.access(x);
      x.invertido = !x.invertido;
      this.traza.push("makeRoot(" + x.nombre + ")");
    }

    findRoot(x) {
      this.access(x);
      x.empujar();
      while (x.izq) {
        x = x.izq;
        x.empujar();
      }
      this.splay(x);
      return x;
    }

    link(x, y) {
      this.makeRoot(x);
      if (this.findRoot(y) === x) return false;
      x.padre = y;
      this.traza.push("link(" + x.nombre + ", " + y.nombre + ")");
      return true;
    }

    cut(x, y) {
      this.makeRoot(x);
      this.access(y);
      x.empujar();
      if (y.izq !== x || x.der) return false;
      y.izq = null;
      x.padre = null;
      y.actualizar();
      this.traza.push("cut(" + x.nombre + ", " + y.nombre + ")");
      return true;
    }

    ruta(origen, destino) {
      if (this.findRoot(origen) !== this.findRoot(destino)) return null;
      this.makeRoot(origen);
      this.access(destino);
      return { km: destino.suma, tramos: destino.cantidad };
    }
  }

  const panel = document.getElementById("simRed");
  if (panel) {
    const motor = new MotorDemo();
    const [fuente, s1, p1, c1] = ["FUENTE", "S1", "P1", "C1"].map(n => new NodoDemo(n));
    const tramoA = new NodoDemo("S1–P1", 0.8, 1);
    const tramoB = new NodoDemo("P1–C1", 0.2, 1);
    for (const [a, b] of [[fuente, s1], [s1, tramoA], [tramoA, p1], [p1, tramoB], [tramoB, c1]]) {
      if (!motor.link(a, b)) throw new Error("No se pudo construir la red de ejemplo");
    }
    const pasos = [];
    function capturar(titulo, conectado, cambio = () => {}) {
      motor.traza = [];
      cambio();
      const ruta = motor.ruta(fuente, c1);
      pasos.push({ titulo, conectado, ruta, traza: [...motor.traza] });
    }
    capturar("Red inicial", true);
    capturar("Consultar C1", true);
    capturar("Cortar P1–C1", false, () => {
      if (!motor.cut(p1, tramoB) || !motor.cut(tramoB, c1)) throw new Error("Falló el corte");
    });
    capturar("Consultar C1 tras el corte", false);
    capturar("Reconectar P1–C1", true, () => {
      if (!motor.link(p1, tramoB) || !motor.link(tramoB, c1)) throw new Error("Falló la reconexión");
    });
    capturar("Consultar C1 tras la reposición", true);

    let paso = 0;
    const estado = document.getElementById("simEstado");
    const traza = document.getElementById("simTraza");
    const grafo = document.getElementById("simGrafo");
    function dibujar() {
      const p = pasos[paso];
      const km = p.ruta?.km.toFixed(2).replace(".", ",");
      const coords = [90, 266, 442, 618];
      const nodos = coords.map((x, i) => '<g><circle cx="' + x + '" cy="153" r="45" class="' +
        (i === 3 && !p.ruta ? "sin-luz" : "") + '"/><text x="' + x + '" y="159">' +
        ["FUENTE", "S1", "P1", "C1"][i] + '</text></g>').join("");
      const aristas = coords.slice(0, 3).map((x, i) => '<line x1="' + x + '" x2="' +
        coords[i + 1] + '" y1="153" y2="153" class="' +
        (i === 0 ? "virtual" : i === 2 && !p.conectado ? "cortado" : "") + '"/><text x="' +
        ((x + coords[i + 1]) / 2) + '" y="132">' +
        ["0 km", "0,8 km", p.conectado ? "0,2 km" : "cortado"][i] + '</text>').join("");
      grafo.innerHTML = aristas + nodos;
      estado.innerHTML = "<h3>" + p.titulo + "</h3><p>" +
        (p.ruta ? "C1 con luz · " + km + " km · " + p.ruta.tramos + " tramos" : "C1 sin luz · sin ruta") + "</p>";
      const cambios = p.traza.filter(t => /^(cut|link)/.test(t));
      const ultimas = p.traza.slice(-6);
      traza.innerHTML = "<h3>Traza del motor</h3><ol>" +
        [...new Set([...cambios, ...ultimas])].map(t => "<li>" + t + "</li>").join("") + "</ol>";
      document.getElementById("simPaso").textContent = "Paso " + paso + " / " + (pasos.length - 1);
    }
    panel.addEventListener("click", event => {
      const accion = event.target.closest("button")?.dataset.paso;
      if (accion === "inicio") paso = 0;
      if (accion === "atras") paso = Math.max(0, paso - 1);
      if (accion === "siguiente") paso = Math.min(pasos.length - 1, paso + 1);
      if (accion === "fin") paso = pasos.length - 1;
      if (accion) dibujar();
    });
    dibujar();
  }

  const graficoLct = document.getElementById("grafLct");
  if (graficoLct) {
    grafico(graficoLct, [
      { name: "Link-Cut Tree", color: "#8A1D2C", pts: BENCH_LCT.map(([n, lct]) => [n, lct]) },
      { name: "BFS", color: "#1F5FA8", dash: 1, pts: BENCH_LCT.filter(r => r[2] !== null).map(r => [r[0], r[2]]) },
    ], { xLabel: "N = M", yLabel: "Tiempo total (ms)", logX: 1, logY: 1 });
  }
})();

