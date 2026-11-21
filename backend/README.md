# Backend - BOTICAS FAMILIA & SALUD

## Descripcion

Backend del sistema de gestion de ventas de la botica Boticas Familia & Salud.
Spring Boot conectado a SQL Server mediante autenticacion integrada de Windows.
Este modulo implementa Roles y Usuarios, el Modulo de Productos con control de stock,
el Modulo de Ventas (Venta + DetalleVenta) con descuento automatico de stock,
seguridad con JWT (BCrypt), configuracion del sistema y reportes de ventas,
con JPA/Hibernate y su API REST.

## Tecnologias

- Java 17.
- Spring Boot.
- Maven.
- Spring Web.
- Spring Data JPA.
- Validation.
- Lombok.
- Spring Security (JWT sin estado, cifrado BCrypt).
- JJWT (0.12.6).
- SQL Server Driver (mssql-jdbc).
- CORS.

## Estado actual

- SQL Server conectado (base: `bd_boticas_familia_salud`).
- Autenticacion integrada de Windows para la conexion JDBC.
- Entidades actuales: `Rol`, `Usuario`, `Producto`, `Venta` y `DetalleVenta`
  (tablas `roles`, `usuarios`, `productos`, `ventas` y `detalle_ventas`).
- Endpoint de salud: `GET /api/salud`.
- API REST de usuarios en `/api/usuarios`.
- API REST de productos en `/api/productos` (con control de stock).
- API REST de ventas en `/api/ventas`: registro de venta con calculo automatico de
  subtotal/IGV/total/vuelto, numero correlativo generado en el servidor (V-%06d),
  descuento automatico de stock y anulacion de venta que restaura el stock.
- Enumeraciones: `RolUsuario` (ADMINISTRADOR, VENDEDOR), `EstadoUsuario` (ACTIVO, INACTIVO),
  `EstadoProducto` (ACTIVO, INACTIVO), `MetodoPago` (EFECTIVO, TARJETA, YAPE, TRANSFERENCIA)
  y `EstadoVenta` (COMPLETADA, PENDIENTE, ANULADA), almacenadas como texto (EnumType.STRING).
- La autenticacion es por token JWT (sin estado): el login cifra y valida con BCrypt las contrasenas
  y devuelve un token que debe enviarse en cada peticion como `Authorization: Bearer <token>`.
  Spring Security protege toda la API menos el login y el endpoint de salud.
  Permisos por rol: `/api/usuarios/**` y `/api/configuracion/**` son solo de ADMINISTRADOR;
  `cambiar-contrasena` la puede usar cualquier usuario autenticado sobre su propia cuenta.

## Seguridad

- **BCrypt** (`BCryptPasswordEncoder`) para guardar contrasenas. Al crear o editar usuarios
  la contrasena se cifra antes de persistir; nunca se guarda en texto plano ni se devuelve en respuestas.
- **JWT HS256** con `jjwt` (firmado con el secreto de `app.seguridad.secreto`, validez
  `app.seguridad.expiracion-minutos`, por defecto 480 minutos = 8 horas).
- Sesion **stateless** (sin cookies de sesion), CSRF desactivado para la API REST y CORS
  para `http://127.0.0.1:5500` y `http://localhost:5500` (configurable con
  `app.seguridad.origenes-permitidos`, separados por coma).
- **Endpoints publicos**: la pagina web (`/`) y sus recursos estaticos
  (`/css/**`, `/js/**`, `/assets/**`), `POST /api/autenticacion/login` y `GET /api/salud`.
- **Frontend servido por el backend**: al compilar se copian `index.html`,
  `css/`, `js/` y `assets/` dentro del jar (`maven-resources-plugin`), de modo
  que el sistema completo se usa abriendo `http://localhost:8080` y las
  peticiones de la pagina van al mismo origen.
- **Respuestas de error** 401/403 en el mismo JSON del sistema:
  `{"fechaHora","estado","error","mensaje","ruta"}`.
