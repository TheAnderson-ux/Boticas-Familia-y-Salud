let productosDisponibles = [];
let clientesDisponibles = [];
let carrito = [];
let ventaProcesando = false;

// INICIALIZACIÓN

document.addEventListener("DOMContentLoaded", async () => {
  document
    .getElementById("btnImprimirComprobante")
    ?.addEventListener("click", () => window.print());

  document
    .getElementById("btnRegistrarVenta")
    ?.addEventListener("click", prepararConfirmacionVenta);

  document
    .getElementById("btnConfirmarVenta")
    ?.addEventListener("click", confirmarVenta);

  document
    .getElementById("buscarProductoVenta")
    ?.addEventListener("input", buscarProductos);

  const campoDescuento = document.getElementById("porcentajeDescuento");

  if (campoDescuento) {
    campoDescuento.value = 0;
    campoDescuento.disabled = true;
    campoDescuento.title =
      "Los descuentos todavía no están habilitados en el backend";
  }

  document
    .getElementById("montoRecibido")
    ?.addEventListener("input", calcularVuelto);

  document.querySelectorAll(".btn-metodo-pago").forEach((boton) => {
    boton.addEventListener("click", () => {
      document
        .querySelectorAll(".btn-metodo-pago")
        .forEach((btn) => btn.classList.remove("activo"));

      boton.classList.add("activo");

      document.getElementById("metodoPago").value = boton.dataset.metodo;

      calcularVuelto();
    });
  });

  document
    .getElementById("btnCancelarVenta")
    ?.addEventListener("click", cancelarVenta);

  mostrarCarrito();

  // CARGAR CLIENTES Y PRODUCTOS
  await cargarClientes();
  await cargarProductos();

  actualizarResumenVenta();
});

// CARGAR CLIENTES DESDE SQL SERVER

async function cargarClientes() {
  const selector = document.getElementById("clienteVenta");

  if (!selector) return;

  selector.disabled = true;

  selector.innerHTML = `
        <option value="general">Cliente genérico</option>
        <option value="">Cargando clientes...</option>
    `;

  try {
    const respuesta = await realizarPeticion(
      `${API_URL}/clientes?estado=ACTIVO`,
    );

    const datos = await respuesta.json();

    if (!respuesta.ok) {
      throw new Error(
        datos.mensaje || datos.message || "No se pudieron cargar los clientes.",
      );
    }

    const lista = Array.isArray(datos)
      ? datos
      : datos.content || datos.data || datos.clientes || [];

    clientesDisponibles = lista.filter(
      (cliente) => String(cliente.estado || "").toUpperCase() === "ACTIVO",
    );

    selector.replaceChildren();

    const opcionGeneral = document.createElement("option");
    opcionGeneral.value = "general";
    opcionGeneral.textContent = "Cliente genérico";

    selector.appendChild(opcionGeneral);

    clientesDisponibles.forEach((cliente) => {
      const opcion = document.createElement("option");

      opcion.value = String(cliente.idCliente);
      opcion.dataset.nombre = cliente.nombre || "";

      opcion.textContent = `${cliente.nombre || "Cliente sin nombre"} - ${cliente.documento || "Sin documento"}`;

      selector.appendChild(opcion);
    });

    selector.value = "general";

    console.log("Clientes activos cargados:", clientesDisponibles);

    mostrarNotificacion(
      `${clientesDisponibles.length} clientes disponibles.`,
      "success",
    );
  } catch (error) {
    console.error("Error al cargar clientes:", error);

    selector.replaceChildren();

    const opcionGeneral = document.createElement("option");
    opcionGeneral.value = "general";
    opcionGeneral.textContent = "Cliente genérico";

    selector.appendChild(opcionGeneral);

    mostrarNotificacion(error.message, "error");
  } finally {
    selector.disabled = false;
  }
}

// CARGAR PRODUCTOS DESDE EL BACKEND

