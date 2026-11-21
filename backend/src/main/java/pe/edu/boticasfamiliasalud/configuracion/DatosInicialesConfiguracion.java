package pe.edu.boticasfamiliasalud.configuracion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.edu.boticasfamiliasalud.entidad.ConfiguracionBotica;
import pe.edu.boticasfamiliasalud.entidad.Cliente;
import pe.edu.boticasfamiliasalud.entidad.Producto;
import pe.edu.boticasfamiliasalud.entidad.Usuario;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoCliente;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoProducto;
import pe.edu.boticasfamiliasalud.enumeracion.EstadoUsuario;
import pe.edu.boticasfamiliasalud.enumeracion.RolUsuario;
import pe.edu.boticasfamiliasalud.repositorio.ClienteRepositorio;
import pe.edu.boticasfamiliasalud.repositorio.ConfiguracionBoticaRepositorio;
import pe.edu.boticasfamiliasalud.repositorio.ProductoRepositorio;
import pe.edu.boticasfamiliasalud.repositorio.UsuarioRepositorio;
import pe.edu.boticasfamiliasalud.servicio.RolServicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Inicializacion de datos de desarrollo al arrancar la aplicacion.
 *
 * 1) Crea los roles ADMINISTRADOR y VENDEDOR de forma idempotente.
 * 2) Garantiza el usuario administrador "admin" (contrasena "1234").
 *    Si ya existia el antiguo "aperez", se renombra a "admin" y se le asigna
 *    la contrasena vigente (idempotente).
 * 3) Migra las contrasenas en texto plano a BCrypt (idempotente).
 * 4) Crea 20 productos mock de botica solo si no existen (idempotente por codigo;
 *    no modifica productos que ya existen).
 *
 * No inserta ventas, detalle_ventas ni configuracion de botica. No imprime contrasenas.
 */
@Component
public class DatosInicialesConfiguracion implements CommandLineRunner {

    private static final Logger registro = LoggerFactory.getLogger(DatosInicialesConfiguracion.class);

    private static final String PREFIJO_BCRYPT = "$2";

    public static final String USUARIO_ADMINISTRADOR = "admin";

    public static final String CONTRASENA_ADMINISTRADOR = "1234";

    private final RolServicio rolServicio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final ConfiguracionBoticaRepositorio configuracionRepositorio;
    private final PasswordEncoder passwordEncoder;

    public DatosInicialesConfiguracion(RolServicio rolServicio,
                                       UsuarioRepositorio usuarioRepositorio,
                                       ProductoRepositorio productoRepositorio,
                                       ClienteRepositorio clienteRepositorio,
                                       ConfiguracionBoticaRepositorio configuracionRepositorio,
                                       PasswordEncoder passwordEncoder) {
        this.rolServicio = rolServicio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.configuracionRepositorio = configuracionRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... argumentos) {
        rolServicio.inicializarRoles();
        normalizarAdministradorInicial();
        migrarContrasenasAbcrypt();
        crearProductosIniciales();
        crearClientesIniciales();
        crearConfiguracionInicial();
    }

    /**
     * Garantiza la existencia del administrador inicial "admin" con la
     * contrasena de desarrollo "1234". Si en una base heredada todavia existe
     * el antiguo "aperez", se renombra a "admin" y se actualiza su contrasena.
     */
    private void normalizarAdministradorInicial() {
        java.util.Optional<Usuario> administrador = usuarioRepositorio
                .findByNombreUsuarioIgnoreCase(USUARIO_ADMINISTRADOR);

        if (administrador.isPresent()) {
            registro.info("Usuario administrador inicial ya existe: {}", USUARIO_ADMINISTRADOR);
            return;
        }

        java.util.Optional<Usuario> heredado = usuarioRepositorio.findByNombreUsuarioIgnoreCase("aperez");

        if (heredado.isPresent()) {
            Usuario usuario = heredado.get();
            usuario.setNombreUsuario(USUARIO_ADMINISTRADOR);
            usuario.setContrasena(passwordEncoder.encode(CONTRASENA_ADMINISTRADOR));
            usuarioRepositorio.save(usuario);
            registro.info("Usuario administrador migrado de aperez a {} (contrasena temporal de desarrollo)",
                    USUARIO_ADMINISTRADOR);
            return;
        }

        Usuario nuevoAdministrador = new Usuario();
        nuevoAdministrador.setNombreCompleto("Administrador");
        nuevoAdministrador.setNombreUsuario(USUARIO_ADMINISTRADOR);
        nuevoAdministrador.setCorreo("admin@boticasfamiliasalud.pe");
        // Contrasena temporal de desarrollo, almacenada cifrada con BCrypt.
        nuevoAdministrador.setContrasena(passwordEncoder.encode(CONTRASENA_ADMINISTRADOR));
        nuevoAdministrador.setEstado(EstadoUsuario.ACTIVO);
        nuevoAdministrador.setFechaRegistro(LocalDateTime.now());
        nuevoAdministrador.setRol(rolServicio.obtenerRolPorNombre(RolUsuario.ADMINISTRADOR));

        usuarioRepositorio.save(nuevoAdministrador);
        registro.info("Usuario administrador inicial creado: {} (contrasena temporal de desarrollo)",
                USUARIO_ADMINISTRADOR);
    }

