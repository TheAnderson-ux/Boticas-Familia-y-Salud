let productos = [];
let productoEnEdicion = null;
let productoParaInactivar = null;

const URL_PRODUCTOS = `${API_URL}/productos`;

const usuarioActual = obtenerUsuarioActual();
const esAlmacenero = usuarioActual?.rol === "ALMACENERO";

// INICIALIZACIÓN

document.addEventListener("DOMContentLoaded", async () => {
  prepararFormularioCompatible();
  configurarPermisosAlmacenero();

  document
    .getElementById("formProducto")
    ?.addEventListener("submit", guardarProducto);

  document
    .getElementById("buscarProducto")
    ?.addEventListener("input", mostrarProductos);

  document
    .getElementById("filtroCategoria")
    ?.addEventListener("change", mostrarProductos);

  document
    .getElementById("filtroEstado")
    ?.addEventListener("change", mostrarProductos);

  document
    .getElementById("btnLimpiar")
    ?.addEventListener("click", limpiarFiltros);

  document
    .getElementById("btnConfirmarEliminar")
    ?.addEventListener("click", confirmarInactivacion);

  // Restaurar formulario cuando se cierre el modal.
  document
    .getElementById("modalProducto")
    ?.addEventListener("hidden.bs.modal", restaurarFormulario);

  await cargarProductos();
});

// CONFIGURAR PERMISOS DEL ALMACENERO

function configurarPermisosAlmacenero() {
  if (!esAlmacenero) return;

  // El almacenero puede registrar productos nuevos.
  // El precio inicial se permite al registrar.

  const titulo = document.querySelector("#modalProducto .modal-title");

  if (titulo) {
    titulo.textContent = "Registrar producto";
  }
}

// PREPARAR FORMULARIO

function prepararFormularioCompatible() {
  ["laboratorio", "presentacion", "vencimiento", "precioCompra"].forEach(
    (id) => {
      const campo = document.getElementById(id);

      if (!campo) return;

      campo.required = false;

      const contenedor = campo.closest(
        ".col-md-6, .col-md-4, .col-lg-6, .col-lg-4",
      );

      if (contenedor) contenedor.hidden = true;
    },
  );

  [
    "detalleLaboratorio",
    "detallePresentacion",
    "detallePrecioCompra",
    "detalleVencimiento",
  ].forEach((id) => {
    const campo = document.getElementById(id);

    if (!campo) return;

    const contenedor = campo.closest(
      ".col-md-6, .col-md-4, .col-lg-6, .col-lg-4",
    );

    if (contenedor) contenedor.hidden = true;
  });

  const buscar = document.getElementById("buscarProducto");

  if (buscar) {
    buscar.placeholder = "Buscar por código o nombre...";
  }

  const estado = document.getElementById("estado");

  if (estado) {
    estado.replaceChildren(
      new Option("Activo", "ACTIVO"),
      new Option("Inactivo", "INACTIVO"),
    );

    estado.value = "ACTIVO";
  }

  const filtroEstado = document.getElementById("filtroEstado");

  if (filtroEstado) {
    filtroEstado.replaceChildren(
      new Option("Todos", ""),
      new Option("Activo", "ACTIVO"),
      new Option("Inactivo", "INACTIVO"),
    );
  }
}

// CARGAR PRODUCTOS

async function cargarProductos() {
  const tabla = document.getElementById("tablaProductos");

  if (tabla) {
    tabla.innerHTML = `
            <tr>
                <td colspan="8" class="text-center py-4">
                    Cargando productos...
                </td>
            </tr>
        `;
  }

  try {
    const respuesta = await realizarPeticion(URL_PRODUCTOS);
    const datos = await leerRespuesta(respuesta);

    productos = Array.isArray(datos) ? datos : [];

    actualizarOpcionesCategorias();
    mostrarProductos();
  } catch (error) {
    console.error("Error al cargar productos:", error);

    if (tabla) {
      tabla.replaceChildren();

      const fila = document.createElement("tr");
      const celda = document.createElement("td");

      celda.colSpan = 8;
      celda.className = "text-center text-danger py-4";
      celda.textContent =
        error.message || "No se pudieron cargar los productos.";

      fila.appendChild(celda);
      tabla.appendChild(fila);
    }

    mostrarNotificacion(
      error.message || "No se pudieron cargar los productos.",
      "error",
    );
  }
}

