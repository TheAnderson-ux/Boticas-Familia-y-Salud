
-- Crear base si no existe
IF DB_ID(N'bd_boticas_familia_salud') IS NULL
BEGIN
    CREATE DATABASE [bd_boticas_familia_salud];
END;
GO
USE [bd_boticas_familia_salud];
GO

-- Eliminar tablas existentes (nuevas descendientes de ventas y detalle_ventas)
IF OBJECT_ID(N'dbo.detalle_ventas', N'U') IS NOT NULL DROP TABLE dbo.detalle_ventas;
IF OBJECT_ID(N'dbo.ventas', N'U') IS NOT NULL DROP TABLE dbo.ventas;
IF OBJECT_ID(N'dbo.usuarios', N'U') IS NOT NULL DROP TABLE dbo.usuarios;
IF OBJECT_ID(N'dbo.roles', N'U') IS NOT NULL DROP TABLE dbo.roles;
IF OBJECT_ID(N'dbo.productos', N'U') IS NOT NULL DROP TABLE dbo.productos;
IF OBJECT_ID(N'dbo.configuracion_botica', N'U') IS NOT NULL DROP TABLE dbo.configuracion_botica;
GO

-- Tabla: configuracion_botica
CREATE TABLE dbo.configuracion_botica (
    [id_configuracion] bigint IDENTITY(1,1) NOT NULL,
    [aplicar_igv] bit NOT NULL,
    [cliente_generico] bit NOT NULL,
    [confirmar_cancelacion_venta] bit NOT NULL,
    [correo] varchar(120) NOT NULL,
    [direccion] varchar(200) NOT NULL,
    [formato_fecha] varchar(20) NOT NULL,
    [formato_hora] varchar(10) NOT NULL,
    [horario] varchar(120) NOT NULL,
    [logo] varchar(max),
    [metodo_pago_predeterminado] varchar(20) NOT NULL,
    [mostrar_comprobante_auto] bit NOT NULL,
    [nombre_comercial] varchar(120) NOT NULL,
    [notificaciones_activas] bit NOT NULL,
    [porcentaje_igv] numeric(5,2) NOT NULL,
    [razon_social] varchar(150) NOT NULL,
    [ruc] varchar(20) NOT NULL,
    [stock_minimo] int NOT NULL,
    [telefono] varchar(40) NOT NULL,
    [tema] varchar(20) NOT NULL
);
GO

-- Tabla: productos
CREATE TABLE dbo.productos (
    [id_producto] bigint IDENTITY(1,1) NOT NULL,
    [categoria] varchar(80) NOT NULL,
    [codigo] varchar(30) NOT NULL,
    [estado] varchar(20) NOT NULL,
    [fecha_registro] datetime2(6) NOT NULL,
    [nombre] varchar(120) NOT NULL,
    [precio] numeric(10,2) NOT NULL,
    [stock] int NOT NULL,
    [stock_minimo] int NOT NULL
);
GO

-- Tabla: roles
CREATE TABLE dbo.roles (
    [id_rol] bigint IDENTITY(1,1) NOT NULL,
    [descripcion] varchar(120) NOT NULL,
    [nombre] varchar(30) NOT NULL
);
GO

-- Tabla: usuarios
CREATE TABLE dbo.usuarios (
    [id_usuario] bigint IDENTITY(1,1) NOT NULL,
    [contrasena] varchar(100) NOT NULL,
    [correo] varchar(120) NOT NULL,
    [estado] varchar(20) NOT NULL,
    [fecha_registro] datetime2(6) NOT NULL,
    [nombre_completo] varchar(150) NOT NULL,
    [nombre_usuario] varchar(60) NOT NULL,
    [id_rol] bigint NOT NULL
);
GO

-- Tabla: ventas
CREATE TABLE dbo.ventas (
    [id_venta] bigint IDENTITY(1,1) NOT NULL,
    [cliente] varchar(150) NOT NULL,
    [estado] varchar(20) NOT NULL,
    [fecha_venta] datetime2(6) NOT NULL,
    [igv] numeric(10,2) NOT NULL,
    [metodo_pago] varchar(15) NOT NULL,
    [monto_recibido] numeric(10,2),
    [numero_venta] varchar(20) NOT NULL,
    [subtotal] numeric(10,2) NOT NULL,
    [total] numeric(10,2) NOT NULL,
    [vuelto] numeric(10,2),
    [id_usuario] bigint NOT NULL
);
GO