    /**
     * Migra cualquier contrasena guardada en texto plano a un hash BCrypt.
     * Es idempotente: los hashes existentes se dejan intactos.
     */
    private void migrarContrasenasAbcrypt() {
        List<Usuario> usuarios = usuarioRepositorio.findAll();
        int migradas = 0;

        for (Usuario usuario : usuarios) {
            String contrasena = usuario.getContrasena();
            if (contrasena != null && !contrasena.startsWith(PREFIJO_BCRYPT)) {
                usuario.setContrasena(passwordEncoder.encode(contrasena));
                usuarioRepositorio.save(usuario);
                migradas++;
            }
        }

        if (migradas > 0) {
            registro.info("Contrasenas migradas a BCrypt: {}", migradas);
        }
    }

    private void crearProductosIniciales() {
        List<Producto> productosIniciales = List.of(
                crearProducto("MED-001", "Paracetamol 500 mg", "Analgesicos", "5.00", 50, 5),
                crearProducto("MED-002", "Ibuprofeno 400 mg", "Analgesicos", "8.50", 35, 5),
                crearProducto("MED-003", "Amoxicilina 500 mg", "Antibioticos", "18.00", 20, 5),
                crearProducto("MED-004", "Omeprazol 20 mg", "Digestivos", "12.50", 25, 5),
                crearProducto("MED-005", "Loratadina 10 mg", "Antialergicos", "9.00", 30, 5),
                crearProducto("JAR-001", "Jarabe para la tos", "Jarabes", "16.50", 15, 5),
                crearProducto("HIG-001", "Alcohol 70%", "Higiene", "7.00", 40, 5),
                crearProducto("HIG-002", "Agua oxigenada", "Higiene", "6.50", 18, 5),
                crearProducto("MAT-001", "Algodon", "Primeros auxilios", "4.50", 60, 10),
                crearProducto("MAT-002", "Curitas", "Primeros auxilios", "3.50", 45, 10),
                crearProducto("MAT-003", "Gasas esteriles", "Primeros auxilios", "6.00", 30, 5),
                crearProducto("DIS-001", "Termometro digital", "Dispositivos", "25.00", 8, 2),
                crearProducto("SOL-001", "Suero fisiologico", "Soluciones", "5.50", 22, 5),
                crearProducto("VIT-001", "Vitamina C", "Vitaminas", "14.00", 28, 5),
                crearProducto("VIT-002", "Multivitaminico", "Vitaminas", "22.00", 16, 5),
                crearProducto("HIG-003", "Gel antibacterial", "Higiene", "8.00", 32, 5),
                crearProducto("DER-001", "Protector solar", "Dermatologia", "35.00", 12, 3),
                crearProducto("DER-002", "Crema antiseptica", "Dermatologia", "11.50", 19, 5),
                crearProducto("GRA-001", "Pastillas para la garganta", "Respiratorio", "7.50", 40, 5),
                crearProducto("SOL-002", "Sales de rehidratacion oral", "Soluciones", "4.00", 26, 5)
        );

        int creados = 0;
        for (Producto producto : productosIniciales) {
            if (productoRepositorio.existsByCodigoIgnoreCase(producto.getCodigo())) {
                continue;
            }
            producto.setFechaRegistro(LocalDateTime.now());
            productoRepositorio.save(producto);
            creados++;
        }
        registro.info("Productos iniciales creados: {}/{}", creados, productosIniciales.size());
    }

