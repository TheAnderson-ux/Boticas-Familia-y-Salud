# Boticas-Familia-y-Salud
Sistema web para la gestión de ventas, productos, clientes e inventario de Boticas Familia &amp; Salud, desarrollado con Java Spring Boot, Bootstrap y SQL Server.
# Boticas Familia & Salud

Sistema web de gestión para una botica, desarrollado como proyecto académico. Permite administrar las operaciones principales del establecimiento mediante una interfaz web y un backend conectado a SQL Server.

## Descripción del proyecto

Boticas Familia & Salud busca facilitar la gestión de productos, clientes, ventas, usuarios y reportes, centralizando la información en un sistema web.

El proyecto está organizado en dos partes principales: frontend y backend.

## Tecnologías utilizadas

### Frontend
- HTML5
- CSS3
- JavaScript
- Bootstrap

### Backend
- Java
- Spring Boot
- Spring Security
- JWT
- Maven
- Spring Data JPA

### Base de datos
- Microsoft SQL Server

## Funcionalidades

- Inicio de sesión y autenticación.
- Gestión de productos.
- Gestión de clientes.
- Registro y consulta de ventas.
- Historial de ventas.
- Administración de usuarios.
- Configuración de la botica.
- Generación de reportes.

## Estructura del proyecto

```text
Boticas-Familia-y-Salud/
│
├── frontend/
│   ├── assets/
│   ├── css/
│   ├── html/
│   └── js/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   ├── pom.xml
│   └── README.md
│
├── .gitignore
└── README.md
```

## Requisitos

- Java compatible con la versión configurada en el proyecto.
- IntelliJ IDEA.
- Maven.
- Microsoft SQL Server.
- Visual Studio Code (recomendado para el frontend).

## Ejecución del proyecto

### 1. Base de datos

Configurar SQL Server y disponer de la base de datos utilizada por el backend.

### 2. Backend

1. Abrir la carpeta `backend` en IntelliJ IDEA.
2. Configurar las credenciales de conexión a SQL Server mediante las variables de entorno `DB_USERNAME` y `DB_PASSWORD`.
3. Configurar `APP_SEGURIDAD_SECRETO` con una clave local propia.
4. Ejecutar la clase principal `BoticasFamiliaSaludApplication`.

### 3. Frontend

1. Abrir la carpeta `frontend` en Visual Studio Code.
2. Abrir el archivo `html/index.html`.
3. Ejecutarlo mediante Live Server.
4. Verificar la conexión con el backend.

## Seguridad

El proyecto utiliza autenticación mediante JWT. Las credenciales y claves privadas deben configurarse mediante variables de entorno y no deben publicarse en el repositorio.

## Finalidad académica

Este proyecto fue desarrollado con fines académicos y de aprendizaje, utilizando datos ficticios para simular las operaciones de una botica.

---

**Proyecto:** Boticas Familia & Salud  
**Repositorio:** [GitHub](https://github.com/TheAnderson-ux/Boticas-Familia-y-Salud)