async function cargarProductos() {
  const contenedor = document.getElementById("resultadosProductos");

  if (contenedor) {
    contenedor.innerHTML = `
            <p class="text-muted text-center py-3">
                Cargando productos...
            </p>
        `;
  }

  try {
    const respuesta = await realizarPeticion(`${API_URL}/productos`);

    const datos = await respuesta.json();

    if (!respuesta.ok) {
      throw new Error(datos.mensaje || "No se pudieron cargar los productos.");
    }

    const lista = Array.isArray(datos)
      ? datos
      : datos.content || datos.data || datos.productos || [];

    productosDisponibles = lista
      .map((producto) => {
        const id = Number(
          producto.idProducto ?? producto.id ?? producto.id_producto,
        );

        return {
          id: id,
          codigo: producto.codigo || "",
          nombre: producto.nombre || "",
          categoria: producto.categoria || "",
          precio: Number(
            producto.precio ??
              producto.precioVenta ??
              producto.precioUnitario ??
              0,
          ),
          stock: Number(producto.stock ?? producto.stockActual ?? 0),
          estado: String(producto.estado || "").toUpperCase(),
        };
      })
      .filter(
        (producto) =>
          Number.isInteger(producto.id) &&
          producto.id > 0 &&
          producto.nombre &&
          producto.estado === "ACTIVO",
      );

    console.log("Productos reales cargados:", productosDisponibles);

    if (contenedor) {
      contenedor.innerHTML = `
                <div class="mensaje-inicial">
                    <i class="bi bi-box-seam"></i>
                    <p>Busca un producto para agregarlo a la venta.</p>
                    <small class="text-muted">
                        ${productosDisponibles.length} productos disponibles
                    </small>
                </div>
            `;
    }

    mostrarNotificacion(
      `${productosDisponibles.length} productos cargados correctamente.`,
      "success",
    );
  } catch (error) {
    console.error("Error al cargar productos:", error);

    if (contenedor) {
      contenedor.innerHTML = `
                <p class="text-danger text-center py-3">
                    No se pudieron cargar los productos.
                </p>
            `;
    }

    mostrarNotificacion(error.message, "error");
  }
}

// BUSCAR PRODUCTOS

function buscarProductos() {
  const texto = document
    .getElementById("buscarProductoVenta")
    .value.trim()
    .toLowerCase();

  const resultados = productosDisponibles.filter(
    (producto) =>
      producto.nombre.toLowerCase().includes(texto) ||
      producto.codigo.toLowerCase().includes(texto),
  );

  mostrarResultados(resultados);
}

// MOSTRAR RESULTADOS

function mostrarResultados(productos) {
  const contenedor = document.getElementById("resultadosProductos");
  const plantilla = document.getElementById("plantillaResultadoProducto");

  if (!contenedor || !plantilla) return;

  contenedor.replaceChildren();

  if (productos.length === 0) {
    const mensaje = document.createElement("p");
    mensaje.className = "text-muted text-center py-3";
    mensaje.textContent = "No se encontraron productos.";

    contenedor.appendChild(mensaje);
    return;
  }

  productos.forEach((producto) => {
    const clon = plantilla.content.cloneNode(true);

    clon.querySelector(".nombre-producto").textContent = producto.nombre;

    clon.querySelector(".codigo-producto").textContent =
      "Código: " + producto.codigo;

    clon.querySelector(".stock-producto").textContent =
      "Stock: " + producto.stock;

    clon.querySelector(".precio-producto").textContent =
      "S/ " + producto.precio.toFixed(2);

    const botonAgregar = clon.querySelector(".btn-agregar-producto");

    if (producto.stock <= 0) {
      botonAgregar.disabled = true;
      botonAgregar.textContent = "Sin stock";
    } else {
      botonAgregar.addEventListener("click", () => {
        agregarAlCarrito(producto.id);
      });
    }

    contenedor.appendChild(clon);
  });
}

// AGREGAR PRODUCTOS AL CARRITO

function agregarAlCarrito(idProducto) {
  const producto = productosDisponibles.find(
    (p) => p.id === Number(idProducto),
  );

  if (!producto) return;

  if (producto.stock <= 0) {
    mostrarNotificacion("Este producto no tiene stock disponible.", "warning");
    return;
  }

  const productoExistente = carrito.find((p) => p.id === producto.id);

  if (productoExistente) {
    if (productoExistente.cantidad >= producto.stock) {
      mostrarNotificacion("No hay más unidades disponibles.", "warning");
      return;
    }

    productoExistente.cantidad++;
  } else {
    carrito.push({
      ...producto,
      cantidad: 1,
    });
  }

  mostrarCarrito();
  actualizarResumenVenta();
}

// MOSTRAR CARRITO

