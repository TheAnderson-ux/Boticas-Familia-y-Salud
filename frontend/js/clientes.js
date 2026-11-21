document.addEventListener("DOMContentLoaded", function () {
  // CONFIGURACIÓN DE LA API

  const API_CLIENTES = `${window.API_URL || "http://localhost:8080/api"}/clientes`;

  const formulario = document.getElementById("formularioCliente");

  const modalClienteElemento = document.getElementById("modalCliente");
  const modalDetalleElemento = document.getElementById("modalDetalleCliente");
  const modalEliminarElemento = document.getElementById("modalEliminarCliente");

  const modalCliente = new bootstrap.Modal(modalClienteElemento);
  const modalDetalle = new bootstrap.Modal(modalDetalleElemento);
  const modalEliminar = new bootstrap.Modal(modalEliminarElemento);

  const tablaClientes = document.getElementById("tablaClientes");
  const plantillaFila = document.getElementById("plantillaFilaCliente");

  const buscarCliente = document.getElementById("buscarCliente");
  const clientesVacio = document.getElementById("clientesVacio");

  // Estadísticas
  const totalClientes = document.getElementById("totalClientes");
  const clientesHoy = document.getElementById("clientesHoy");
  const cantidadClientes = document.getElementById("cantidadClientes");
  const textoResultados = document.getElementById("textoResultados");

  // Campos del formulario
  const clienteId = document.getElementById("clienteId");
  const clienteDni = document.getElementById("clienteDni");
  const clienteNombre = document.getElementById("clienteNombre");
  const clienteCelular = document.getElementById("clienteCelular");
  const clienteCorreo = document.getElementById("clienteCorreo");
  const clienteDireccion = document.getElementById("clienteDireccion");

  const tituloModalCliente = document.getElementById("tituloModalCliente");
  const btnGuardarCliente = document.getElementById("btnGuardarCliente");

  // Elementos del modal de cambio de estado
  const nombreClienteEliminar = document.getElementById(
    "nombreClienteEliminar",
  );
  const btnConfirmarEliminar = document.getElementById("btnConfirmarEliminar");

  let clientes = [];
  let clientePendienteEstado = null;

  // AUTENTICACIÓN Y PETICIONES

  function obtenerToken() {
    return localStorage.getItem("token") || localStorage.getItem("jwtToken");
  }

  async function realizarPeticion(url, opciones = {}) {
    const token = obtenerToken();

    if (!token) {
      throw new Error("No se encontró el token. Inicia sesión nuevamente.");
    }

    const respuesta = await fetch(url, {
      ...opciones,
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
        ...opciones.headers,
      },
    });

    const texto = await respuesta.text();

    let datos = null;

    if (texto) {
      try {
        datos = JSON.parse(texto);
      } catch {
        datos = texto;
      }
    }

    if (!respuesta.ok) {
      if (respuesta.status === 401) {
        throw new Error("Tu sesión expiró. Inicia sesión nuevamente.");
      }

      if (respuesta.status === 403) {
        throw new Error("No tienes permisos para realizar esta operación.");
      }

      const mensaje =
        typeof datos === "string"
          ? datos
          : datos?.mensaje || datos?.message || datos?.error;

      throw new Error(mensaje || `Error HTTP ${respuesta.status}`);
    }

    return datos;
  }

  function notificar(mensaje, tipo = "success") {
    if (typeof mostrarNotificacion === "function") {
      mostrarNotificacion(mensaje, tipo);
    } else {
      alert(mensaje);
    }
  }

  // FUNCIONES GENERALES

  function obtenerFechaActual() {
    const fecha = new Date();

    return [
      fecha.getFullYear(),
      String(fecha.getMonth() + 1).padStart(2, "0"),
      String(fecha.getDate()).padStart(2, "0"),
    ].join("-");
  }

  function obtenerFechaCliente(fecha) {
    if (!fecha) return "";

    return String(fecha).substring(0, 10);
  }

  function formatearFecha(fecha) {
    if (!fecha) return "-";

    const fechaLocal = obtenerFechaCliente(fecha);
    const partes = fechaLocal.split("-");

    if (partes.length !== 3) return "-";

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
  }

  function escaparHTML(valor) {
    return String(valor ?? "").replace(/[&<>"']/g, function (caracter) {
      const caracteres = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;",
      };

      return caracteres[caracter];
    });
  }

  // CARGAR CLIENTES DESDE SQL SERVER

  async function cargarClientes() {
    tablaClientes.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-4">
                    <div class="spinner-border text-success"></div>
                    <p class="mt-2 mb-0">Cargando clientes...</p>
                </td>
            </tr>
        `;

    try {
      const respuesta = await realizarPeticion(API_CLIENTES);

      clientes = Array.isArray(respuesta) ? respuesta : [];

      actualizarVista();
    } catch (error) {
      console.error("Error al cargar clientes:", error);

      tablaClientes.replaceChildren();

      clientesVacio.classList.remove("d-none");

      totalClientes.textContent = "-";
      clientesHoy.textContent = "-";
      cantidadClientes.textContent = "Error";
      textoResultados.textContent = "No se pudieron cargar los clientes.";

      notificar(error.message, "danger");
    }
  }

  // ESTADÍSTICAS

  function actualizarEstadisticas() {
    const fechaActual = obtenerFechaActual();

    const registradosHoy = clientes.filter(function (cliente) {
      return obtenerFechaCliente(cliente.fechaRegistro) === fechaActual;
    });

    totalClientes.textContent = clientes.length;

    clientesHoy.textContent = registradosHoy.length;
  }

  // MOSTRAR CLIENTES

  function mostrarClientes(lista) {
    tablaClientes.replaceChildren();

    lista.forEach(function (cliente) {
      const fila = plantillaFila.content.cloneNode(true);

      fila.querySelector(".cliente-dni").textContent = cliente.documento || "-";

      fila.querySelector(".cliente-nombre").textContent = cliente.nombre || "-";

      fila.querySelector(".cliente-celular").textContent =
        cliente.telefono || "-";

      fila.querySelector(".cliente-correo").textContent = cliente.correo || "-";

      fila.querySelector(".cliente-fecha").textContent = formatearFecha(
        cliente.fechaRegistro,
      );

      // Ver detalle
      fila.querySelector(".btn-detalle").addEventListener("click", function () {
        mostrarDetalleCliente(cliente.idCliente);
      });

      // Editar
      fila.querySelector(".btn-editar").addEventListener("click", function () {
        editarCliente(cliente.idCliente);
      });

      // Activar / inactivar
      const botonEstado = fila.querySelector(".btn-eliminar");

      if (cliente.estado === "INACTIVO") {
        botonEstado.classList.remove("btn-outline-danger");
        botonEstado.classList.add("btn-outline-success");

        botonEstado.innerHTML = '<i class="bi bi-check-circle"></i>';
        botonEstado.title = "Activar cliente";
      } else {
        botonEstado.innerHTML = '<i class="bi bi-person-x"></i>';
        botonEstado.title = "Inactivar cliente";
      }

      botonEstado.addEventListener("click", function () {
        prepararCambioEstado(cliente.idCliente);
      });

      // Identificar clientes inactivos visualmente
      if (cliente.estado === "INACTIVO") {
        fila.querySelector("tr")?.classList.add("table-secondary");
      }

      tablaClientes.appendChild(fila);
    });

    clientesVacio.classList.toggle("d-none", lista.length > 0);

    cantidadClientes.textContent =
      lista.length + (lista.length === 1 ? " cliente" : " clientes");

    textoResultados.textContent = `Mostrando ${lista.length} de ${clientes.length} clientes`;
  }

  function actualizarVista() {
    actualizarEstadisticas();
    filtrarClientes();
  }

  // BÚSQUEDA

  function filtrarClientes() {
    const texto = buscarCliente.value.trim().toLowerCase();

    const resultado = clientes.filter(function (cliente) {
      return (
        (cliente.nombre || "").toLowerCase().includes(texto) ||
        (cliente.documento || "").toLowerCase().includes(texto) ||
        (cliente.telefono || "").toLowerCase().includes(texto)
      );
    });

    mostrarClientes(resultado);
  }

  // LIMPIAR FORMULARIO

  function limpiarFormulario() {
    formulario.reset();

    formulario.classList.remove("was-validated");

    clienteId.value = "";

    clienteDni.setCustomValidity("");
    clienteNombre.setCustomValidity("");
    clienteCelular.setCustomValidity("");
    clienteCorreo.setCustomValidity("");
  }

  // NUEVO CLIENTE

  function abrirNuevoCliente() {
    limpiarFormulario();

    tituloModalCliente.textContent = "Nuevo cliente";

    btnGuardarCliente.innerHTML =
      '<i class="bi bi-check-lg me-1"></i> Guardar cliente';

    modalCliente.show();
  }

  // EDITAR CLIENTE

  async function editarCliente(id) {
    try {
      const cliente = await realizarPeticion(`${API_CLIENTES}/${id}`);

      limpiarFormulario();

      clienteId.value = cliente.idCliente;
      clienteDni.value = cliente.documento || "";
      clienteNombre.value = cliente.nombre || "";
      clienteCelular.value = cliente.telefono || "";
      clienteCorreo.value = cliente.correo || "";
      clienteDireccion.value = cliente.direccion || "";

      tituloModalCliente.textContent = "Editar cliente";

      btnGuardarCliente.innerHTML =
        '<i class="bi bi-check-lg me-1"></i> Actualizar cliente';

      modalCliente.show();
    } catch (error) {
      console.error(error);
      notificar(error.message, "danger");
    }
  }

  // VALIDAR FORMULARIO

  function validarDatos() {
    const dni = clienteDni.value.trim();

    const celular = clienteCelular.value.trim();

    const nombre = clienteNombre.value.trim();

    const dniValido = /^\d{8}$/.test(dni);

    const celularValido = /^\d{9}$/.test(celular);

    const nombreValido = nombre.length >= 3;

    const correo = clienteCorreo.value.trim();

    const correoValido =
      correo === "" || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo);

    clienteDni.setCustomValidity(
      dniValido ? "" : "El DNI debe tener 8 dígitos.",
    );

    clienteCelular.setCustomValidity(
      celularValido ? "" : "El celular debe tener 9 dígitos.",
    );

    clienteNombre.setCustomValidity(
      nombreValido ? "" : "Ingrese un nombre válido.",
    );

    clienteCorreo.setCustomValidity(
      correoValido ? "" : "Ingrese un correo válido.",
    );

    formulario.classList.add("was-validated");

    return formulario.checkValidity();
  }

  // REGISTRAR / ACTUALIZAR CLIENTE

  async function guardarCliente(evento) {
    evento.preventDefault();

    if (!validarDatos()) return;

    const idActual = clienteId.value;

    const datosCliente = {
      documento: clienteDni.value.trim(),

      nombre: clienteNombre.value.trim(),

      telefono: clienteCelular.value.trim(),

      correo: clienteCorreo.value.trim(),

      direccion: clienteDireccion.value.trim(),
    };

    try {
      btnGuardarCliente.disabled = true;

      btnGuardarCliente.innerHTML =
        '<span class="spinner-border spinner-border-sm me-1"></span> Guardando...';

      if (idActual) {
        await realizarPeticion(`${API_CLIENTES}/${idActual}`, {
          method: "PUT",

          body: JSON.stringify(datosCliente),
        });

        notificar("Cliente actualizado correctamente.", "success");
      } else {
        await realizarPeticion(API_CLIENTES, {
          method: "POST",

          body: JSON.stringify(datosCliente),
        });

        notificar("Cliente registrado correctamente.", "success");
      }

      modalCliente.hide();

      limpiarFormulario();

      await cargarClientes();
    } catch (error) {
      console.error("Error al guardar cliente:", error);

      notificar(error.message, "danger");
    } finally {
      btnGuardarCliente.disabled = false;

      btnGuardarCliente.innerHTML = idActual
        ? '<i class="bi bi-check-lg me-1"></i> Actualizar cliente'
        : '<i class="bi bi-check-lg me-1"></i> Guardar cliente';
    }
  }

  // VER DETALLE

  async function mostrarDetalleCliente(id) {
    try {
      const cliente = await realizarPeticion(`${API_CLIENTES}/${id}`);

      document.getElementById("detalleNombre").textContent =
        cliente.nombre || "-";

      document.getElementById("detalleDni").textContent =
        "Documento: " + (cliente.documento || "No registrado");

      document.getElementById("detalleCelular").textContent =
        cliente.telefono || "No registrado";

      document.getElementById("detalleCorreo").textContent =
        cliente.correo || "No registrado";

      document.getElementById("detalleDireccion").textContent =
        cliente.direccion || "No registrada";

      document.getElementById("detalleFecha").textContent = formatearFecha(
        cliente.fechaRegistro,
      );

      modalDetalle.show();
    } catch (error) {
      console.error(error);
      notificar(error.message, "danger");
    }
  }

  // ACTIVAR / INACTIVAR CLIENTE

  function prepararCambioEstado(id) {
    const cliente = clientes.find(function (elemento) {
      return elemento.idCliente === id;
    });

    if (!cliente) return;

    clientePendienteEstado = cliente;

    const estaActivo = cliente.estado === "ACTIVO";

    nombreClienteEliminar.textContent = cliente.nombre;

    modalEliminarElemento.querySelector(".modal-title").textContent = estaActivo
      ? "Inactivar cliente"
      : "Activar cliente";

    modalEliminarElemento.querySelector(".modal-body p").textContent =
      estaActivo
        ? "¿Deseas inactivar a este cliente?"
        : "¿Deseas volver a activar a este cliente?";

    modalEliminarElemento.querySelector(".modal-body .text-muted").textContent =
      estaActivo
        ? "El cliente se conservará en la base de datos."
        : "El cliente volverá a estar habilitado.";

    btnConfirmarEliminar.textContent = estaActivo ? "Inactivar" : "Activar";

    btnConfirmarEliminar.classList.toggle("btn-danger", estaActivo);
    btnConfirmarEliminar.classList.toggle("btn-success", !estaActivo);

    modalEliminar.show();
  }

  async function confirmarCambioEstado() {
    if (!clientePendienteEstado) return;

    const cliente = clientePendienteEstado;

    const nuevoEstado = cliente.estado === "ACTIVO" ? "INACTIVO" : "ACTIVO";

    try {
      btnConfirmarEliminar.disabled = true;

      btnConfirmarEliminar.textContent = "Procesando...";

      await realizarPeticion(`${API_CLIENTES}/${cliente.idCliente}/estado`, {
        method: "PATCH",
        body: JSON.stringify({
          estado: nuevoEstado,
        }),
      });

      modalEliminar.hide();

      notificar(
        nuevoEstado === "ACTIVO"
          ? "Cliente activado correctamente."
          : "Cliente inactivado correctamente.",
        "success",
      );

      clientePendienteEstado = null;

      await cargarClientes();
    } catch (error) {
      console.error("Error al cambiar estado:", error);

      notificar(error.message, "danger");
    } finally {
      btnConfirmarEliminar.disabled = false;
    }
  }

  // EVENTOS

  document
    .getElementById("btnNuevoCliente")
    .addEventListener("click", abrirNuevoCliente);

  document
    .getElementById("btnLimpiarBusqueda")
    .addEventListener("click", function () {
      buscarCliente.value = "";

      filtrarClientes();
    });

  buscarCliente.addEventListener("input", filtrarClientes);

  formulario.addEventListener("submit", guardarCliente);

  btnConfirmarEliminar.addEventListener("click", confirmarCambioEstado);

  // VALIDACIONES EN TIEMPO REAL

  clienteDni.addEventListener("input", function () {
    this.value = this.value.replace(/\D/g, "").slice(0, 8);

    this.setCustomValidity("");
  });

  clienteCelular.addEventListener("input", function () {
    this.value = this.value.replace(/\D/g, "").slice(0, 9);

    this.setCustomValidity("");
  });

  clienteNombre.addEventListener("input", function () {
    this.setCustomValidity("");
  });

  clienteCorreo.addEventListener("input", function () {
    this.setCustomValidity("");
  });

  // Limpiar formulario al cerrar modal
  modalClienteElemento.addEventListener("hidden.bs.modal", limpiarFormulario);

  modalEliminarElemento.addEventListener("hidden.bs.modal", function () {
    clientePendienteEstado = null;

    modalEliminarElemento.querySelector(".modal-title").textContent =
      "Eliminar cliente";

    btnConfirmarEliminar.textContent = "Eliminar";
    btnConfirmarEliminar.classList.remove("btn-success");
    btnConfirmarEliminar.classList.add("btn-danger");
  });

  // INICIALIZAR MÓDULO

  cargarClientes();
});
