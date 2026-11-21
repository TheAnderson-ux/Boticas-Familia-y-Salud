// SPRING BOOT + SQL SERVER

const URL_CONFIGURACION = `${API_URL}/configuracion`;

const formulario = document.getElementById("formularioConfiguracion");
const botonGuardar = document.getElementById("btnGuardarConfiguracion");

// REFERENCIAS A LOS CAMPOS

const campos = {
  nombreComercial: document.getElementById("nombreBotica"),
  razonSocial: document.getElementById("razonSocial"),
  ruc: document.getElementById("rucBotica"),
  direccion: document.getElementById("direccionBotica"),
  telefono: document.getElementById("telefonoBotica"),
  correo: document.getElementById("correoBotica"),
  horario: document.getElementById("horarioBotica"),
  logo: document.getElementById("logoBotica"),

  tema: document.getElementById("temaSistema"),
  formatoFecha: document.getElementById("formatoFecha"),
  formatoHora: document.getElementById("formatoHora"),
  notificacionesActivas: document.getElementById("notificacionesActivas"),

  confirmarCancelacionVenta: document.getElementById(
    "confirmarCancelacionVenta",
  ),

  aplicarIgv: document.getElementById("aplicarIgv"),
  porcentajeIgv: document.getElementById("porcentajeIgv"),

  metodoPagoPredeterminado: document.getElementById("metodoPagoPredeterminado"),

  clienteGenerico: document.getElementById("clienteGenerico"),

  mostrarComprobanteAuto: document.getElementById("mostrarComprobanteAuto"),

  stockMinimo: document.getElementById("stockMinimo"),
};

// CONFIGURACION PREDETERMINADA

const configuracionPredeterminada = {
  nombreComercial: "BOTICAS FAMILIA & SALUD",
  razonSocial: "Boticas Familia y Salud S.A.C.",
  ruc: "20123456789",
  direccion: "Av. Principal 123, Lima",
  telefono: "(01) 555-1234",
  correo: "contacto@boticasfamiliasalud.pe",
  horario: "Lun - Dom: 8:00 a. m. - 10:00 p. m.",
  logo: null,

  tema: "claro",
  formatoFecha: "dd/mm/aaaa",
  formatoHora: "12",
  notificacionesActivas: false,

  confirmarCancelacionVenta: true,
  aplicarIgv: false,
  porcentajeIgv: 18,
  metodoPagoPredeterminado: "efectivo",
  clienteGenerico: true,
  mostrarComprobanteAuto: true,

  stockMinimo: 5,
};

// MOSTRAR NOTIFICACIONES BOOTSTRAP

function notificarConfiguracion(mensaje, tipo = "success") {
  const contenedor = document.getElementById("contenedorNotificaciones");

  const plantilla = document.getElementById("plantillaNotificacion");

  if (!contenedor || !plantilla || !window.bootstrap) {
    console.log(mensaje);
    alert(mensaje);
    return;
  }

  const toast = plantilla.content.firstElementChild.cloneNode(true);

  toast.classList.add(tipo === "error" ? "text-bg-danger" : "text-bg-success");

  toast.querySelector(".toast-body").textContent = mensaje;

  contenedor.appendChild(toast);

  const instancia = new bootstrap.Toast(toast, {
    delay: 3500,
  });

  toast.addEventListener("hidden.bs.toast", function () {
    toast.remove();
  });

  instancia.show();
}

// ASIGNAR VALOR A SELECT

function asignarValorSelect(select, valor) {
  if (!select) return;

  const valorTexto = String(valor ?? "");

  const existe = Array.from(select.options).some(
    (opcion) => opcion.value === valorTexto,
  );

  if (valorTexto !== "" && !existe) {
    const opcion = document.createElement("option");

    opcion.value = valorTexto;
    opcion.textContent = valorTexto;
    opcion.selected = true;

    select.appendChild(opcion);
  }

  select.value = valorTexto;
}

// MOSTRAR CONFIGURACION EN EL FORMULARIO

