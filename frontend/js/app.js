const API_URL = "http://localhost:8080/api";

// PERMISOS POR ROL

const permisosPorRol = {
  ADMINISTRADOR: [
    "dashboard",
    "nueva-venta",
    "ventas",
    "productos",
    "clientes",
    "reportes",
    "usuarios",
    "configuracion",
  ],

  VENDEDOR: ["dashboard", "nueva-venta", "ventas", "clientes"],

  ALMACENERO: ["dashboard", "productos"],
};

// OBTENER USUARIO ACTUAL

function obtenerUsuarioActual() {
  try {
    return JSON.parse(localStorage.getItem("usuarioActual"));
  } catch {
    return null;
  }
}

// Ajustar ruta de inicio de sesión según ubicación
function obtenerRutaLogin() {
  return window.location.pathname.includes("/pages/")
    ? "../index.html"
    : "index.html";
}

// VERIFICAR SESIÓN

function verificarSesion() {
  const token = localStorage.getItem("token");
  const usuario = obtenerUsuarioActual();

  if (!token || !usuario) {
    localStorage.removeItem("token");
    localStorage.removeItem("usuarioActual");

    window.location.href = obtenerRutaLogin();
    return false;
  }

  return true;
}

// VERIFICAR ACCESO A UN MÓDULO

function verificarAcceso(modulo) {
  if (!verificarSesion()) {
    return false;
  }

  const usuario = obtenerUsuarioActual();
  const rol = String(usuario.rol || "").toUpperCase();
  const permisos = permisosPorRol[rol] || [];

  if (!permisos.includes(modulo)) {
    window.location.href = "dashboard.html";
    return false;
  }

  return true;
}

// OCULTAR OPCIONES NO PERMITIDAS

function aplicarPermisosMenu() {
  const usuario = obtenerUsuarioActual();

  if (!usuario) return;

  const rol = String(usuario.rol || "").toUpperCase();
  const permisos = permisosPorRol[rol] || [];

  document.querySelectorAll("[data-modulo]").forEach((elemento) => {
    const modulo = elemento.dataset.modulo;
    elemento.hidden = !permisos.includes(modulo);
  });
}

// MOSTRAR INFORMACIÓN DEL USUARIO

function mostrarInformacionUsuario() {
  const usuario = obtenerUsuarioActual();

  if (!usuario) return;

  const nombreElemento = document.getElementById("nombreUsuario");
  const rolElemento = document.getElementById("rolUsuario");

  const nombreMostrar =
    usuario.nombreCompleto || usuario.nombre || usuario.usuario || "Usuario";

  const roles = {
    ADMINISTRADOR: "Administrador",
    VENDEDOR: "Vendedor",
    ALMACENERO: "Almacenero",
  };

  const rolMostrar =
    roles[String(usuario.rol || "").toUpperCase()] ||
    usuario.rol ||
    "Usuario del sistema";

  if (nombreElemento) {
    nombreElemento.textContent = nombreMostrar;
  }

  if (rolElemento) {
    rolElemento.textContent = rolMostrar;
  }
}

// CERRAR SESIÓN

function cerrarSesion() {
  localStorage.removeItem("token");
  localStorage.removeItem("usuarioActual");

  window.location.href = obtenerRutaLogin();
}

// NOTIFICACIONES DEL SISTEMA

function mostrarNotificacion(mensaje, tipo = "success") {
  const elemento = document.getElementById("notificacionSistema");
  const cuerpo = document.getElementById("mensajeNotificacion");

  if (!elemento || !cuerpo) {
    console.log(mensaje);
    return;
  }

  cuerpo.textContent = mensaje;

  elemento.classList.remove(
    "text-bg-success",
    "text-bg-danger",
    "text-bg-warning",
    "text-bg-info",
  );

  const colores = {
    success: "text-bg-success",
    error: "text-bg-danger",
    warning: "text-bg-warning",
    info: "text-bg-info",
  };

  elemento.classList.add(colores[tipo] || colores.success);

  const toast = bootstrap.Toast.getOrCreateInstance(elemento);
  toast.show();
}

// PETICIONES AUTENTICADAS

async function realizarPeticion(url, opciones = {}) {
  const token = localStorage.getItem("token");

  const encabezados = {
    ...(opciones.headers || {}),
    Authorization: `Bearer ${token}`,
  };

  if (opciones.body && !encabezados["Content-Type"]) {
    encabezados["Content-Type"] = "application/json";
  }

  const respuesta = await fetch(url, {
    ...opciones,
    headers: encabezados,
  });

  if (respuesta.status === 401) {
    cerrarSesion();
    throw new Error("Tu sesión ha expirado.");
  }

  if (respuesta.status === 403) {
    throw new Error("No tienes permisos para realizar esta operación.");
  }

  return respuesta;
}