    private Producto crearProducto(String codigo, String nombre, String categoria,
                                   String precio, Integer stock, Integer stockMinimo) {
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setPrecio(new BigDecimal(precio));
        producto.setStock(stock);
        producto.setStockMinimo(stockMinimo);
        producto.setEstado(EstadoProducto.ACTIVO);
        return producto;
    }

    /**
     * Crea clientes de demostracion solo si no existen (idempotente por nombre;
     * no modifica clientes que ya existen).
     */
    private void crearClientesIniciales() {
        List<Cliente> clientesIniciales = List.of(
                crearCliente("Maria Fernandez Lopez", "20456789123", "987654321",
                        "maria.fernandez@gmail.com", "Jr. Los Lirios 220, Lima"),
                crearCliente("Carlos Quispe Ramos", "45678912", "999888777",
                        "carlos.qr@hotmail.com", "Av. San Martin 450, Lima"),
                crearCliente("Rosa Mamani Torres", "10203040", "988777666",
                        null, null),
                crearCliente("Jose Luis Paredes", "31313131", "977666555",
                        "jl.paredes@outlook.com", "Calle Las Flores 800, Callao"),
                crearCliente("Ana Gutierrez Silva", "51416170", "966555444",
                        "ana.gutierrez@gmail.com", null),
                crearCliente("Farmacia Santa Ana S.R.L.", "20222555111", "(01) 455-8899",
                        "contacto@santaanafarmacia.pe", "Av. Colonial 1230, Lima"),
                crearCliente("Jorge Rios Castillo", "09401840", "955444333",
                        null, "Mz. H Lt. 12, Villa El Salvador"),
                crearCliente("Lucia Chambilla Diaz", "41750970", "944333222",
                        "lucia.chambilla@yahoo.com", "Jr. Union 56, Lima")
        );

        int creados = 0;
        for (Cliente cliente : clientesIniciales) {
            if (clienteRepositorio.existsByNombreIgnoreCase(cliente.getNombre())) {
                continue;
            }
            cliente.setFechaRegistro(LocalDateTime.now());
            clienteRepositorio.save(cliente);
            creados++;
        }
        registro.info("Clientes iniciales creados: {}/{}", creados, clientesIniciales.size());
    }

    private Cliente crearCliente(String nombre, String documento, String telefono,
                                 String correo, String direccion) {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setDocumento(documento);
        cliente.setTelefono(telefono);
        cliente.setCorreo(correo);
        cliente.setDireccion(direccion);
        cliente.setEstado(EstadoCliente.ACTIVO);
        return cliente;
    }

    private void crearConfiguracionInicial() {
        if (configuracionRepositorio.findTopByOrderByIdConfiguracionAsc().isPresent()) {
            registro.info("Configuracion del sistema ya existe");
            return;
        }

        ConfiguracionBotica configuracion = new ConfiguracionBotica();
        configuracion.setNombreComercial("BOTICAS FAMILIA & SALUD");
        configuracion.setRazonSocial("Boticas Familia y Salud S.A.C.");
        configuracion.setRuc("20123456789");
        configuracion.setDireccion("Av. Principal 123, Lima");
        configuracion.setTelefono("(01) 555-1234");
        configuracion.setCorreo("contacto@boticasfamiliasalud.pe");
        configuracion.setHorario("Lun - Dom: 8:00 a. m. - 10:00 p. m.");
        configuracion.setLogo(null);
        configuracion.setTema("claro");
        configuracion.setFormatoFecha("dd/mm/aaaa");
        configuracion.setFormatoHora("12");
        configuracion.setNotificacionesActivas(false);
        configuracion.setConfirmarCancelacionVenta(true);
        configuracion.setAplicarIgv(false);
        configuracion.setPorcentajeIgv(new BigDecimal("18"));
        configuracion.setMetodoPagoPredeterminado("efectivo");
        configuracion.setClienteGenerico(true);
        configuracion.setMostrarComprobanteAuto(true);
        configuracion.setStockMinimo(5);

        configuracionRepositorio.save(configuracion);
        registro.info("Configuracion del sistema inicial creada");
    }
}