function mostrarConfiguracion(configuracion) {
  campos.nombreComercial.value = configuracion.nombreComercial ?? "";

  campos.razonSocial.value = configuracion.razonSocial ?? "";

  campos.ruc.value = configuracion.ruc ?? "";

  campos.direccion.value = configuracion.direccion ?? "";

  campos.telefono.value = configuracion.telefono ?? "";

  campos.correo.value = configuracion.correo ?? "";

  campos.horario.value = configuracion.horario ?? "";

  campos.logo.value = configuracion.logo ?? "";

  asignarValorSelect(campos.tema, configuracion.tema ?? "claro");

  asignarValorSelect(
    campos.formatoFecha,
    configuracion.formatoFecha ?? "dd/mm/aaaa",
  );

  asignarValorSelect(campos.formatoHora, configuracion.formatoHora ?? "12");

  campos.notificacionesActivas.checked =
    configuracion.notificacionesActivas ?? false;

  campos.confirmarCancelacionVenta.checked =
    configuracion.confirmarCancelacionVenta ?? true;

  campos.aplicarIgv.checked = configuracion.aplicarIgv ?? false;

  campos.porcentajeIgv.value = configuracion.porcentajeIgv ?? 18;

  asignarValorSelect(
    campos.metodoPagoPredeterminado,
    configuracion.metodoPagoPredeterminado ?? "efectivo",
  );

  campos.clienteGenerico.checked = configuracion.clienteGenerico ?? true;

  campos.mostrarComprobanteAuto.checked =
    configuracion.mostrarComprobanteAuto ?? true;

  campos.stockMinimo.value = configuracion.stockMinimo ?? 5;

  // Limpiar errores visuales anteriores.
  formulario.querySelectorAll(".is-invalid").forEach((elemento) => {
    elemento.classList.remove("is-invalid");
  });
}

// OBTENER TODOS LOS DATOS DEL FORMULARIO

function obtenerDatosFormulario() {
  return {
    nombreComercial: campos.nombreComercial.value.trim(),

    razonSocial: campos.razonSocial.value.trim(),

    ruc: campos.ruc.value.trim(),

    direccion: campos.direccion.value.trim(),

    telefono: campos.telefono.value.trim(),

    correo: campos.correo.value.trim(),

    horario: campos.horario.value.trim(),

    logo: campos.logo.value.trim() || null,

    tema: campos.tema.value,

    formatoFecha: campos.formatoFecha.value,

    formatoHora: campos.formatoHora.value,

    notificacionesActivas: campos.notificacionesActivas.checked,

    confirmarCancelacionVenta: campos.confirmarCancelacionVenta.checked,

    aplicarIgv: campos.aplicarIgv.checked,

    porcentajeIgv: Number(campos.porcentajeIgv.value),

    metodoPagoPredeterminado: campos.metodoPagoPredeterminado.value,

    clienteGenerico: campos.clienteGenerico.checked,

    mostrarComprobanteAuto: campos.mostrarComprobanteAuto.checked,

    stockMinimo: Number(campos.stockMinimo.value),
  };
}

// VALIDAR FORMULARIO

function validarConfiguracion(configuracion) {
  formulario.querySelectorAll(".is-invalid").forEach((elemento) => {
    elemento.classList.remove("is-invalid");
  });

  if (!/^\d{11}$/.test(configuracion.ruc)) {
    campos.ruc.classList.add("is-invalid");

    notificarConfiguracion(
      "El RUC debe contener exactamente 11 números.",
      "error",
    );

    campos.ruc.focus();

    return false;
  }

  if (
    !Number.isFinite(configuracion.porcentajeIgv) ||
    configuracion.porcentajeIgv < 0 ||
    configuracion.porcentajeIgv > 100
  ) {
    campos.porcentajeIgv.classList.add("is-invalid");

    notificarConfiguracion(
      "El porcentaje de IGV debe estar entre 0 y 100.",
      "error",
    );

    campos.porcentajeIgv.focus();

    return false;
  }

  if (
    !Number.isInteger(configuracion.stockMinimo) ||
    configuracion.stockMinimo < 0
  ) {
    campos.stockMinimo.classList.add("is-invalid");

    notificarConfiguracion(
      "El stock mínimo debe ser un número entero igual o mayor que cero.",
      "error",
    );

    campos.stockMinimo.focus();

    return false;
  }

  if (!formulario.checkValidity()) {
    formulario.reportValidity();

    return false;
  }

  return true;
}

