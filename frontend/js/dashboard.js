document.addEventListener("DOMContentLoaded", () => {
  console.log("Dashboard conectado al backend");

  // 1. CONFIGURACIÓN

  const API_BASE = (() => {
    if (typeof API_URL !== "undefined") {
      return API_URL.replace(/\/$/, "");
    }
    return "http://localhost:8080/api";
  })();

  let graficoVentas = null;
  let graficoProductos = null;

  // 2. PETICIONES AL BACKEND

  async function consultarAPI(endpoint) {
    const token =
      localStorage.getItem("token") || localStorage.getItem("jwtToken");

    const headers = {
      "Content-Type": "application/json",
    };

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const respuesta = await fetch(`${API_BASE}${endpoint}`, {
      method: "GET",
      headers,
    });

    if (respuesta.status === 401 || respuesta.status === 403) {
      throw new Error("No tienes autorización para consultar " + endpoint);
    }

    if (!respuesta.ok) {
      throw new Error(`Error HTTP ${respuesta.status} en ${endpoint}`);
    }

    return respuesta.json();
  }

  // 3. UTILIDADES

  function formatoMoneda(valor) {
    return "S/ " + Number(valor || 0).toFixed(2);
  }

  function obtenerFechaLocal(fecha) {
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, "0");
    const dia = String(fecha.getDate()).padStart(2, "0");

    return `${anio}-${mes}-${dia}`;
  }

  function convertirFecha(fecha) {
    if (!fecha) return null;

    // LocalDateTime de Spring Boot: YYYY-MM-DDTHH:mm:ss
    const partes = String(fecha).split("T");
    const [anio, mes, dia] = partes[0].split("-").map(Number);

    if (!anio || !mes || !dia) return null;

    const [hora = 0, minuto = 0, segundo = 0] = (partes[1] || "00:00:00")
      .split(":")
      .map(Number);

    return new Date(anio, mes - 1, dia, hora, minuto, segundo);
  }

  function esVentaCompletada(venta) {
    return String(venta.estado || "").toUpperCase() === "COMPLETADA";
  }

  function esActivo(registro) {
    return String(registro.estado || "").toUpperCase() === "ACTIVO";
  }

  function actualizarTexto(id, valor) {
    const elemento = document.getElementById(id);

    if (elemento) {
      elemento.textContent = valor;
    }
  }

  // 4. CARGAR INFORMACIÓN GENERAL

  async function cargarDashboard() {
    const resultados = await Promise.allSettled([
      consultarAPI("/ventas"),
      consultarAPI("/productos"),
      consultarAPI("/clientes"),
    ]);

    const [resultadoVentas, resultadoProductos, resultadoClientes] = resultados;

    let ventas = [];
    let productos = [];
    let clientes = [];

    if (resultadoVentas.status === "fulfilled") {
      ventas = Array.isArray(resultadoVentas.value)
        ? resultadoVentas.value
        : [];
    } else {
      console.error("Error al cargar ventas:", resultadoVentas.reason);
      actualizarTexto("totalVentas", "—");
    }

    if (resultadoProductos.status === "fulfilled") {
      productos = Array.isArray(resultadoProductos.value)
        ? resultadoProductos.value
        : [];
    } else {
      console.error("Error al cargar productos:", resultadoProductos.reason);
      actualizarTexto("totalProductos", "—");
      actualizarTexto("totalStockBajo", "—");
    }

    if (resultadoClientes.status === "fulfilled") {
      clientes = Array.isArray(resultadoClientes.value)
        ? resultadoClientes.value
        : [];
    } else {
      console.error("Error al cargar clientes:", resultadoClientes.reason);
      actualizarTexto("totalClientes", "—");
    }

    // 5. VENTAS DEL DÍA

    if (resultadoVentas.status === "fulfilled") {
      const hoy = obtenerFechaLocal(new Date());

      const ventasHoy = ventas.filter((venta) => {
        if (!esVentaCompletada(venta)) return false;

        const fecha = convertirFecha(venta.fechaVenta);

        return fecha && obtenerFechaLocal(fecha) === hoy;
      });

      const ingresosHoy = ventasHoy.reduce(
        (suma, venta) => suma + Number(venta.total || 0),
        0,
      );

      actualizarTexto("totalVentas", formatoMoneda(ingresosHoy));
    }

    // 6. PRODUCTOS REGISTRADOS

    if (resultadoProductos.status === "fulfilled") {
      const productosActivos = productos.filter(esActivo);

      actualizarTexto("totalProductos", productosActivos.length);

      const productosStockBajo = productosActivos.filter((producto) => {
        const stock = Number(producto.stock || 0);
        const minimo = Number(producto.stockMinimo || 0);

        return stock <= minimo;
      });

      actualizarTexto("totalStockBajo", productosStockBajo.length);
    }

    // 7. CLIENTES REGISTRADOS

    if (resultadoClientes.status === "fulfilled") {
      const clientesActivos = clientes.filter(esActivo);

      actualizarTexto("totalClientes", clientesActivos.length);
    }

    // 8. GRÁFICO SEMANAL

    if (resultadoVentas.status === "fulfilled") {
      crearGraficoVentas(ventas);
    }

    // 9. GRÁFICO DE PRODUCTOS MÁS VENDIDOS

    if (resultadoVentas.status === "fulfilled") {
      await crearGraficoProductos(ventas);
    }
  }

  // 10. GRÁFICO DE VENTAS

  function crearGraficoVentas(ventas) {
    const contexto = document.getElementById("graficoVentas");

    if (!contexto) return;

    const hoy = new Date();
    const fechas = [];

    // Últimos siete días, incluyendo hoy.
    for (let i = 6; i >= 0; i--) {
      const fecha = new Date(
        hoy.getFullYear(),
        hoy.getMonth(),
        hoy.getDate() - i,
      );

      fechas.push(fecha);
    }

    const etiquetas = fechas.map((fecha) =>
      fecha.toLocaleDateString("es-PE", {
        weekday: "short",
        day: "2-digit",
      }),
    );

    const totales = fechas.map((fecha) => {
      const fechaBuscada = obtenerFechaLocal(fecha);

      return ventas
        .filter((venta) => {
          if (!esVentaCompletada(venta)) return false;

          const fechaVenta = convertirFecha(venta.fechaVenta);

          return fechaVenta && obtenerFechaLocal(fechaVenta) === fechaBuscada;
        })
        .reduce((suma, venta) => {
          return suma + Number(venta.total || 0);
        }, 0);
    });

    if (graficoVentas) {
      graficoVentas.destroy();
    }

    graficoVentas = new Chart(contexto, {
      type: "line",

      data: {
        labels: etiquetas,

        datasets: [
          {
            label: "Ventas (S/)",
            data: totales,

            borderColor: "#16834b",
            backgroundColor: "rgba(22, 131, 75, 0.12)",

            borderWidth: 3,
            fill: true,
            tension: 0.4,

            pointRadius: 4,
            pointBackgroundColor: "#16834b",
          },
        ],
      },

      options: {
        responsive: true,
        maintainAspectRatio: false,

        plugins: {
          legend: {
            display: false,
          },

          tooltip: {
            callbacks: {
              label: (contexto) => "Ventas: " + formatoMoneda(contexto.raw),
            },
          },
        },

        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (valor) => "S/ " + valor,
            },
          },
        },
      },
    });
  }

  // 11. GRÁFICO DE PRODUCTOS MÁS VENDIDOS

  async function crearGraficoProductos(ventas) {
    const contexto = document.getElementById("graficoProductos");

    if (!contexto) return;

    const ventasCompletadas = ventas.filter(esVentaCompletada);

    const acumulado = new Map();

    // El listado de ventas no contiene los productos.
    // Consultamos el detalle individual de cada venta.

    const detalles = await Promise.allSettled(
      ventasCompletadas.map((venta) =>
        consultarAPI(`/ventas/${venta.idVenta}`),
      ),
    );

    detalles.forEach((resultado) => {
      if (resultado.status !== "fulfilled") {
        console.error(
          "No se pudo consultar un detalle de venta:",
          resultado.reason,
        );
        return;
      }

      const detalle = resultado.value;

      (detalle.items || []).forEach((item) => {
        const id = item.idProducto;
        const nombre = item.nombre || "Producto";
        const cantidad = Number(item.cantidad || 0);

        const clave = id ?? nombre;

        const actual = acumulado.get(clave) || {
          nombre,
          cantidad: 0,
        };

        actual.cantidad += cantidad;

        acumulado.set(clave, actual);
      });
    });

    const ranking = Array.from(acumulado.values())
      .sort((a, b) => b.cantidad - a.cantidad)
      .slice(0, 5);

    const etiquetas = ranking.length
      ? ranking.map((producto) => producto.nombre)
      : ["Sin ventas registradas"];

    const cantidades = ranking.length
      ? ranking.map((producto) => producto.cantidad)
      : [1];

    if (graficoProductos) {
      graficoProductos.destroy();
    }

    graficoProductos = new Chart(contexto, {
      type: "doughnut",

      data: {
        labels: etiquetas,

        datasets: [
          {
            data: cantidades,

            backgroundColor: [
              "#16834b",
              "#3478d4",
              "#8154c7",
              "#e58b27",
              "#aab5ae",
            ],

            borderWidth: 2,
            borderColor: "#ffffff",
          },
        ],
      },

      options: {
        responsive: true,
        maintainAspectRatio: false,

        plugins: {
          legend: {
            position: "bottom",
          },

          tooltip: {
            callbacks: {
              label: (contexto) => {
                if (!ranking.length) {
                  return "Sin ventas registradas";
                }

                return `${contexto.label}: ${contexto.raw} unidades`;
              },
            },
          },
        },
      },
    });
  }

  // 12. INICIAR DASHBOARD

  cargarDashboard();
});