-- Tabla: detalle_ventas
CREATE TABLE dbo.detalle_ventas (
    [id_detalle_venta] bigint IDENTITY(1,1) NOT NULL,
    [cantidad] int NOT NULL,
    [codigo_producto] varchar(30) NOT NULL,
    [nombre_producto] varchar(120) NOT NULL,
    [precio_unitario] numeric(10,2) NOT NULL,
    [subtotal] numeric(10,2) NOT NULL,
    [id_producto] bigint NOT NULL,
    [id_venta] bigint NOT NULL
);
GO

SET IDENTITY_INSERT dbo.configuracion_botica ON;
-- Datos de: configuracion_botica
INSERT INTO dbo.configuracion_botica ([id_configuracion], [aplicar_igv], [cliente_generico], [confirmar_cancelacion_venta], [correo], [direccion], [formato_fecha], [formato_hora], [horario], [logo], [metodo_pago_predeterminado], [mostrar_comprobante_auto], [nombre_comercial], [notificaciones_activas], [porcentaje_igv], [razon_social], [ruc], [stock_minimo], [telefono], [tema])
VALUES
    (1, 0, 1, 1, 'contacto@boticasfamiliasalud.pe', 'Av. Principal 123, Lima', 'dd/mm/aaaa', '12', 'Lun - Dom: 8:00 a. m. - 10:00 p. m.', NULL, 'efectivo', 1, 'BOTICAS FAMILIA & SALUD', 0, 18.00, 'Boticas Familia y Salud S.A.C.', '20123456789', 5, '(01) 555-1234', 'claro')
;
GO
SET IDENTITY_INSERT dbo.configuracion_botica OFF;
GO

SET IDENTITY_INSERT dbo.productos ON;
-- Datos de: productos
INSERT INTO dbo.productos ([id_producto], [categoria], [codigo], [estado], [fecha_registro], [nombre], [precio], [stock], [stock_minimo])
VALUES
    (1, 'Analgesicos', 'MED-001', 'ACTIVO', '2026-09-04 22:11:15.0430360', 'Paracetamol 500 mg', 5.00, 50, 5),
    (2, 'Analgesicos', 'MED-002', 'ACTIVO', '2026-09-04 22:11:15.0815140', 'Ibuprofeno 400 mg', 8.50, 35, 5),
    (3, 'Antibioticos', 'MED-003', 'ACTIVO', '2026-09-04 22:11:15.0888530', 'Amoxicilina 500 mg', 18.00, 20, 5),
    (4, 'Digestivos', 'MED-004', 'ACTIVO', '2026-09-04 22:11:15.0931810', 'Omeprazol 20 mg', 12.50, 25, 5),
    (5, 'Antialergicos', 'MED-005', 'ACTIVO', '2026-09-04 22:11:15.0972250', 'Loratadina 10 mg', 9.00, 30, 5),
    (6, 'Jarabes', 'JAR-001', 'ACTIVO', '2026-09-04 22:11:15.1014760', 'Jarabe para la tos', 16.50, 15, 5),
    (7, 'Higiene', 'HIG-001', 'ACTIVO', '2026-09-04 22:11:15.1060190', 'Alcohol 70%', 7.00, 40, 5),
    (8, 'Higiene', 'HIG-002', 'ACTIVO', '2026-09-04 22:11:15.1161780', 'Agua oxigenada', 6.50, 18, 5),
    (9, 'Primeros auxilios', 'MAT-001', 'ACTIVO', '2026-09-04 22:11:15.1220390', 'Algodon', 4.50, 60, 10),
    (10, 'Primeros auxilios', 'MAT-002', 'ACTIVO', '2026-09-04 22:11:15.1261600', 'Curitas', 3.50, 45, 10),
    (11, 'Primeros auxilios', 'MAT-003', 'ACTIVO', '2026-09-04 22:11:15.1328550', 'Gasas esteriles', 6.00, 30, 5),
    (12, 'Dispositivos', 'DIS-001', 'ACTIVO', '2026-09-04 22:11:15.1370920', 'Termometro digital', 25.00, 8, 2),
    (13, 'Soluciones', 'SOL-001', 'ACTIVO', '2026-09-04 22:11:15.1453860', 'Suero fisiologico', 5.50, 22, 5),
    (14, 'Vitaminas', 'VIT-001', 'ACTIVO', '2026-09-04 22:11:15.1453860', 'Vitamina C', 14.00, 28, 5),
    (15, 'Vitaminas', 'VIT-002', 'ACTIVO', '2026-09-04 22:11:15.1528410', 'Multivitaminico', 22.00, 16, 5),
    (16, 'Higiene', 'HIG-003', 'ACTIVO', '2026-09-04 22:11:15.1614270', 'Gel antibacterial', 8.00, 32, 5),
    (17, 'Dermatologia', 'DER-001', 'ACTIVO', '2026-09-04 22:11:15.1655890', 'Protector solar', 35.00, 12, 3),
    (18, 'Dermatologia', 'DER-002', 'ACTIVO', '2026-09-04 22:11:15.1719080', 'Crema antiseptica', 11.50, 19, 5),
    (19, 'Respiratorio', 'GRA-001', 'ACTIVO', '2026-09-04 22:11:15.1759770', 'Pastillas para la garganta', 7.50, 40, 5),
    (20, 'Soluciones', 'SOL-002', 'ACTIVO', '2026-09-04 22:11:15.1759770', 'Sales de rehidratacion oral', 4.00, 26, 5)