- Permisos:
  - `/api/usuarios/**` (excepto `cambiar-contrasena`) y `/api/configuracion/**`: solo ADMINISTRADOR.
  - `POST /api/usuarios/{id}/cambiar-contrasena`: cualquier autenticado, pero solo sobre su propia
    cuenta (un administrador puede cambiar la de cualquiera).
  - Resto de la API `/api/productos`, `/api/ventas`, `/api/reportes`: cualquier autenticado.
- El ultimo administrador activo no puede desactivarse ni degradarse.

## Datos iniciales (desarrollo)

Al arrancar, de forma idempotente, se crean:

- Roles: ADMINISTRADOR ("Acceso completo al sistema.") y VENDEDOR ("Registro y consulta de ventas.").
- Usuario administrador temporal:
  - Nombre completo: Administrador.
  - Nombre de usuario: `admin`.
  - Correo: `admin@boticasfamiliasalud.pe`.
  - Contrasena temporal: `1234`.
  - Rol: ADMINISTRADOR. Estado: ACTIVO.
  - Si en una base heredada aun existia `aperez`, se renombra a `admin` y se
    le asigna la contrasena vigente (migracion idempotente).

ADVERTENCIA DE SEGURIDAD:
La contrasena del usuario `admin` es SOLO un dato temporal de desarrollo. Se almacena cifrada con BCrypt.
Cambiala cuando sea necesario desde el frontend (Configuracion > Seguridad) o directamente en la base.
El sistema nunca devuelve contrasena en las respuestas HTTP.

Productos iniciales (mock, idempotentes por codigo, 20 registros):
Se crean al arrancar solo si el codigo no existe. No se modifican productos ya existentes
si fueron editados manualmente. Ejemplos: `MED-001` Paracetamol 500 mg (Analgesicos, 5.00, stock 50),
`HIG-001` Alcohol 70% (Higiene, 7.00, stock 40), `DIS-001` Termometro digital (Dispositivos, 25.00, stock 8).

## Estructura actual

- `BoticasFamiliaSaludApplication`: clase principal que inicia la aplicacion.
- `SaludControlador`: endpoint de prueba `GET /api/salud`.
- `CorsConfiguracion`: CORS (localhost:5500 y 127.0.0.1:5500).
- `seguridad/JwtServicio`: genera y valida los tokens JWT.
- `seguridad/JwtAutenticacionFiltro`: lee el encabezado `Authorization: Bearer`, valida el token
  y carga el `JwtPrincipal` (id, nombre de usuario y rol) en el contexto de seguridad.
- `seguridad/SeguridadConfig`: API stateless con Spring Security (BCrypt, JWT, CORS, 401/403 JSON
  y permisos por rol).
- `VerificadorConexion`: prueba minima de conexion a SQL Server.
- `DatosInicialesConfiguracion`: crea roles, administrador inicial, productos mock (idempotentes)
  y la configuracion inicial de la botica.
- `entidad/Rol`, `entidad/Usuario`, `entidad/Producto`, `entidad/ConfiguracionBotica`: entidades JPA.
- `entidad/Venta`, `entidad/DetalleVenta`: entidades JPA del modulo de ventas.
- `enumeracion/RolUsuario`, `enumeracion/EstadoUsuario`, `enumeracion/EstadoProducto`,
  `enumeracion/MetodoPago`, `enumeracion/EstadoVenta`: enumeraciones (STRING).
- `repositorio/RolRepositorio`, `repositorio/UsuarioRepositorio`, `repositorio/ProductoRepositorio`,
  `repositorio/VentaRepositorio`, `repositorio/ConfiguracionBoticaRepositorio`: acceso a datos.
- `servicio/RolServicio`, `servicio/UsuarioServicio`, `servicio/ProductoServicio`,
  `servicio/VentaServicio`, `servicio/AutenticacionServicio`, `servicio/ConfiguracionServicio`,
  `servicio/ReporteServicio` y sus implementaciones en `servicio/impl`.
