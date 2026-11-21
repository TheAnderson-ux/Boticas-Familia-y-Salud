document.addEventListener("DOMContentLoaded", function () {
  // 1. CONFIGURACIÓN Y ELEMENTOS

  const API_BASE = (() => {
    if (typeof API_URL !== "undefined") {
      return API_URL.replace(/\/$/, "");
    }
    return "http://localhost:8080/api";
  })();

  const tablaHistorial = document.getElementById("tablaHistorialVentas");
  const plantillaFila = document.getElementById("plantillaFilaVenta");

  const buscarVenta = document.getElementById("buscarVenta");
  const fechaDesde = document.getElementById("fechaDesde");
  const fechaHasta = document.getElementById("fechaHasta");
  const filtroMetodoPago = document.getElementById("filtroMetodoPago");
  const filtroEstado = document.getElementById("filtroEstado");
  const btnLimpiarFiltros = document.getElementById("btnLimpiarFiltros");

  let ventas = [];
  let cargando = false;

  // 2. PETICIONES A SPRING BOOT

  async function realizarPeticionAPI(endpoint) {
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
      throw new Error(
        "Tu sesión no está autorizada. Inicia sesión nuevamente.",
      );
    }

    if (!respuesta.ok) {
      const mensaje = await respuesta.text();
      throw new Error(mensaje || `Error HTTP ${respuesta.status}`);
    }

    return respuesta.json();
  }

  // 3. UTILIDADES

  function formatoMoneda(valor) {
    return "S/ " + Number(valor || 0).toFixed(2);
  }

  function formatoFecha(fecha) {
    if (!fecha) return "-";

    return new Date(fecha).toLocaleString("es-PE", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  function normalizar(valor) {
    return String(valor || "")
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .toUpperCase()
      .trim();
  }

  function textoMetodoPago(metodo) {
    const metodos = {
      EFECTIVO: "Efectivo",
      TARJETA: "Tarjeta",
      TRANSFERENCIA: "Transferencia",
    };

    return metodos[metodo] || metodo || "-";
  }

  function textoEstado(estado) {
    const estados = {
      COMPLETADA: "Completada",
      ANULADA: "Anulada",
    };

    return estados[estado] || estado || "-";
  }

  function fechaParaFiltro(fecha) {
    if (!fecha) return "";

    const f = new Date(fecha);

    const anio = f.getFullYear();
    const mes = String(f.getMonth() + 1).padStart(2, "0");
    const dia = String(f.getDate()).padStart(2, "0");

    return `${anio}-${mes}-${dia}`;
  }

  function mostrarError(mensaje) {
    const fila = document.createElement("tr");
    const celda = document.createElement("td");

    celda.colSpan = 7;
    celda.className = "text-center text-danger py-5";
    celda.textContent = mensaje;

    fila.appendChild(celda);
    tablaHistorial.replaceChildren(fila);
  }

  // 4. CARGAR VENTAS DESDE LA BASE DE DATOS

  async function cargarVentas() {
    if (cargando) return;

    cargando = true;

    tablaHistorial.innerHTML = `
            <tr>
                <td colspan="7" class="text-center py-5">
                    <div class="spinner-border text-success"></div>
                    <p class="mt-2 mb-0">Cargando historial...</p>
                </td>
            </tr>
        `;

    try {
      const respuesta = await realizarPeticionAPI("/ventas");

      ventas = Array.isArray(respuesta) ? respuesta : [];

      actualizarEstadisticas();
      filtrarVentas();
    } catch (error) {
      console.error("Error al cargar historial:", error);
      mostrarError(error.message || "No se pudo cargar el historial.");
    } finally {
      cargando = false;
    }
  }

  // 5. MOSTRAR VENTAS EN LA TABLA

  function mostrarVentas(listaVentas) {
    tablaHistorial.replaceChildren();

    if (listaVentas.length === 0) {
      const fila = document.createElement("tr");
      const celda = document.createElement("td");

      celda.colSpan = 7;
      celda.className = "text-center text-muted py-5";

      const icono = document.createElement("i");
      icono.className = "bi bi-search fs-1 d-block mb-2";

      const mensaje = document.createElement("strong");
      mensaje.textContent = "No se encontraron ventas";

      const descripcion = document.createElement("p");
      descripcion.className = "mb-0 small";
      descripcion.textContent = "Intenta cambiar los filtros de búsqueda.";

      celda.append(icono, mensaje, descripcion);
      fila.appendChild(celda);
      tablaHistorial.appendChild(fila);

      actualizarCantidadVentas(0);
      return;
    }

    listaVentas.forEach((venta) => {
      const fila = plantillaFila.content.firstElementChild.cloneNode(true);

      fila.querySelector(".numero-venta").textContent =
        venta.numeroVenta || "-";

      fila.querySelector(".fecha-venta").textContent = formatoFecha(
        venta.fechaVenta,
      );

      fila.querySelector(".cliente-venta").textContent =
        venta.cliente || "Cliente genérico";

      fila.querySelector(".metodo-venta").textContent = textoMetodoPago(
        venta.metodoPago,
      );

      fila.querySelector(".total-venta").textContent = formatoMoneda(
        venta.total,
      );

      const estado = fila.querySelector(".estado-venta");

      estado.textContent = textoEstado(venta.estado);

      estado.classList.remove(
        "bg-success-subtle",
        "text-success",
        "bg-danger-subtle",
        "text-danger",
      );

      if (normalizar(venta.estado) === "COMPLETADA") {
        estado.classList.add("bg-success-subtle", "text-success");
      } else {
        estado.classList.add("bg-danger-subtle", "text-danger");
      }

      fila.querySelector(".btn-ver-detalle").addEventListener("click", () => {
        mostrarDetalleVenta(venta);
      });

      tablaHistorial.appendChild(fila);
    });

    actualizarCantidadVentas(listaVentas.length);
  }

  // 6. ESTADÍSTICAS

  function actualizarEstadisticas() {
    const completadas = ventas.filter(
      (venta) => normalizar(venta.estado) === "COMPLETADA",
    );

    const ingresos = completadas.reduce(
      (suma, venta) => suma + Number(venta.total || 0),
      0,
    );

    const hoy = fechaParaFiltro(new Date());

    const ventasHoy = completadas.filter(
      (venta) => fechaParaFiltro(venta.fechaVenta) === hoy,
    );

    document.getElementById("totalVentasRegistradas").textContent =
      completadas.length;

    document.getElementById("totalIngresos").textContent =
      formatoMoneda(ingresos);

    document.getElementById("ventasDelDia").textContent = ventasHoy.length;
  }

  // 7. CANTIDAD DE RESULTADOS

  function actualizarCantidadVentas(cantidad) {
    document.getElementById("cantidadVentas").textContent =
      cantidad + (cantidad === 1 ? " venta" : " ventas");

    document.getElementById("textoResultados").textContent =
      "Mostrando " + cantidad + (cantidad === 1 ? " venta" : " ventas");
  }

  // 8. FILTRAR VENTAS

  function filtrarVentas() {
    const busqueda = normalizar(buscarVenta.value);
    const desde = fechaDesde.value;
    const hasta = fechaHasta.value;
    const metodo = normalizar(filtroMetodoPago.value);
    const estado = normalizar(filtroEstado.value);

    const resultado = ventas.filter((venta) => {
      const numero = normalizar(venta.numeroVenta);
      const cliente = normalizar(venta.cliente);
      const fecha = fechaParaFiltro(venta.fechaVenta);

      const coincideBusqueda =
        numero.includes(busqueda) || cliente.includes(busqueda);

      const coincideDesde = !desde || fecha >= desde;
      const coincideHasta = !hasta || fecha <= hasta;

      const coincideMetodo = !metodo || normalizar(venta.metodoPago) === metodo;

      const coincideEstado = !estado || normalizar(venta.estado) === estado;

      return (
        coincideBusqueda &&
        coincideDesde &&
        coincideHasta &&
        coincideMetodo &&
        coincideEstado
      );
    });

    mostrarVentas(resultado);
  }

  // 9. CONSULTAR DETALLE DE UNA VENTA

  async function mostrarDetalleVenta(venta) {
    const tabla = document.getElementById("tablaDetalleProductos");

    tabla.innerHTML = `
            <tr>
                <td colspan="4" class="text-center py-3">
                    Cargando productos...
                </td>
            </tr>
        `;

    try {
      // El listado no incluye items.
      // Por eso consultamos el endpoint individual.

      const detalle = await realizarPeticionAPI(`/ventas/${venta.idVenta}`);

      document.getElementById("detalleNumeroVenta").textContent =
        detalle.numeroVenta || "-";

      document.getElementById("detalleFechaVenta").textContent = formatoFecha(
        detalle.fechaVenta,
      );

      document.getElementById("detalleClienteVenta").textContent =
        detalle.cliente || "Cliente genérico";

      document.getElementById("detalleMetodoPago").textContent =
        textoMetodoPago(detalle.metodoPago);

      document.getElementById("detalleSubtotal").textContent = formatoMoneda(
        detalle.subtotal,
      );

      document.getElementById("detalleDescuento").textContent =
        formatoMoneda(0);

      document.getElementById("detalleTotal").textContent = formatoMoneda(
        detalle.total,
      );

      tabla.replaceChildren();

      (detalle.items || []).forEach((producto) => {
        const fila = document.createElement("tr");

        const datos = [
          producto.nombre,
          formatoMoneda(producto.precioUnitario),
          producto.cantidad,
          formatoMoneda(producto.subtotal),
        ];

        datos.forEach((dato) => {
          const celda = document.createElement("td");
          celda.textContent = dato;
          fila.appendChild(celda);
        });

        tabla.appendChild(fila);
      });

      if (!detalle.items || detalle.items.length === 0) {
        const fila = document.createElement("tr");
        const celda = document.createElement("td");

        celda.colSpan = 4;
        celda.className = "text-center text-muted";
        celda.textContent = "Esta venta no tiene productos registrados.";

        fila.appendChild(celda);
        tabla.appendChild(fila);
      }

      const modal = bootstrap.Modal.getOrCreateInstance(
        document.getElementById("modalDetalleVenta"),
      );

      modal.show();
    } catch (error) {
      console.error("Error al consultar detalle:", error);

      tabla.innerHTML = `
                <tr>
                    <td colspan="4" class="text-center text-danger">
                        No se pudo cargar el detalle de la venta.
                    </td>
                </tr>
            `;

      alert(error.message || "Error al consultar el detalle.");
    }
  }

  // 10. EVENTOS DE FILTROS

  buscarVenta.addEventListener("input", filtrarVentas);
  fechaDesde.addEventListener("change", filtrarVentas);
  fechaHasta.addEventListener("change", filtrarVentas);
  filtroMetodoPago.addEventListener("change", filtrarVentas);
  filtroEstado.addEventListener("change", filtrarVentas);

  // 11. LIMPIAR FILTROS

  btnLimpiarFiltros.addEventListener("click", function () {
    buscarVenta.value = "";
    fechaDesde.value = "";
    fechaHasta.value = "";
    filtroMetodoPago.value = "";
    filtroEstado.value = "";

    filtrarVentas();
  });

  // 12. INICIAR

  cargarVentas();
});
