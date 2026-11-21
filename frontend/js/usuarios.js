document.addEventListener("DOMContentLoaded", () => {
  // CONFIGURACIÓN

  const API_USUARIOS = `${API_URL}/usuarios`;

  let usuarios = [];
  let usuarioPendienteCambioEstado = null;

  const formulario = document.getElementById("formularioUsuario");

  const modalUsuario = new bootstrap.Modal(
    document.getElementById("modalUsuario"),
  );

  const modalDetalle = new bootstrap.Modal(
    document.getElementById("modalDetalleUsuario"),
  );

  const modalEstado = new bootstrap.Modal(
    document.getElementById("modalEliminarUsuario"),
  );

  const tablaUsuarios = document.getElementById("tablaUsuarios");
  const plantillaFila = document.getElementById("plantillaFilaUsuario");

  const buscarUsuario = document.getElementById("buscarUsuario");
  const filtroRol = document.getElementById("filtroRol");
  const filtroEstado = document.getElementById("filtroEstado");

  const totalUsuarios = document.getElementById("totalUsuarios");
  const usuariosActivos = document.getElementById("usuariosActivos");
  const totalAdministradores = document.getElementById("totalAdministradores");

  const cantidadUsuarios = document.getElementById("cantidadUsuarios");
  const textoResultados = document.getElementById("textoResultadosUsuarios");
  const usuariosVacio = document.getElementById("usuariosVacio");

  const usuarioId = document.getElementById("usuarioId");
  const usuarioNombre = document.getElementById("usuarioNombre");
  const usuarioNombreUsuario = document.getElementById("usuarioNombreUsuario");
  const usuarioCorreo = document.getElementById("usuarioCorreo");
  const usuarioRol = document.getElementById("usuarioRol");
  const usuarioEstado = document.getElementById("usuarioEstado");
  const usuarioContrasena = document.getElementById("usuarioContrasena");

  const tituloModal = document.getElementById("tituloModalUsuario");
  const btnGuardar = document.getElementById("btnGuardarUsuario");
  const ayudaContrasena = document.getElementById("ayudaContrasena");

  const btnNuevoUsuario = document.getElementById("btnNuevoUsuario");
  const btnLimpiarFiltros = document.getElementById(
    "btnLimpiarFiltrosUsuarios",
  );
  const btnConfirmarEstado = document.getElementById(
    "btnConfirmarEliminarUsuario",
  );

  // FUNCIONES GENERALES

  function notificar(mensaje, tipo = "info") {
    if (typeof mostrarNotificacion === "function") {
      mostrarNotificacion(mensaje, tipo);
    } else {
      alert(mensaje);
    }
  }

  function obtenerTextoRol(rol) {
    const roles = {
      ADMINISTRADOR: "Administrador",
      VENDEDOR: "Vendedor",
      ALMACENERO: "Almacenero",
    };

    return roles[rol] || rol || "-";
  }

  function obtenerTextoEstado(estado) {
    const estados = {
      ACTIVO: "Activo",
      INACTIVO: "Inactivo",
    };

    return estados[estado] || estado || "-";
  }

  function formatearFecha(fecha) {
    if (!fecha) return "-";

    const fechaConvertida = new Date(fecha);

    if (Number.isNaN(fechaConvertida.getTime())) {
      return "-";
    }

    return fechaConvertida.toLocaleString("es-PE", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  async function procesarRespuesta(respuesta) {
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
      let mensaje = "Ocurrió un error al procesar la solicitud.";

      if (datos && typeof datos === "object") {
        mensaje = datos.mensaje || datos.message || datos.error || mensaje;
      } else if (typeof datos === "string" && datos.trim()) {
        mensaje = datos;
      }

      throw new Error(mensaje);
    }

    return datos;
  }

  async function peticion(url, opciones = {}) {
    const respuesta = await realizarPeticion(url, {
      ...opciones,
      headers: {
        "Content-Type": "application/json",
        ...(opciones.headers || {}),
      },
    });

    return procesarRespuesta(respuesta);
  }

  function establecerBotonCargando(cargando) {
    btnGuardar.disabled = cargando;

    if (cargando) {
      btnGuardar.innerHTML = `
                <span class="spinner-border spinner-border-sm me-2"></span>
                Procesando...
            `;
    }
  }

  // CARGAR USUARIOS DESDE SPRING BOOT

  async function cargarUsuarios() {
    tablaUsuarios.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-4">
                    <div class="spinner-border text-success"></div>
                    <p class="mt-2 mb-0">Cargando usuarios...</p>
                </td>
            </tr>
        `;

    try {
      const respuesta = await peticion(API_USUARIOS);

      usuarios = Array.isArray(respuesta) ? respuesta : [];

      actualizarVista();
    } catch (error) {
      console.error("Error al cargar usuarios:", error);

      usuarios = [];
      actualizarVista();

      notificar(error.message, "danger");
    }
  }

  // ESTADÍSTICAS

  function actualizarEstadisticas() {
    const activos = usuarios.filter((usuario) => usuario.estado === "ACTIVO");

    const administradores = usuarios.filter(
      (usuario) => usuario.rol === "ADMINISTRADOR",
    );

    totalUsuarios.textContent = usuarios.length;
    usuariosActivos.textContent = activos.length;
    totalAdministradores.textContent = administradores.length;
  }

  // MOSTRAR USUARIOS

  function mostrarUsuarios(lista) {
    tablaUsuarios.replaceChildren();

    lista.forEach((usuario) => {
      const fila = plantillaFila.content.cloneNode(true);

      fila.querySelector(".nombre-usuario").textContent =
        usuario.nombreCompleto || "-";

      fila.querySelector(".nombre-usuario-login").textContent =
        usuario.nombreUsuario || "-";

      fila.querySelector(".correo-usuario").textContent = usuario.correo || "-";

      // ROL
      const celdaRol = fila.querySelector(".rol-usuario");
      const etiquetaRol = document.createElement("span");

      etiquetaRol.className = "rol-badge";
      etiquetaRol.textContent = obtenerTextoRol(usuario.rol);

      celdaRol.appendChild(etiquetaRol);

      // ESTADO
      const celdaEstado = fila.querySelector(".estado-usuario");
      const etiquetaEstado = document.createElement("span");

      etiquetaEstado.className =
        usuario.estado === "ACTIVO"
          ? "estado-badge estado-activo"
          : "estado-badge estado-inactivo";

      etiquetaEstado.textContent = obtenerTextoEstado(usuario.estado);

      celdaEstado.appendChild(etiquetaEstado);

      // DETALLE
      fila
        .querySelector(".btn-detalle-usuario")
        .addEventListener("click", () => {
          mostrarDetalleUsuario(usuario.idUsuario);
        });

      // EDITAR
      fila
        .querySelector(".btn-editar-usuario")
        .addEventListener("click", () => {
          editarUsuario(usuario.idUsuario);
        });

      // CAMBIAR ESTADO
      const botonEstado = fila.querySelector(".btn-eliminar-usuario");

      if (usuario.estado === "ACTIVO") {
        botonEstado.innerHTML = '<i class="bi bi-person-lock"></i>';
        botonEstado.title = "Desactivar usuario";
      } else {
        botonEstado.innerHTML = '<i class="bi bi-person-check"></i>';
        botonEstado.title = "Activar usuario";
      }

      botonEstado.addEventListener("click", () => {
        prepararCambioEstado(usuario);
      });

      tablaUsuarios.appendChild(fila);
    });

    usuariosVacio.classList.toggle("d-none", lista.length > 0);

    cantidadUsuarios.textContent = `${lista.length} ${lista.length === 1 ? "usuario" : "usuarios"}`;

    textoResultados.textContent = `Mostrando ${lista.length} de ${usuarios.length} usuarios`;
  }

  // BÚSQUEDA Y FILTROS

  function filtrarUsuarios() {
    const texto = buscarUsuario.value.trim().toLowerCase();
    const rol = filtroRol.value;
    const estado = filtroEstado.value;

    const resultado = usuarios.filter((usuario) => {
      const nombre = (usuario.nombreCompleto || "").toLowerCase();
      const nombreUsuario = (usuario.nombreUsuario || "").toLowerCase();
      const correo = (usuario.correo || "").toLowerCase();

      const coincideTexto =
        nombre.includes(texto) ||
        nombreUsuario.includes(texto) ||
        correo.includes(texto);

      const coincideRol = rol === "" || usuario.rol === rol;

      const coincideEstado = estado === "" || usuario.estado === estado;

      return coincideTexto && coincideRol && coincideEstado;
    });

    mostrarUsuarios(resultado);
  }

  function actualizarVista() {
    actualizarEstadisticas();
    filtrarUsuarios();
  }

  // LIMPIAR FORMULARIO

  function limpiarFormulario() {
    formulario.reset();

    formulario.classList.remove("was-validated");

    usuarioId.value = "";

    usuarioNombre.setCustomValidity("");
    usuarioNombreUsuario.setCustomValidity("");
    usuarioCorreo.setCustomValidity("");
    usuarioContrasena.setCustomValidity("");

    usuarioContrasena.required = true;
    usuarioContrasena.disabled = false;

    ayudaContrasena.textContent =
      "La contraseña es obligatoria para nuevos usuarios.";

    usuarioEstado.value = "ACTIVO";

    establecerBotonCargando(false);
  }

  // NUEVO USUARIO

  function abrirNuevoUsuario() {
    limpiarFormulario();

    tituloModal.textContent = "Nuevo usuario";

    btnGuardar.innerHTML = `
            <i class="bi bi-check-lg me-1"></i>
            Guardar usuario
        `;

    modalUsuario.show();
  }

  // EDITAR USUARIO

  async function editarUsuario(id) {
    try {
      const usuario = await peticion(`${API_USUARIOS}/${id}`);

      limpiarFormulario();

      usuarioId.value = usuario.idUsuario;
      usuarioNombre.value = usuario.nombreCompleto || "";
      usuarioNombreUsuario.value = usuario.nombreUsuario || "";
      usuarioCorreo.value = usuario.correo || "";
      usuarioRol.value = usuario.rol || "";
      usuarioEstado.value = usuario.estado || "ACTIVO";

      usuarioContrasena.value = "";
      usuarioContrasena.required = false;

      ayudaContrasena.textContent =
        "Deja este campo vacío si no deseas cambiar la contraseña.";

      tituloModal.textContent = "Editar usuario";

      btnGuardar.innerHTML = `
                <i class="bi bi-check-lg me-1"></i>
                Actualizar usuario
            `;

      modalUsuario.show();
    } catch (error) {
      console.error("Error al consultar usuario:", error);
      notificar(error.message, "danger");
    }
  }

  // VALIDAR FORMULARIO

  function validarFormulario() {
    const nombreValido = usuarioNombre.value.trim().length >= 3;

    const nombreUsuarioValido = /^[A-Za-z0-9._-]{3,60}$/.test(
      usuarioNombreUsuario.value.trim(),
    );

    const correoValido = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(
      usuarioCorreo.value.trim(),
    );

    const contrasenaValida =
      !usuarioContrasena.required || usuarioContrasena.value.length >= 6;

    usuarioNombre.setCustomValidity(nombreValido ? "" : "Nombre inválido");

    usuarioNombreUsuario.setCustomValidity(
      nombreUsuarioValido ? "" : "Nombre de usuario inválido",
    );

    usuarioCorreo.setCustomValidity(correoValido ? "" : "Correo inválido");

    usuarioContrasena.setCustomValidity(
      contrasenaValida ? "" : "Contraseña inválida",
    );

    formulario.classList.add("was-validated");

    return formulario.checkValidity();
  }

  // REGISTRAR / ACTUALIZAR USUARIO

  async function guardarUsuario(evento) {
    evento.preventDefault();

    if (!validarFormulario()) {
      return;
    }

    const idActual = usuarioId.value;

    const datosUsuario = {
      nombreCompleto: usuarioNombre.value.trim(),

      nombreUsuario: usuarioNombreUsuario.value.trim(),

      correo: usuarioCorreo.value.trim().toLowerCase(),

      rol: usuarioRol.value,

      estado: usuarioEstado.value,
    };

    // La contraseña se envía solamente si fue escrita.
    if (usuarioContrasena.value.trim() !== "") {
      datosUsuario.contrasena = usuarioContrasena.value;
    }

    // En creación es obligatoria.
    if (!idActual && !datosUsuario.contrasena) {
      usuarioContrasena.setCustomValidity("La contraseña es obligatoria.");

      formulario.classList.add("was-validated");
      usuarioContrasena.reportValidity();

      return;
    }

    establecerBotonCargando(true);

    try {
      let respuesta;

      if (idActual) {
        respuesta = await peticion(`${API_USUARIOS}/${idActual}`, {
          method: "PUT",
          body: JSON.stringify(datosUsuario),
        });

        notificar("Usuario actualizado correctamente.", "success");
      } else {
        respuesta = await peticion(API_USUARIOS, {
          method: "POST",
          body: JSON.stringify(datosUsuario),
        });

        notificar("Usuario registrado correctamente.", "success");
      }

      modalUsuario.hide();

      limpiarFormulario();

      await cargarUsuarios();
    } catch (error) {
      console.error("Error al guardar usuario:", error);
      notificar(error.message, "danger");
    } finally {
      establecerBotonCargando(false);
    }
  }

  // DETALLE DEL USUARIO

  async function mostrarDetalleUsuario(id) {
    try {
      const usuario = await peticion(`${API_USUARIOS}/${id}`);

      document.getElementById("detalleUsuarioNombre").textContent =
        usuario.nombreCompleto || "-";

      document.getElementById("detalleUsuarioNombreUsuario").textContent =
        usuario.nombreUsuario ? `@${usuario.nombreUsuario}` : "-";

      document.getElementById("detalleUsuarioCorreo").textContent =
        usuario.correo || "-";

      document.getElementById("detalleUsuarioRol").textContent =
        obtenerTextoRol(usuario.rol);

      document.getElementById("detalleUsuarioEstado").textContent =
        obtenerTextoEstado(usuario.estado);

      document.getElementById("detalleUsuarioFecha").textContent =
        formatearFecha(usuario.fechaRegistro);

      modalDetalle.show();
    } catch (error) {
      console.error("Error al consultar detalle:", error);
      notificar(error.message, "danger");
    }
  }

  // PREPARAR CAMBIO DE ESTADO

  function prepararCambioEstado(usuario) {
    usuarioPendienteCambioEstado = usuario;

    document.getElementById("nombreUsuarioEliminar").textContent =
      usuario.nombreCompleto;

    const nuevoEstado = usuario.estado === "ACTIVO" ? "INACTIVO" : "ACTIVO";

    btnConfirmarEstado.textContent =
      nuevoEstado === "ACTIVO" ? "Activar usuario" : "Desactivar usuario";

    btnConfirmarEstado.className =
      nuevoEstado === "ACTIVO" ? "btn btn-success" : "btn btn-warning";

    modalEstado.show();
  }

  // CONFIRMAR CAMBIO DE ESTADO

  async function confirmarCambioEstado() {
    if (!usuarioPendienteCambioEstado) {
      return;
    }

    const usuario = usuarioPendienteCambioEstado;

    const nuevoEstado = usuario.estado === "ACTIVO" ? "INACTIVO" : "ACTIVO";

    btnConfirmarEstado.disabled = true;

    btnConfirmarEstado.innerHTML = `
            <span class="spinner-border spinner-border-sm me-2"></span>
            Procesando...
        `;

    try {
      await peticion(`${API_USUARIOS}/${usuario.idUsuario}/estado`, {
        method: "PATCH",
        body: JSON.stringify({
          estado: nuevoEstado,
        }),
      });

      modalEstado.hide();

      usuarioPendienteCambioEstado = null;

      notificar(
        `Usuario ${nuevoEstado === "ACTIVO" ? "activado" : "desactivado"} correctamente.`,
        "success",
      );

      await cargarUsuarios();
    } catch (error) {
      console.error("Error al cambiar estado:", error);
      notificar(error.message, "danger");
    } finally {
      btnConfirmarEstado.disabled = false;
      btnConfirmarEstado.textContent = "Confirmar cambio";
    }
  }

  // EVENTOS

  btnNuevoUsuario.addEventListener("click", abrirNuevoUsuario);

  formulario.addEventListener("submit", guardarUsuario);

  buscarUsuario.addEventListener("input", filtrarUsuarios);

  filtroRol.addEventListener("change", filtrarUsuarios);

  filtroEstado.addEventListener("change", filtrarUsuarios);

  btnConfirmarEstado.addEventListener("click", confirmarCambioEstado);

  btnLimpiarFiltros.addEventListener("click", () => {
    buscarUsuario.value = "";
    filtroRol.value = "";
    filtroEstado.value = "";

    filtrarUsuarios();
  });

  // INICIO

  cargarUsuarios();
});