- `dto/usuario`: `UsuarioSolicitud`, `UsuarioRespuesta`, `CambioEstadoUsuarioSolicitud`, `CambioContrasenaSolicitud`.
- `dto/producto`: `ProductoSolicitud`, `ProductoRespuesta`, `CambioEstadoProductoSolicitud`, `AjusteStockSolicitud`.
- `dto/venta`: `VentaSolicitud`, `VentaRespuesta`, `ItemVentaSolicitud`, `ItemVentaRespuesta`.
- `dto/autenticacion`: `LoginSolicitud`, `LoginRespuesta` (incluye el token JWT).
- `dto/configuracion`: `ConfiguracionSolicitud`, `ConfiguracionRespuesta`.
- `dto/reporte`: `ResumenReporte`, `VentasPorDiaReporte`, `ProductosMasVendidosReporte`, `VentasPorMetodoReporte`.
- `controlador/UsuarioControlador`, `controlador/ProductoControlador`, `controlador/VentaControlador`,
  `controlador/AutenticacionControlador`, `controlador/ConfiguracionControlador`, `controlador/ReporteControlador`: API REST.
- `excepcion`: `ManejadorGlobalExcepciones`, `RecursoNoEncontradoExcepcion`, `ReglaNegocioExcepcion`,
  `CredencialesInvalidasExcepcion`, `AccesoDenegadoExcepcion`, `RespuestaError`.
- `application.properties`: configuracion (puerto 8080, SQL Server, JPA con ddl-auto=update).
- `pom.xml`: proyecto Maven.

## Tablas creadas por JPA (ddl-auto=update)

- `roles`
- `usuarios`
- `productos`
- `ventas`
- `detalle_ventas` (con FK a `ventas` y `productos`)
- `configuracion_botica` (una sola fila, id 1, creada al arrancar)

Hibernate (ddl-auto=update) crea o actualiza solo las tablas de las entidades definidas.
No existen tablas de compras/proveedores/inventario.

## Como abrir en IntelliJ IDEA

1. Abrir IntelliJ IDEA.
2. Seleccionar Open.
3. Abrir:
   `C:\Users\vasqu\OneDrive\Escritorio\INTERACCION\backend-botica`
4. Esperar a que Maven descargue las dependencias.
5. Seleccionar JDK 17 si IntelliJ lo solicita.

## Como ejecutar

La autenticacion integrada de Windows requiere la biblioteca nativa
`mssql-jdbc_auth-12.8.1.x64.dll`. Esta instalada en `C:\Users\vasqu\sqljdbc-auth`.

Desde IntelliJ IDEA es OBLIGATORIO configurar la VM option en Run/Debug Configurations:

```
-Djava.library.path=C:\Users\vasqu\sqljdbc-auth
```

Desde la terminal:

```
java -Djava.library.path=C:\Users\vasqu\sqljdbc-auth -jar target\backend-botica-0.0.1-SNAPSHOT.jar
```

O con Maven:

```
mvn spring-boot:run
```

## Como probar

La pagina web y sus recursos estaticos (`/`, `/css/**`, `/js/**`, `/assets/**`)
se sirven sin token. Las rutas de la API (excepto `POST /api/autenticacion/login`
y `GET /api/salud`) exigen el encabezado `Authorization: Bearer <token>` obtenido
del login. Una peticion a la API sin token, con token invalido o con token
expirado devuelve HTTP 401; un usuario autenticado sin permiso para el recurso
devuelve HTTP 403.

### Salud

```
GET http://localhost:8080/api/salud
```

Respuesta (HTTP 200):

```json
{
  "mensaje": "El backend de BOTICAS FAMILIA & SALUD esta funcionando correctamente.",
  "estado": "ACTIVO"
}
```

### Usuarios

Requiere rol ADMINISTRADOR (excepto `cambiar-contrasena`). Listar usuarios:

```
GET http://localhost:8080/api/usuarios
Authorization: Bearer <token-admin>
```

Obtener un usuario:

```
GET http://localhost:8080/api/usuarios/{idUsuario}
Authorization: Bearer <token-admin>
```

Crear usuario (HTTP 201):

```
POST http://localhost:8080/api/usuarios
Authorization: Bearer <token-admin>
```

```json
{
  "nombreCompleto": "Carlos Lopez",
  "nombreUsuario": "clopez",
  "correo": "carlos.lopez@boticasfamiliasalud.pe",
  "contrasena": "Prueba123",
  "rol": "VENDEDOR",
  "estado": "ACTIVO"
}
```

Actualizar usuario (HTTP 200). Si se omite `contrasena` o se envia vacia,
se conserva la contrasena existente:

```
PUT http://localhost:8080/api/usuarios/{idUsuario}
Authorization: Bearer <token-admin>
```

```json
{
  "nombreCompleto": "Carlos Lopez Editado",
  "nombreUsuario": "clopez",
  "correo": "carlos.lopez@boticasfamiliasalud.pe",
  "rol": "VENDEDOR"
}
```

Cambiar estado (activo/inactivo) (HTTP 200):

```
PATCH http://localhost:8080/api/usuarios/{idUsuario}/estado
Authorization: Bearer <token-admin>
```

```json
{
  "estado": "INACTIVO"
}
```

Respuesta de usuario (nunca incluye contrasena):

```json
{
  "idUsuario": 2,
  "nombreCompleto": "Carlos Lopez",
  "nombreUsuario": "clopez",
  "correo": "carlos.lopez@boticasfamiliasalud.pe",
  "rol": "VENDEDOR",
  "descripcionRol": "Registro y consulta de ventas.",
  "estado": "ACTIVO",
  "fechaRegistro": "2026-09-04T20:00:00"
}
```

Errores (JSON consistente con campo `contrasena` nunca presente):

```json
{
  "fechaHora": "2026-09-04T20:00:00",
  "estado": 409,
  "error": "Conflict",
  "mensaje": "Ya existe un usuario con ese nombre de usuario",
  "ruta": "/api/usuarios"
}
```

Codigos de error esperados:

- 400: validaciones @Valid o solicitud mal formada.
- 401: token ausente, invalido o expirado; o credenciales incorrectas.
- 403: usuario autenticado sin permiso para el recurso (vendedor intenta listar usuarios, etc.).
- 404: usuario o rol no encontrado.
- 409: conflicto de negocio (duplicados, ultimo administrador activo, integridad).
- 500: error inesperado (sin detalles internos).

### Productos

Listar todos (HTTP 200):

```
GET http://localhost:8080/api/productos
```

Buscar con parametros opcionales `termino` (codigo, nombre o categoria) y `estado` (ACTIVO/INACTIVO):

```
GET http://localhost:8080/api/productos?termino=paracetamol
GET http://localhost:8080/api/productos?termino=MED-001
GET http://localhost:8080/api/productos?estado=ACTIVO
GET http://localhost:8080/api/productos?termino=alcohol&estado=ACTIVO
```

Obtener un producto (HTTP 200 o 404):

```
GET http://localhost:8080/api/productos/{idProducto}
```

Crear producto (HTTP 201):

```
POST http://localhost:8080/api/productos
```

```json
{
  "codigo": "MED-100",
  "nombre": "Prueba de producto",
  "categoria": "Analgesicos",
  "precio": 10.00,
  "stock": 5,
  "stockMinimo": 5,
  "estado": "ACTIVO"
}
```

Si se omite `estado`, se asume ACTIVO. El codigo se normaliza a mayusculas.
El precio, stock y stockMinimo no pueden ser negativos, y el codigo no puede repetirse
(ignorando mayusculas/minusculas).

Actualizar producto (HTTP 200):

```
PUT http://localhost:8080/api/productos/{idProducto}
```

Cambiar estado (activar/inactivar) (HTTP 200):

```
PATCH http://localhost:8080/api/productos/{idProducto}/estado
```

```json
{
  "estado": "INACTIVO"
}
```