function mostrarCarrito() {
  const tabla = document.getElementById("tablaCarrito");
  const plantilla = document.getElementById("plantillaProductoCarrito");

  if (!tabla || !plantilla) return;

  tabla.replaceChildren();

  if (carrito.length === 0) {
    const fila = document.createElement("tr");
    const celda = document.createElement("td");

    celda.colSpan = 5;
    celda.className = "text-center text-muted py-4";
    celda.textContent = "No hay productos agregados.";

    fila.appendChild(celda);
    tabla.appendChild(fila);
  }

  carrito.forEach((producto) => {
    const clon = plantilla.content.cloneNode(true);

    clon.querySelector(".nombre-producto").textContent = producto.nombre;

    clon.querySelector(".codigo-producto").textContent = producto.codigo;

    clon.querySelector(".precio-producto").textContent =
      "S/ " + producto.precio.toFixed(2);

    const campoCantidad = clon.querySelector(".cantidad-producto");

    campoCantidad.value = producto.cantidad;
    campoCantidad.max = producto.stock;
    campoCantidad.min = 1;

    clon.querySelector(".subtotal-producto").textContent =
      "S/ " + (producto.precio * producto.cantidad).toFixed(2);

    campoCantidad.addEventListener("change", () => {
      let cantidad = parseInt(campoCantidad.value) || 1;

      cantidad = Math.max(1, Math.min(producto.stock, cantidad));

      producto.cantidad = cantidad;

      mostrarCarrito();
      actualizarResumenVenta();
    });

    clon.querySelector(".btn-eliminar").addEventListener("click", () => {
      carrito = carrito.filter((p) => p.id !== producto.id);

      mostrarCarrito();
      actualizarResumenVenta();
    });

    tabla.appendChild(clon);
  });

  const cantidadTotal = carrito.reduce(
    (total, producto) => total + producto.cantidad,
    0,
  );

  const contador = document.getElementById("cantidadProductos");

  if (contador) {
    contador.textContent =
      cantidadTotal + (cantidadTotal === 1 ? " producto" : " productos");
  }
}

// ACTUALIZAR RESUMEN

function actualizarResumenVenta() {
  const subtotal = carrito.reduce(
    (total, producto) => total + producto.precio * producto.cantidad,
    0,
  );

  document.getElementById("subtotalVenta").textContent =
    "S/ " + subtotal.toFixed(2);

  calcularTotalVenta();
}

// CALCULAR TOTAL

function calcularTotalVenta() {
  const subtotal = carrito.reduce(
    (total, producto) => total + producto.precio * producto.cantidad,
    0,
  );

  const descuento = 0;
  const total = subtotal - descuento;

  const campoDescuento = document.getElementById("porcentajeDescuento");

  if (campoDescuento) {
    campoDescuento.value = 0;
  }

  document.getElementById("montoDescuento").textContent =
    "Descuento aplicado: S/ 0.00";

  document.getElementById("totalVenta").textContent = "S/ " + total.toFixed(2);

  calcularVuelto();
}

// CALCULAR VUELTO

function calcularVuelto() {
  const metodoPago = document.getElementById("metodoPago")?.value || "Efectivo";

  const total = carrito.reduce(
    (suma, producto) => suma + producto.precio * producto.cantidad,
    0,
  );

  const campoMonto = document.getElementById("montoRecibido");
  const campoVuelto = document.getElementById("vueltoVenta");

  if (!campoMonto || !campoVuelto) return;

  let vuelto = 0;

  if (metodoPago === "Efectivo") {
    campoMonto.disabled = false;
    campoMonto.placeholder = "0.00";

    const recibido = Number(campoMonto.value) || 0;
    vuelto = Math.max(0, recibido - total);
  } else {
    campoMonto.value = total.toFixed(2);
    campoMonto.disabled = true;
  }

  campoVuelto.textContent = "S/ " + vuelto.toFixed(2);

  document.getElementById("resumenMetodoPago").textContent = metodoPago;

  document.getElementById("resumenMontoRecibido").textContent =
    "S/ " + Number(campoMonto.value || 0).toFixed(2);

  document.getElementById("resumenVuelto").textContent =
    "S/ " + vuelto.toFixed(2);
}

// PREPARAR CONFIRMACIÓN