;
GO
SET IDENTITY_INSERT dbo.productos OFF;
GO

SET IDENTITY_INSERT dbo.roles ON;
-- Datos de: roles
INSERT INTO dbo.roles ([id_rol], [descripcion], [nombre])
VALUES
    (1, 'Acceso completo al sistema.', 'ADMINISTRADOR'),
    (2, 'Registro y consulta de ventas.', 'VENDEDOR')
;
GO
SET IDENTITY_INSERT dbo.roles OFF;
GO

SET IDENTITY_INSERT dbo.usuarios ON;
-- Datos de: usuarios
INSERT INTO dbo.usuarios ([id_usuario], [contrasena], [correo], [estado], [fecha_registro], [nombre_completo], [nombre_usuario], [id_rol])
VALUES
    (1, '$2a$10$SZ1Ikb0alCpCVMqy3aji0unBD68hGzmtVRw5kQ4XYH9hyrUaP227y', 'ana.perez@boticasfamiliasalud.pe', 'ACTIVO', '2026-09-04 20:55:29.1177070', 'Ana Perez', 'aperez', 1)
;
GO
SET IDENTITY_INSERT dbo.usuarios OFF;
GO

ALTER TABLE dbo.configuracion_botica ADD CONSTRAINT [PK__configur__16A13EBD8869A209] PRIMARY KEY ([id_configuracion] ASC);
GO

ALTER TABLE dbo.productos ADD CONSTRAINT [PK__producto__FF341C0DF8DBA826] PRIMARY KEY ([id_producto] ASC);
ALTER TABLE dbo.productos ADD CONSTRAINT [UKh04wpyqwddobltuqq56cp6s05] UNIQUE ([codigo] ASC);
GO

ALTER TABLE dbo.roles ADD CONSTRAINT [PK__roles__6ABCB5E055D2BD51] PRIMARY KEY ([id_rol] ASC);
ALTER TABLE dbo.roles ADD CONSTRAINT [UKldv0v52e0udsh2h1rs0r0gw1n] UNIQUE ([nombre] ASC);
GO

ALTER TABLE dbo.usuarios ADD CONSTRAINT [PK__usuarios__4E3E04ADDBDE566E] PRIMARY KEY ([id_usuario] ASC);
ALTER TABLE dbo.usuarios ADD CONSTRAINT [UKcdmw5hxlfj78uf4997i3qyyw5] UNIQUE ([correo] ASC);
ALTER TABLE dbo.usuarios ADD CONSTRAINT [UKof5vabgukahdwmgxk4kjrbu98] UNIQUE ([nombre_usuario] ASC);
GO

ALTER TABLE dbo.ventas ADD CONSTRAINT [PK__ventas__459533BFCAA55C1B] PRIMARY KEY ([id_venta] ASC);
ALTER TABLE dbo.ventas ADD CONSTRAINT [UKsb0bn5xgym5jnwmw7fnbkyqq6] UNIQUE ([numero_venta] ASC);
GO

ALTER TABLE dbo.detalle_ventas ADD CONSTRAINT [PK__detalle___5B265D4742CF11CF] PRIMARY KEY ([id_detalle_venta] ASC);
GO

ALTER TABLE dbo.usuarios ADD CONSTRAINT [FK3kl77pehgupicftwfreqnjkll] FOREIGN KEY ([id_rol]) REFERENCES dbo.roles ([id_rol]);
GO

ALTER TABLE dbo.ventas ADD CONSTRAINT [FKngvsjlvvv240ohesoj9e87s3h] FOREIGN KEY ([id_usuario]) REFERENCES dbo.usuarios ([id_usuario]);
GO

ALTER TABLE dbo.detalle_ventas ADD CONSTRAINT [FK6457hpcqok31dpejm2gv2x5uu] FOREIGN KEY ([id_venta]) REFERENCES dbo.ventas ([id_venta]);
ALTER TABLE dbo.detalle_ventas ADD CONSTRAINT [FK7n95cag4p1jy8dajap81mg56a] FOREIGN KEY ([id_producto]) REFERENCES dbo.productos ([id_producto]);
GO

-- Fin del script generado automaticamente.