// INICIAR SESIÓN

const formularioLogin = document.getElementById("formulario-login");

if (formularioLogin) {
  formularioLogin.addEventListener("submit", async function (evento) {
    evento.preventDefault();

    const usuario = document.getElementById("usuario").value.trim();
    const contrasena = document.getElementById("contrasena").value;
    const mensaje = document.getElementById("mensaje");

    mensaje.textContent = "Verificando credenciales...";
    mensaje.className = "mt-3 text-secondary";

    try {
      const respuesta = await fetch(`${API_URL}/autenticacion/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          nombreUsuario: usuario,
          contrasena: contrasena,
        }),
      });

      const datos = await respuesta.json();

      if (!respuesta.ok) {
        throw new Error(datos.mensaje || "Credenciales incorrectas.");
      }

      if (!datos.token || !datos.rol) {
        throw new Error(
          "La respuesta del servidor no contiene el token o el rol.",
        );
      }

      if (!datos.idUsuario) {
        throw new Error("El servidor no devolvió el ID del usuario.");
      }

      const rol = String(datos.rol).toUpperCase();

      if (!permisosPorRol[rol]) {
        throw new Error("El usuario tiene un rol no reconocido.");
      }

      localStorage.setItem("token", datos.token);

      localStorage.setItem(
        "usuarioActual",
        JSON.stringify({
          idUsuario: datos.idUsuario,
          usuario: datos.nombreUsuario || usuario,
          nombre:
            datos.nombreCompleto ||
            datos.nombre ||
            datos.nombreUsuario ||
            usuario,
          nombreCompleto:
            datos.nombreCompleto ||
            datos.nombre ||
            datos.nombreUsuario ||
            usuario,
          correo: datos.correo || "",
          rol: rol,
        }),
      );

      window.location.href = "dashboard.html";
    } catch (error) {
      mensaje.textContent = error.message;
      mensaje.className = "mt-3 text-danger";
    }
  });
}

// INICIALIZACIÓN DE PÁGINAS PROTEGIDAS

document.addEventListener("DOMContentLoaded", function () {
  const pagina = window.location.pathname.split("/").pop();

  if (pagina === "index.html" || pagina === "") {
    return;
  }

  if (!verificarSesion()) return;

  aplicarPermisosMenu();
  mostrarInformacionUsuario();

  const paginasPorModulo = {
    "dashboard.html": "dashboard",
    "ventas.html": "nueva-venta",
    "historial-ventas.html": "ventas",
    "producto.html": "productos",
    "clientes.html": "clientes",
    "reportes.html": "reportes",
    "usuarios.html": "usuarios",
    "configuracion.html": "configuracion",
  };

  const modulo = paginasPorModulo[pagina];

  if (modulo) {
    const usuario = obtenerUsuarioActual();
    const rol = String(usuario.rol || "").toUpperCase();
    const permisos = permisosPorRol[rol] || [];

    if (!permisos.includes(modulo)) {
      window.location.href = "dashboard.html";
      return;
    }
  }
});

// MENÚ LATERAL PARA MÓVILES

document.addEventListener("DOMContentLoaded", function () {
  const botonMenu = document.getElementById("btn-menu");
  const sidebar = document.getElementById("sidebar");

  if (!botonMenu || !sidebar) return;

  let fondoMenu = document.querySelector(".fondo-menu-movil");

  if (!fondoMenu) {
    fondoMenu = document.createElement("div");
    fondoMenu.className = "fondo-menu-movil";
    document.body.appendChild(fondoMenu);
  }

  function abrirMenu() {
    sidebar.classList.add("sidebar-abierto");
    fondoMenu.classList.add("fondo-menu-visible");
    botonMenu.setAttribute("aria-expanded", "true");
  }

  function cerrarMenu() {
    sidebar.classList.remove("sidebar-abierto");
    fondoMenu.classList.remove("fondo-menu-visible");
    botonMenu.setAttribute("aria-expanded", "false");
  }

  botonMenu.addEventListener("click", function () {
    if (sidebar.classList.contains("sidebar-abierto")) {
      cerrarMenu();
    } else {
      abrirMenu();
    }
  });

  fondoMenu.addEventListener("click", cerrarMenu);

  sidebar.querySelectorAll("a.menu-link").forEach((enlace) => {
    enlace.addEventListener("click", cerrarMenu);
  });

  document.addEventListener("keydown", function (evento) {
    if (evento.key === "Escape") {
      cerrarMenu();
    }
  });

  window.addEventListener("resize", function () {
    if (window.innerWidth > 991.98) {
      cerrarMenu();
    }
  });
});