// CARGAR CONFIGURACION DESDE SQL SERVER

async function cargarConfiguracion() {
  try {
    const respuesta = await realizarPeticion(URL_CONFIGURACION);

    if (!respuesta.ok) {
      throw new Error("No se pudo obtener la configuración del sistema.");
    }

    const configuracion = await respuesta.json();

    mostrarConfiguracion(configuracion);

    console.log("Configuración cargada desde SQL Server:", configuracion);
  } catch (error) {
    console.error("Error al cargar configuración:", error);

    notificarConfiguracion(
      error.message || "Error al cargar la configuración.",
      "error",
    );
  }
}

// GUARDAR CONFIGURACION EN SQL SERVER

async function guardarConfiguracion(configuracion) {
  try {
    botonGuardar.disabled = true;

    botonGuardar.innerHTML = `
            <span class="spinner-border spinner-border-sm me-2"></span>
            Guardando...
        `;

    const respuesta = await realizarPeticion(URL_CONFIGURACION, {
      method: "PUT",

      headers: {
        "Content-Type": "application/json",
      },

      body: JSON.stringify(configuracion),
    });

    if (!respuesta.ok) {
      const texto = await respuesta.text();

      let mensaje = "No se pudieron guardar los cambios.";

      try {
        const error = JSON.parse(texto);

        mensaje = error.mensaje || error.message || mensaje;
      } catch (errorLectura) {
        if (texto) {
          mensaje = texto;
        }
      }

      throw new Error(mensaje);
    }

    // Leer respuesta sin asumir que siempre contiene JSON.
    const textoRespuesta = await respuesta.text();

    let datosGuardados = configuracion;

    if (textoRespuesta.trim() !== "") {
      try {
        datosGuardados = JSON.parse(textoRespuesta);
      } catch (errorLectura) {
        console.warn(
          "El backend no devolvió un JSON válido. Se utilizarán los datos enviados.",
          errorLectura,
        );
      }
    }

    mostrarConfiguracion(datosGuardados);

    console.log("Configuración guardada:", datosGuardados);

    notificarConfiguracion(
      "¡La configuración se guardó correctamente!",
      "success",
    );

    return true;
  } catch (error) {
    console.error("Error al guardar configuración:", error);

    notificarConfiguracion(
      error.message || "No se pudo guardar la configuración.",
      "error",
    );

    return false;
  } finally {
    botonGuardar.disabled = false;

    botonGuardar.innerHTML = `
            <i class="bi bi-floppy me-1"></i>
            Guardar cambios
        `;
  }
}

// EVENTO DEL FORMULARIO

formulario.addEventListener("submit", async function (evento) {
  evento.preventDefault();

  const configuracion = obtenerDatosFormulario();

  if (!validarConfiguracion(configuracion)) {
    return;
  }

  await guardarConfiguracion(configuracion);
});

// RESTABLECER CONFIGURACION

document
  .getElementById("btnRestablecerConfiguracion")
  .addEventListener("click", async function () {
    const confirmar = confirm(
      "¿Deseas restablecer todos los datos de configuración a sus valores predeterminados?",
    );

    if (!confirmar) {
      return;
    }

    const copiaPredeterminada = {
      ...configuracionPredeterminada,
    };

    mostrarConfiguracion(copiaPredeterminada);

    await guardarConfiguracion(copiaPredeterminada);
  });

// QUITAR ERRORES AL MODIFICAR CAMPOS

Object.values(campos).forEach((campo) => {
  if (!campo) return;

  campo.addEventListener("input", function () {
    campo.classList.remove("is-invalid");
  });

  campo.addEventListener("change", function () {
    campo.classList.remove("is-invalid");
  });
});

// INICIALIZAR MODULO

cargarConfiguracion();

console.log("Módulo de configuración inicializado.");