Ajustar stock (HTTP 200 o 409 si deja el stock negativo). Es un ajuste administrativo temporal:
no crea movimientos ni kardex. La cantidad puede ser positiva o negativa:

```
PATCH http://localhost:8080/api/productos/{idProducto}/stock
```

```json
{
  "cantidad": -5,
  "motivo": "Ajuste por conteo fisico"
}
```

El `motivo` solo queda en el log de la aplicacion, no en una tabla adicional.

Respuesta de producto (campos calculados `stockBajo` y `sinStock`):

```json
{
  "idProducto": 1,
  "codigo": "MED-001",
  "nombre": "Paracetamol 500 mg",
  "categoria": "Analgesicos",
  "precio": 5.00,
  "stock": 50,
  "stockMinimo": 5,
  "estado": "ACTIVO",
  "fechaRegistro": "2026-09-04T20:00:00",
  "stockBajo": false,
  "sinStock": false
}
```

Campos calculados:

- `stockBajo`: true cuando `stock` es menor o igual a `stockMinimo` y mayor que 0.
- `sinStock`: true cuando `stock` es 0.

Codigos de error esperados en productos:

- 400: validaciones @Valid (precio/stock/stockMinimo negativos, codigo con caracteres invalidos) o parametro `estado` invalido.
- 404: producto inexistente.
- 409: codigo duplicado (crear o actualizar) o ajuste de stock que deja el stock en negativo.

### Ventas

Listar ventas con filtros opcionales. El `termino` coincide con el numero de venta
exacto o con parte del nombre del cliente. `desde` y `hasta` son fechas ISO (`YYYY-MM-DD`):

```
GET http://localhost:8080/api/ventas
GET http://localhost:8080/api/ventas?termino=maria
GET http://localhost:8080/api/ventas?estado=COMPLETADA
GET http://localhost:8080/api/ventas?metodoPago=EFECTIVO
GET http://localhost:8080/api/ventas?desde=2026-09-01&hasta=2026-09-05
```

Obtener una venta con sus items (HTTP 200 o 404):

```
GET http://localhost:8080/api/ventas/{idVenta}
```

Registrar venta (HTTP 201):

```
POST http://localhost:8080/api/ventas
```

```json
{
  "idUsuario": 1,
  "cliente": "Maria Lopez",
  "metodoPago": "EFECTIVO",
  "montoRecibido": 50.00,
  "aplicarIgv": true,
  "porcentajeIgv": 18,
  "items": [
    { "idProducto": 1, "cantidad": 3 },
    { "idProducto": 7, "cantidad": 2 }
  ]
}
```

El servidor calcula y guarda el numero correlativo (`V-000001`), el subtotal, el IGV,
el total y el vuelto. Descuenta el stock de cada producto. `cliente` es opcional
(si se omite queda "Cliente generico"). `aplicarIgv` y `porcentajeIgv` son opcionales
(sin IGV por defecto). `montoRecibido` solo se usa con `metodoPago=EFECTIVO`.

Respuesta (se rechaza vender productos INACTIVOS o con stock insuficiente):

```json
{
  "idVenta": 1,
  "numeroVenta": "V-000001",
  "fechaVenta": "2026-09-05T13:46:21",
  "cliente": "Maria Lopez",
  "idVendedor": 1,
  "vendedor": "Administrador",
  "metodoPago": "EFECTIVO",
  "subtotal": 29.00,
  "igv": 5.22,
  "total": 34.22,
  "montoRecibido": 50.00,
  "vuelto": 15.78,
  "estado": "COMPLETADA",
  "items": [
    {
      "idProducto": 1,
      "codigo": "MED-001",
      "nombre": "Paracetamol 500 mg",
      "cantidad": 3,
      "precioUnitario": 5.00,
      "subtotal": 15.00
    }
  ]
}
```

En el listado, el campo `items` viene vacio (solo se incluye al obtener una venta por id).

Anular venta (HTTP 200). Solo si no esta anulada ya; devuelve el stock vendido
a los productos:

```
PATCH http://localhost:8080/api/ventas/{idVenta}/anular
```

Codigos de error esperados en ventas:

- 400: validaciones @Valid (sin items, cantidad menor a 1, metodo de pago nulo, etc.).
- 404: venta, producto o vendedor inexistente.
- 409: vendedor INACTIVO, producto INACTIVO, stock insuficiente o venta ya anulada.

### Autenticacion (login)

```
POST http://localhost:8080/api/autenticacion/login
```

```json
{
  "nombreUsuario": "admin",
  "contrasena": "1234"
}
```

Respuesta (HTTP 200):

```json
{
  "idUsuario": 1,
  "nombreCompleto": "Administrador",
  "nombreUsuario": "admin",
  "correo": "admin@boticasfamiliasalud.pe",
  "rol": "ADMINISTRADOR",
  "descripcionRol": "Acceso completo al sistema.",
  "fechaRegistro": "2026-09-04T20:00:00",
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "mensaje": "Inicio de sesion correcto."
}
```

El campo `token` debe enviarse como `Authorization: Bearer <token>` en las siguientes
peticiones. Si el token expira (por defecto 8 horas), se debe iniciar sesion de nuevo.

Los nombres de usuario se validan ignorando mayusculas/minusculas. Credenciales invalidas
o usuarios INACTIVOS devuelven HTTP 401: `{"mensaje":"Usuario o contrasena incorrectos."}`.

### Cambiar contrasena

El usuario autenticado puede cambiar su propia contrasena. Un administrador puede
cambiar la contrasena de cualquier usuario. La contrasena actual debe ser correcta:

```
POST http://localhost:8080/api/usuarios/{idUsuario}/cambiar-contrasena
Authorization: Bearer <token>
```

```json
{
  "contrasenaActual": "1234",
  "nuevaContrasena": "NuevaClave#777",
  "confirmarContrasena": "NuevaClave#777"
}
```

### Configuracion de la botica

La configuracion es una fila unica. GET la crea con valores por defecto la primera vez:

```
GET  http://localhost:8080/api/configuracion
PUT  http://localhost:8080/api/configuracion   (DatosVaciosAction.IGNORE: solo actualiza campos no nulos)
```

```json
{
  "nombreComercial": "BOTICAS FAMILIA & SALUD",
  "ruc": "20123456789",
  "direccion": "Jr. Las Dalias 123 - Lima",
  "telefono": "01-2345678",
  "correo": "contacto@boticasfamiliasalud.pe",
  "afectoIgv": true,
  "porcentajeIgv": 18,
  "comprobanteAutomatico": true
}
```

### Reportes

Solo consideran ventas COMPLETADAS (las anuladas se excluyen). Fechas en formato ISO `YYYY-MM-DD`:

```
GET http://localhost:8080/api/reportes/resumen
GET http://localhost:8080/api/reportes/ventas-por-dia?dias=7
GET http://localhost:8080/api/reportes/ventas-por-dia?desde=2026-09-01&hasta=2026-09-30
GET http://localhost:8080/api/reportes/productos-mas-vendidos?desde=...&hasta=...
GET http://localhost:8080/api/reportes/metodos-pago?desde=...&hasta=...
```

- `resumen`: cantidad de ventas, ingreso total, ingreso con IGV, IGV total, ingreso sin IGV
  y promedio por venta en el rango.
- `ventas-por-dia`: total del dia para cada fecha del rango. Con `dias` rellena con ceros
  los dias sin ventas (por defecto devuelve los ultimos 7 dias).
- `productos-mas-vendidos`: top 10 productos por cantidad vendida (idProducto, codigo, nombre,
  cantidad, monto).
- `metodos-pago`: resumen por metodo de pago (metodo, cantidad, monto).

## Modulos pendientes

- Comprobantes fisicos.
- Compras, proveedores, almacenes, kardex, lotes, vencimientos e inventario avanzado.
- Pruebas automatizadas (backend y frontend).