function prepararConfirmacionVenta() {
  if (carrito.length === 0) {
    mostrarNotificacion(
      "Agrega al menos un producto antes de registrar la venta.",
      "warning",
    );

    return;
  }

  const total = carrito.reduce(
    (suma, producto) => suma + producto.precio * producto.cantidad,
    0,
  );

  const metodoPago = document.getElementById("metodoPago").value;

  const montoRecibido =
    Number(document.getElementById("montoRecibido").value) || 0;

  if (!metodoPago) {
    mostrarNotificacion("Selecciona un método de pago.", "warning");
    return;
  }

  if (metodoPago === "Efectivo" && montoRecibido < total) {
    mostrarNotificacion("El monto recibido no es suficiente.", "error");

    return;
  }

  const cantidad = carrito.reduce(
    (acumulado, producto) => acumulado + producto.cantidad,
    0,
  );

  document.getElementById("confirmarCantidad").textContent =
    cantidad + (cantidad === 1 ? " producto" : " productos");

  document.getElementById("confirmarMetodo").textContent = metodoPago;

  document.getElementById("confirmarTotal").textContent =
    "S/ " + total.toFixed(2);

  const modal = bootstrap.Modal.getOrCreateInstance(
    document.getElementById("modalConfirmarVenta"),
  );

  modal.show();
}

// REGISTRAR VENTA EN EL BACKEND

async function confirmarVenta() {
  if (ventaProcesando) return;

  const usuario = obtenerUsuarioActual();

  if (!usuario || !usuario.idUsuario) {
    mostrarNotificacion(
      "No se pudo identificar al usuario. Cierra sesión e ingresa nuevamente.",
      "error",
    );

    return;
  }

  if (carrito.length === 0) {
    mostrarNotificacion("El carrito está vacío.", "warning");
    return;
  }

  const metodoPagoUI = document.getElementById("metodoPago").value;

  const metodosPago = {
    Efectivo: "EFECTIVO",
    Tarjeta: "TARJETA",
    Transferencia: "TRANSFERENCIA",
  };

  const metodoPagoBackend = metodosPago[metodoPagoUI];

  if (!metodoPagoBackend) {
    mostrarNotificacion(
      "El método de pago seleccionado no está configurado.",
      "error",
    );

    return;
  }

  const montoRecibido =
    Number(document.getElementById("montoRecibido").value) || 0;

  const total = carrito.reduce(
    (suma, producto) => suma + producto.precio * producto.cantidad,
    0,
  );

  if (metodoPagoUI === "Efectivo" && montoRecibido < total) {
    mostrarNotificacion("El monto recibido no es suficiente.", "error");

    return;
  }

  // CLIENTE SELECCIONADO
  // El backend recibe el nombre del cliente como texto.

  const clienteSelect = document.getElementById("clienteVenta");

  let cliente = null;

  if (clienteSelect && clienteSelect.value !== "general") {
    const opcionSeleccionada =
      clienteSelect.options[clienteSelect.selectedIndex];

    cliente = opcionSeleccionada?.dataset.nombre || null;
  }

  const solicitud = {
    idUsuario: Number(usuario.idUsuario),

    cliente: cliente,

    metodoPago: metodoPagoBackend,

    montoRecibido: metodoPagoUI === "Efectivo" ? montoRecibido : total,

    aplicarIgv: false,

    porcentajeIgv: 18,

    items: carrito.map((producto) => ({
      idProducto: producto.id,
      cantidad: producto.cantidad,
    })),
  };

  try {
    ventaProcesando = true;

    const botonConfirmar = document.getElementById("btnConfirmarVenta");

    if (botonConfirmar) {
      botonConfirmar.disabled = true;
      botonConfirmar.textContent = "Registrando...";
    }

    console.log("Solicitud de venta:", solicitud);

    const respuesta = await realizarPeticion(`${API_URL}/ventas`, {
      method: "POST",
      body: JSON.stringify(solicitud),
    });

    const datos = await respuesta.json();

    if (!respuesta.ok) {
      throw new Error(
        datos.mensaje || datos.error || "No se pudo registrar la venta.",
      );
    }

    console.log("Venta registrada:", datos);

    mostrarComprobante(datos);

    const elementoModal = document.getElementById("modalConfirmarVenta");

    const modalConfirmacion =
      bootstrap.Modal.getOrCreateInstance(elementoModal);

    elementoModal.addEventListener(
      "hidden.bs.modal",
      function abrirComprobante() {
        const modalComprobante = bootstrap.Modal.getOrCreateInstance(
          document.getElementById("modalComprobanteVenta"),
        );

        modalComprobante.show();
      },
      { once: true },
    );

    modalConfirmacion.hide();

    carrito = [];

    document.getElementById("buscarProductoVenta").value = "";
    document.getElementById("montoRecibido").value = "";

    mostrarCarrito();
    actualizarResumenVenta();

    await cargarProductos();

    mostrarNotificacion(
      "Venta registrada correctamente: " + (datos.numeroVenta || ""),
      "success",
    );
  } catch (error) {
    console.error("Error al registrar venta:", error);

    mostrarNotificacion(error.message, "error");
  } finally {
    ventaProcesando = false;

    const botonConfirmar = document.getElementById("btnConfirmarVenta");

    if (botonConfirmar) {
      botonConfirmar.disabled = false;
      botonConfirmar.textContent = "Confirmar venta";
    }
  }
}