// LEER RESPUESTA HTTP

async function leerRespuesta(respuesta) {
  const texto = await respuesta.text();

  let datos = null;

  if (texto) {
    try {
      datos = JSON.parse(texto);
    } catch {
      datos = { mensaje: texto };
    }
  }

  if (!respuesta.ok) {
    throw new Error(
      datos?.mensaje || datos?.message || `Error HTTP ${respuesta.status}`,
    );
  }

  return datos;
}

// FUNCIONES AUXILIARES

function normalizar(texto) {
  return String(texto ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();
}

// ACTUALIZAR CATEGORÍAS

function actualizarOpcionesCategorias() {
  const filtro = document.getElementById("filtroCategoria");

  if (!filtro) return;

  const seleccionActual = filtro.value;

  const categorias = [
    ...new Set(productos.map((producto) => producto.categoria).filter(Boolean)),
  ].sort((a, b) => a.localeCompare(b));

  filtro.replaceChildren(new Option("Todas las categorías", ""));

  categorias.forEach((categoria) => {
    filtro.add(new Option(categoria, categoria));
  });

  if (categorias.includes(seleccionActual)) {
    filtro.value = seleccionActual;
  }

  const selectorFormulario = document.getElementById("categoria");

  if (selectorFormulario) {
    const categoriaActual = selectorFormulario.value;

    selectorFormulario.replaceChildren(new Option("Seleccionar categoría", ""));

    categorias.forEach((categoria) => {
      selectorFormulario.add(new Option(categoria, categoria));
    });

    if (categorias.includes(categoriaActual)) {
      selectorFormulario.value = categoriaActual;
    }
  }
}

// OBTENER DATOS DEL FORMULARIO

function obtenerDatosFormulario() {
  return {
    codigo: document.getElementById("codigo").value.trim(),
    nombre: document.getElementById("nombre").value.trim(),
    categoria: document.getElementById("categoria").value.trim(),
    precio: Number(document.getElementById("precioVenta").value),
    stock: Number(document.getElementById("stock").value),
    stockMinimo: Number(document.getElementById("stockMinimo").value),
    estado: document.getElementById("estado").value,
  };
}

// RESTAURAR FORMULARIO

function restaurarFormulario() {
  const formulario = document.getElementById("formProducto");

  if (!formulario) return;

  formulario.reset();

  productoEnEdicion = null;

  const titulo = document.querySelector("#modalProducto .modal-title");

  if (titulo) {
    titulo.textContent = "Registrar producto";
  }

  // Restaurar campos editables.
  ["codigo", "nombre", "precioVenta", "stock", "stockMinimo"].forEach((id) => {
    const campo = document.getElementById(id);

    if (campo) {
      campo.readOnly = false;
      campo.disabled = false;
      campo.classList.remove("bg-light");
    }
  });

  ["categoria", "estado"].forEach((id) => {
    const campo = document.getElementById(id);

    if (campo) {
      campo.disabled = false;
    }
  });

  const estado = document.getElementById("estado");

  if (estado) {
    estado.value = "ACTIVO";
  }

  const botonGuardar = formulario.querySelector('button[type="submit"]');

  if (botonGuardar) {
    botonGuardar.innerHTML = '<i class="bi bi-save"></i> Guardar producto';
  }
}

// REGISTRAR Y ACTUALIZAR PRODUCTOS

async function guardarProducto(evento) {
  evento.preventDefault();

  const formulario = document.getElementById("formProducto");

  if (!formulario.reportValidity()) return;

  const producto = obtenerDatosFormulario();

  if (!producto.codigo || !producto.nombre || !producto.categoria) {
    mostrarNotificacion("Completa el código, nombre y categoría.", "warning");
    return;
  }

  if (!/^[A-Za-z0-9-]+$/.test(producto.codigo)) {
    mostrarNotificacion(
      "El código solo puede contener letras, números y guiones.",
      "warning",
    );
    return;
  }

  if (
    !Number.isFinite(producto.precio) ||
    producto.precio < 0 ||
    !Number.isInteger(producto.stock) ||
    producto.stock < 0 ||
    !Number.isInteger(producto.stockMinimo) ||
    producto.stockMinimo < 0
  ) {
    mostrarNotificacion("Verifica el precio, stock y stock mínimo.", "warning");
    return;
  }

  const editando = productoEnEdicion !== null;

  // ALMACENERO: al editar, conservar todos los datos
  // originales excepto el stock.
  if (esAlmacenero && editando) {
    const original = productos.find((p) => p.idProducto === productoEnEdicion);

    if (!original) {
      mostrarNotificacion("No se encontró el producto.", "error");
      return;
    }

    producto.codigo = original.codigo;
    producto.nombre = original.nombre;
    producto.categoria = original.categoria;
    producto.precio = original.precio;
    producto.stockMinimo = original.stockMinimo;
    producto.estado = original.estado;
  }

  // ALMACENERO: productos nuevos comienzan activos.
  if (esAlmacenero && !editando) {
    producto.estado = "ACTIVO";
  }

  const botonGuardar = formulario.querySelector('button[type="submit"]');

  if (botonGuardar) botonGuardar.disabled = true;

  try {
    const url = editando
      ? `${URL_PRODUCTOS}/${productoEnEdicion}`
      : URL_PRODUCTOS;

    const respuesta = await realizarPeticion(url, {
      method: editando ? "PUT" : "POST",
      body: JSON.stringify(producto),
    });

    await leerRespuesta(respuesta);

    bootstrap.Modal.getOrCreateInstance(
      document.getElementById("modalProducto"),
    ).hide();

    restaurarFormulario();

    await cargarProductos();

    mostrarNotificacion(
      editando
        ? "Producto actualizado correctamente."
        : "Producto registrado correctamente.",
    );
  } catch (error) {
    console.error("Error al guardar producto:", error);

    mostrarNotificacion(
      error.message || "No se pudo guardar el producto.",
      "error",
    );
  } finally {
    if (botonGuardar) botonGuardar.disabled = false;
  }
}

// MOSTRAR PRODUCTOS

function mostrarProductos() {
  const tabla = document.getElementById("tablaProductos");

  if (!tabla) return;

  const busqueda = normalizar(
    document.getElementById("buscarProducto")?.value.trim(),
  );

  const categoria = document.getElementById("filtroCategoria")?.value || "";

  const estado = document.getElementById("filtroEstado")?.value || "";

  const filtrados = productos.filter((producto) => {
    const coincideTexto = normalizar(
      `${producto.codigo} ${producto.nombre}`,
    ).includes(busqueda);

    return (
      coincideTexto &&
      (!categoria || producto.categoria === categoria) &&
      (!estado || producto.estado === estado)
    );
  });

  tabla.replaceChildren();

  if (filtrados.length === 0) {
    const fila = document.createElement("tr");
    const celda = document.createElement("td");

    celda.colSpan = 8;
    celda.className = "text-center py-4";

    celda.textContent = productos.length
      ? "No se encontraron productos con esos filtros."
      : "Todavía no hay productos registrados.";

    fila.appendChild(celda);
    tabla.appendChild(fila);

    actualizarEstadisticas();
    return;
  }

  filtrados.forEach((producto) => {
    const fila = document.createElement("tr");

    const valores = [
      producto.codigo,
      producto.nombre,
      producto.categoria,
      `S/ ${Number(producto.precio).toFixed(2)}`,
      producto.stock,
      "-",
      producto.estado,
    ];

    valores.forEach((valor, indice) => {
      const celda = document.createElement("td");

      if (indice === 1) {
        const fuerte = document.createElement("strong");
        fuerte.textContent = valor ?? "";
        celda.appendChild(fuerte);
      } else if (indice === 6) {
        const badge = document.createElement("span");

        badge.className =
          producto.estado === "ACTIVO"
            ? "badge bg-success"
            : "badge bg-secondary";

        badge.textContent =
          producto.estado === "ACTIVO" ? "Activo" : "Inactivo";

        celda.appendChild(badge);
      } else {
        celda.textContent = valor ?? "-";
      }

      fila.appendChild(celda);
    });

    const acciones = document.createElement("td");
    const grupo = document.createElement("div");

    grupo.className = "d-flex gap-2";

    // Consultar detalle: permitido para todos.
    grupo.appendChild(
      crearBotonAccion(
        "btn btn-sm btn-outline-info",
        "bi bi-eye",
        "Ver detalle",
        () => verProducto(producto),
      ),
    );

    // Editar: el almacenero solo puede actualizar stock.
    grupo.appendChild(
      crearBotonAccion(
        "btn btn-sm btn-outline-primary",
        "bi bi-pencil",
        esAlmacenero ? "Actualizar stock" : "Editar",
        () => editarProducto(producto),
      ),
    );

    // Cambiar estado: solamente otros roles.
    if (!esAlmacenero) {
      if (producto.estado === "ACTIVO") {
        grupo.appendChild(
          crearBotonAccion(
            "btn btn-sm btn-outline-danger",
            "bi bi-ban",
            "Inactivar",
            () => solicitarInactivacion(producto),
          ),
        );
      } else {
        grupo.appendChild(
          crearBotonAccion(
            "btn btn-sm btn-outline-success",
            "bi bi-check-circle",
            "Activar",
            () => cambiarEstadoProducto(producto, "ACTIVO"),
          ),
        );
      }
    }

    acciones.appendChild(grupo);
    fila.appendChild(acciones);
    tabla.appendChild(fila);
  });

  actualizarEstadisticas();
}

// CREAR BOTONES DE ACCIONES

function crearBotonAccion(clases, icono, titulo, accion) {
  const boton = document.createElement("button");

  boton.type = "button";
  boton.className = clases;
  boton.title = titulo;
  boton.setAttribute("aria-label", titulo);

  const elementoIcono = document.createElement("i");
  elementoIcono.className = icono;

  boton.appendChild(elementoIcono);
  boton.addEventListener("click", accion);

  return boton;
}

// ACTUALIZAR ESTADÍSTICAS

function actualizarEstadisticas() {
  const total = productos.length;

  const activos = productos.filter(
    (producto) => producto.estado === "ACTIVO",
  ).length;

  const stockBajo = productos.filter(
    (producto) => producto.stock > 0 && producto.stock <= producto.stockMinimo,
  ).length;

  const sinStock = productos.filter((producto) => producto.stock === 0).length;

  const totalEl = document.getElementById("totalProductos");
  const activosEl = document.getElementById("productosActivos");
  const stockEl = document.getElementById("productosStockBajo");

  if (totalEl) totalEl.textContent = total;
  if (activosEl) activosEl.textContent = activos;

  if (stockEl) {
    stockEl.textContent = stockBajo + sinStock;
  }
}

// LIMPIAR FILTROS

function limpiarFiltros() {
  document.getElementById("buscarProducto").value = "";
  document.getElementById("filtroCategoria").value = "";
  document.getElementById("filtroEstado").value = "";

  mostrarProductos();
}

// EDITAR PRODUCTO

function editarProducto(producto) {
  productoEnEdicion = producto.idProducto;

  document.getElementById("codigo").value = producto.codigo ?? "";
  document.getElementById("nombre").value = producto.nombre ?? "";
  document.getElementById("categoria").value = producto.categoria ?? "";
  document.getElementById("precioVenta").value = producto.precio ?? 0;
  document.getElementById("stock").value = producto.stock ?? 0;

  document.getElementById("stockMinimo").value = producto.stockMinimo ?? 0;

  document.getElementById("estado").value = producto.estado ?? "ACTIVO";

  const titulo = document.querySelector("#modalProducto .modal-title");

  if (esAlmacenero) {
    // Bloquear todos los campos excepto stock.
    ["codigo", "nombre", "precioVenta", "stockMinimo"].forEach((id) => {
      const campo = document.getElementById(id);

      if (campo) {
        campo.readOnly = true;
        campo.classList.add("bg-light");
      }
    });

    ["categoria", "estado"].forEach((id) => {
      const campo = document.getElementById(id);

      if (campo) {
        campo.disabled = true;
      }
    });

    // El stock permanece editable.
    const stock = document.getElementById("stock");

    if (stock) {
      stock.readOnly = false;
      stock.disabled = false;
      stock.focus();
    }

    if (titulo) {
      titulo.textContent = "Actualizar stock";
    }
  } else {
    if (titulo) {
      titulo.textContent = "Editar producto";
    }
  }

  const botonGuardar = document.querySelector(
    '#formProducto button[type="submit"]',
  );

  if (botonGuardar) {
    botonGuardar.innerHTML = esAlmacenero
      ? '<i class="bi bi-save"></i> Actualizar stock'
      : '<i class="bi bi-save"></i> Guardar producto';
  }

  bootstrap.Modal.getOrCreateInstance(
    document.getElementById("modalProducto"),
  ).show();
}

// VER DETALLE DEL PRODUCTO

function verProducto(producto) {
  const asignaciones = {
    detalleNombre: producto.nombre,
    detalleCodigo: producto.codigo,
    detalleCategoria: producto.categoria,
    detallePrecioVenta: `S/ ${Number(producto.precio).toFixed(2)}`,
    detalleStock: producto.stock,
    detalleStockMinimo: producto.stockMinimo,
    detalleEstado: producto.estado === "ACTIVO" ? "Activo" : "Inactivo",
  };

  Object.entries(asignaciones).forEach(([id, valor]) => {
    const elemento = document.getElementById(id);

    if (elemento) {
      elemento.textContent = valor ?? "-";
    }
  });

  bootstrap.Modal.getOrCreateInstance(
    document.getElementById("modalDetalleProducto"),
  ).show();
}

// INACTIVAR PRODUCTO

function solicitarInactivacion(producto) {
  if (esAlmacenero) {
    mostrarNotificacion(
      "No tienes permiso para inactivar productos.",
      "warning",
    );
    return;
  }

  productoParaInactivar = producto;

  const nombre = document.getElementById("nombreProductoEliminar");

  if (nombre) {
    nombre.textContent = producto.nombre;
  }

  const titulo = document.querySelector("#modalEliminarProducto .modal-title");

  if (titulo) {
    titulo.textContent = "Inactivar producto";
  }

  bootstrap.Modal.getOrCreateInstance(
    document.getElementById("modalEliminarProducto"),
  ).show();
}

async function confirmarInactivacion() {
  if (esAlmacenero) return;
  if (!productoParaInactivar) return;

  await cambiarEstadoProducto(productoParaInactivar, "INACTIVO");

  bootstrap.Modal.getOrCreateInstance(
    document.getElementById("modalEliminarProducto"),
  ).hide();

  productoParaInactivar = null;
}

// CAMBIAR ESTADO DEL PRODUCTO

async function cambiarEstadoProducto(producto, estado) {
  if (esAlmacenero) {
    mostrarNotificacion(
      "No tienes permiso para cambiar el estado de productos.",
      "warning",
    );
    return;
  }

  try {
    const respuesta = await realizarPeticion(
      `${URL_PRODUCTOS}/${producto.idProducto}/estado`,
      {
        method: "PATCH",
        body: JSON.stringify({ estado }),
      },
    );

    await leerRespuesta(respuesta);
    await cargarProductos();

    mostrarNotificacion(
      estado === "ACTIVO"
        ? "Producto activado correctamente."
        : "Producto inactivado correctamente.",
    );
  } catch (error) {
    console.error("Error al cambiar estado:", error);

    mostrarNotificacion(
      error.message || "No se pudo cambiar el estado.",
      "error",
    );
  }
}
