document.addEventListener("DOMContentLoaded", async function () {
  // ELEMENTOS HTML

  const fechaDesde = document.getElementById("fechaDesde");
  const fechaHasta = document.getElementById("fechaHasta");

  const btnFiltrar = document.getElementById("btnFiltrarReportes");
  const btnLimpiar = document.getElementById("btnLimpiarReportes");
  const btnActualizar = document.getElementById("btnActualizarReportes");

  const totalVentas = document.getElementById("reporteTotalVentas");
  const ingresosTotales = document.getElementById("reporteIngresos");
  const promedioVenta = document.getElementById("reportePromedio");
  const clientesAtendidos = document.getElementById("reporteClientes");

  const tablaMetodosPago = document.getElementById("tablaMetodosPago");
  const vacioTablaReportes = document.getElementById("vacioTablaReportes");

  const canvasIngresos = document.getElementById("graficoIngresos");
  const canvasMetodos = document.getElementById("graficoMetodosPago");

  const vacioIngresos = document.getElementById("vacioIngresos");
  const vacioMetodos = document.getElementById("vacioMetodos");

  // VARIABLES

  let ventas = [];
  let graficoIngresos = null;
  let graficoMetodos = null;

  // NOTIFICACIONES

  function notificar(mensaje, tipo = "success") {
    if (typeof mostrarNotificacion === "function") {
      mostrarNotificacion(mensaje, tipo);
    } else {
      console.log(mensaje);
    }
  }

  // FORMATEAR MONEDA

  function formatearMoneda(valor) {
    return new Intl.NumberFormat("es-PE", {
      style: "currency",
      currency: "PEN",
    }).format(Number(valor) || 0);
  }

  // OBTENER FECHA LOCAL

  function obtenerFechaLocal(fecha) {
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, "0");
    const dia = String(fecha.getDate()).padStart(2, "0");

    return `${anio}-${mes}-${dia}`;
  }

  // OBTENER FECHA DE UNA VENTA

  function obtenerFechaVenta(venta) {
    if (!venta.fechaVenta) {
      return "";
    }

    const fecha = new Date(venta.fechaVenta);

    if (Number.isNaN(fecha.getTime())) {
      return "";
    }

    return obtenerFechaLocal(fecha);
  }

  // CARGAR VENTAS DESDE SQL SERVER

  async function cargarVentas() {
    try {
      if (btnActualizar) {
        btnActualizar.disabled = true;
      }

      const respuesta = await realizarPeticion(`${API_URL}/ventas`);

      const datos = await respuesta.json();

      if (!respuesta.ok) {
        throw new Error(
          datos.mensaje ||
            datos.message ||
            "No se pudieron obtener las ventas.",
        );
      }

      if (!Array.isArray(datos)) {
        throw new Error("El backend no devolvió una lista válida de ventas.");
      }

      ventas = datos;

      console.log("Ventas obtenidas desde SQL Server:", ventas);

      actualizarReportes();

      notificar("Los reportes se cargaron correctamente.", "success");
    } catch (error) {
      console.error("Error al cargar reportes:", error);

      notificar(error.message, "error");
    } finally {
      if (btnActualizar) {
        btnActualizar.disabled = false;
      }
    }
  }

  // OBTENER VENTAS COMPLETADAS

  function obtenerVentasCompletadas(lista) {
    return lista.filter(
      (venta) => String(venta.estado || "").toUpperCase() === "COMPLETADA",
    );
  }

  // FILTRAR VENTAS POR FECHAS

  function filtrarVentas() {
    const desde = fechaDesde.value;
    const hasta = fechaHasta.value;

    if (desde && hasta && desde > hasta) {
      notificar(
        "La fecha inicial no puede ser posterior a la fecha final.",
        "warning",
      );

      return null;
    }

    const ventasCompletadas = obtenerVentasCompletadas(ventas);

    return ventasCompletadas.filter((venta) => {
      const fechaVenta = obtenerFechaVenta(venta);

      if (!fechaVenta) {
        return false;
      }

      if (desde && fechaVenta < desde) {
        return false;
      }

      if (hasta && fechaVenta > hasta) {
        return false;
      }

      return true;
    });
  }

  // ACTUALIZAR TARJETAS ESTADÍSTICAS

  function actualizarEstadisticas(lista) {
    const cantidad = lista.length;

    const ingresos = lista.reduce(
      (acumulado, venta) => acumulado + Number(venta.total || 0),
      0,
    );

    const promedio = cantidad > 0 ? ingresos / cantidad : 0;

    // Contar clientes únicos.
    // No se considera Cliente genérico.

    const clientesUnicos = new Set(
      lista
        .map((venta) => (venta.cliente || "").trim())
        .filter(
          (cliente) =>
            cliente !== "" &&
            cliente.toLowerCase() !== "cliente genérico" &&
            cliente.toLowerCase() !== "cliente generico",
        ),
    );

    totalVentas.textContent = cantidad;

    ingresosTotales.textContent = formatearMoneda(ingresos);

    promedioVenta.textContent = formatearMoneda(promedio);

    clientesAtendidos.textContent = clientesUnicos.size;
  }

  // AGRUPAR INGRESOS POR DÍA

  function obtenerIngresosPorDia(lista) {
    const resumen = {};

    lista.forEach((venta) => {
      const fecha = obtenerFechaVenta(venta);

      if (!fecha) return;

      if (!resumen[fecha]) {
        resumen[fecha] = 0;
      }

      resumen[fecha] += Number(venta.total || 0);
    });

    const fechas = Object.keys(resumen).sort();

    return {
      etiquetas: fechas.map((fecha) => {
        const partes = fecha.split("-");

        return `${partes[2]}/${partes[1]}`;
      }),

      valores: fechas.map((fecha) => resumen[fecha]),
    };
  }

  // MOSTRAR GRÁFICO DE INGRESOS

  function mostrarGraficoIngresos(lista) {
    if (graficoIngresos) {
      graficoIngresos.destroy();
      graficoIngresos = null;
    }

    const resumen = obtenerIngresosPorDia(lista);

    if (resumen.etiquetas.length === 0) {
      canvasIngresos.classList.add("d-none");
      vacioIngresos.classList.remove("d-none");

      return;
    }

    canvasIngresos.classList.remove("d-none");
    vacioIngresos.classList.add("d-none");

    graficoIngresos = new Chart(canvasIngresos, {
      type: "line",

      data: {
        labels: resumen.etiquetas,

        datasets: [
          {
            label: "Ingresos",

            data: resumen.valores,

            borderColor: "#16834b",

            backgroundColor: "rgba(22, 131, 75, 0.10)",

            borderWidth: 2,

            fill: true,

            tension: 0.3,

            pointRadius: 4,

            pointHoverRadius: 6,
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
              label: function (context) {
                return "Ingresos: " + formatearMoneda(context.parsed.y);
              },
            },
          },
        },

        scales: {
          y: {
            beginAtZero: true,

            ticks: {
              callback: function (valor) {
                return "S/ " + valor;
              },
            },
          },
        },
      },
    });
  }

  // AGRUPAR POR MÉTODO DE PAGO

  function obtenerResumenMetodos(lista) {
    const resumen = {};

    lista.forEach((venta) => {
      const metodo = String(
        venta.metodoPago || "NO ESPECIFICADO",
      ).toUpperCase();

      if (!resumen[metodo]) {
        resumen[metodo] = {
          cantidad: 0,
          total: 0,
        };
      }

      resumen[metodo].cantidad++;

      resumen[metodo].total += Number(venta.total || 0);
    });

    return resumen;
  }

  // NOMBRE DE MÉTODOS DE PAGO

  function obtenerNombreMetodo(metodo) {
    const nombres = {
      EFECTIVO: "Efectivo",
      TARJETA: "Tarjeta",
      TRANSFERENCIA: "Transferencia",
    };

    return nombres[metodo] || metodo;
  }

  // MOSTRAR GRÁFICO DE MÉTODOS DE PAGO

  function mostrarGraficoMetodos(lista) {
    if (graficoMetodos) {
      graficoMetodos.destroy();
      graficoMetodos = null;
    }

    const resumen = obtenerResumenMetodos(lista);

    const metodos = Object.keys(resumen);

    if (metodos.length === 0) {
      canvasMetodos.classList.add("d-none");
      vacioMetodos.classList.remove("d-none");

      return;
    }

    canvasMetodos.classList.remove("d-none");
    vacioMetodos.classList.add("d-none");

    const cantidades = metodos.map((metodo) => resumen[metodo].cantidad);

    graficoMetodos = new Chart(canvasMetodos, {
      type: "doughnut",

      data: {
        labels: metodos.map(obtenerNombreMetodo),

        datasets: [
          {
            data: cantidades,

            backgroundColor: [
              "#16834b",
              "#4caf80",
              "#83c9a2",
              "#b7dfc8",
              "#d7eadf",
            ],

            borderWidth: 2,

            borderColor: "#ffffff",
          },
        ],
      },

      options: {
        responsive: true,

        maintainAspectRatio: false,

        cutout: "65%",

        plugins: {
          legend: {
            position: "bottom",

            labels: {
              usePointStyle: true,
              padding: 15,
            },
          },

          tooltip: {
            callbacks: {
              label: function (context) {
                return context.label + ": " + context.parsed + " ventas";
              },
            },
          },
        },
      },
    });
  }

  // MOSTRAR TABLA DE MÉTODOS DE PAGO

  function mostrarTablaMetodos(lista) {
    tablaMetodosPago.replaceChildren();

    const resumen = obtenerResumenMetodos(lista);

    const metodos = Object.keys(resumen);

    vacioTablaReportes.classList.toggle("d-none", metodos.length > 0);

    metodos.forEach((metodo) => {
      const fila = document.createElement("tr");

      const celdaMetodo = document.createElement("td");
      const celdaCantidad = document.createElement("td");
      const celdaTotal = document.createElement("td");

      celdaMetodo.textContent = obtenerNombreMetodo(metodo);

      celdaCantidad.textContent = resumen[metodo].cantidad;

      celdaCantidad.className = "text-center";

      celdaTotal.textContent = formatearMoneda(resumen[metodo].total);

      celdaTotal.className = "text-end fw-semibold";

      fila.appendChild(celdaMetodo);
      fila.appendChild(celdaCantidad);
      fila.appendChild(celdaTotal);

      tablaMetodosPago.appendChild(fila);
    });
  }

  // ACTUALIZAR TODOS LOS REPORTES

  function actualizarReportes() {
    const resultado = filtrarVentas();

    if (resultado === null) {
      return;
    }

    actualizarEstadisticas(resultado);

    mostrarGraficoIngresos(resultado);

    mostrarGraficoMetodos(resultado);

    mostrarTablaMetodos(resultado);
  }

  // EVENTOS

  btnFiltrar.addEventListener("click", function () {
    actualizarReportes();
  });

  btnActualizar.addEventListener("click", async function () {
    await cargarVentas();
  });

  btnLimpiar.addEventListener("click", function () {
    fechaDesde.value = "";
    fechaHasta.value = "";

    actualizarReportes();
  });

  // INICIALIZAR MÓDULO

  await cargarVentas();
});