// MOSTRAR COMPROBANTE

function mostrarComprobante(venta) {
  const fecha = venta.fechaVenta ? new Date(venta.fechaVenta) : new Date();

  const numeroVenta = venta.numeroVenta || venta.idVenta || "-";

  const subtotal = Number(venta.subtotal || 0);
  const descuento = 0;
  const total = Number(venta.total || 0);
  const recibido = Number(venta.montoRecibido || 0);
  const vuelto = Number(venta.vuelto || 0);

  document.getElementById("comprobanteNumero").textContent = numeroVenta;

  document.getElementById("comprobanteFecha").textContent =
    fecha.toLocaleString("es-PE");

  document.getElementById("comprobanteCliente").textContent =
    venta.cliente || "Cliente general";

  document.getElementById("comprobanteSubtotal").textContent =
    "S/ " + subtotal.toFixed(2);

  document.getElementById("comprobanteDescuento").textContent =
    "S/ " + descuento.toFixed(2);

  document.getElementById("comprobanteTotal").textContent =
    "S/ " + total.toFixed(2);

  document.getElementById("comprobanteMetodo").textContent =
    venta.metodoPago || "-";

  document.getElementById("comprobanteRecibido").textContent =
    "S/ " + recibido.toFixed(2);

  document.getElementById("comprobanteVuelto").textContent =
    "S/ " + vuelto.toFixed(2);

  const tabla = document.getElementById("tablaComprobante");

  tabla.replaceChildren();

  const items = venta.items || venta.detalles || [];

  items.forEach((item) => {
    const fila = document.createElement("tr");

    const nombre = item.nombreProducto || item.producto || item.nombre || "-";

    const precio = Number(item.precioUnitario || item.precio || 0);

    const cantidad = Number(item.cantidad || 0);

    const importe = Number(item.subtotal || precio * cantidad);

    const valores = [
      nombre,
      "S/ " + precio.toFixed(2),
      cantidad,
      "S/ " + importe.toFixed(2),
    ];

    valores.forEach((valor) => {
      const celda = document.createElement("td");
      celda.textContent = valor;

      fila.appendChild(celda);
    });

    tabla.appendChild(fila);
  });
}

// CANCELAR VENTA

function cancelarVenta() {
  carrito = [];

  document.getElementById("buscarProductoVenta").value = "";
  document.getElementById("montoRecibido").value = "";

  const campoDescuento = document.getElementById("porcentajeDescuento");

  if (campoDescuento) {
    campoDescuento.value = 0;
  }

  const resultados = document.getElementById("resultadosProductos");

  resultados.replaceChildren();

  const mensaje = document.createElement("div");
  mensaje.className = "mensaje-inicial";

  const icono = document.createElement("i");
  icono.className = "bi bi-box-seam";

  const texto = document.createElement("p");
  texto.textContent = "Busca un producto para agregarlo a la venta.";

  mensaje.append(icono, texto);
  resultados.appendChild(mensaje);

  mostrarCarrito();
  actualizarResumenVenta();
}

// NOTIFICACIONES DEL MÓDULO

function mostrarNotificacion(mensaje, tipo = "success") {
  const contenedor = document.getElementById("contenedorNotificaciones");

  const plantilla = document.getElementById("plantillaNotificacion");

  if (!contenedor || !plantilla) {
    if (typeof window.mostrarNotificacionSistema === "function") {
      window.mostrarNotificacionSistema(mensaje, tipo);
    } else {
      console.log(mensaje);
    }

    return;
  }

  const notificacion = plantilla.content.firstElementChild.cloneNode(true);

  const colores = {
    success: "text-bg-success",
    error: "text-bg-danger",
    warning: "text-bg-warning",
    info: "text-bg-primary",
  };

  notificacion.classList.add(colores[tipo] || colores.info);

  notificacion.querySelector(".toast-body").textContent = mensaje;

  contenedor.appendChild(notificacion);

  const toast = new bootstrap.Toast(notificacion, {
    delay: 3500,
  });

  notificacion.addEventListener("hidden.bs.toast", () => {
    notificacion.remove();
  });

  toast.show();
}
