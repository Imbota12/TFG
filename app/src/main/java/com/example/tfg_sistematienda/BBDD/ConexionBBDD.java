package com.example.tfg_sistematienda.BBDD;

import android.os.StrictMode;

import com.example.tfg_sistematienda.modelos.LogModel;
import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.modelos.Producto_TicketModel;
import com.example.tfg_sistematienda.modelos.TicketModel;
import com.example.tfg_sistematienda.modelos.TiendaModel;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConexionBBDD {

    private static final String DRIVER = "org.postgresql.Driver";
    //private static final String URL = "jdbc:postgresql://192.168.10.150:5432/TiendaInfo";
    private static final String URL = "jdbc:postgresql://10.0.2.2:5432/TiendaInfo";
    private static final String USUARIO = "postgres";
    private static final String PASSWORD = "admin";
    private Connection conexion = null;

    public void conectarBD() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName(DRIVER);
            conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    protected void cerrarConnection(Connection conexion) throws Exception {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void crearBaseDeDatosSiNoExiste() {
        Connection conexion = null;
        final String DRIVER1 = "org.postgresql.Driver";
        final String URL1 = "jdbc:postgresql://10.0.2.2:5432/";
        //final String URL1 = "jdbc:postgresql://192.168.10.150:5432/";
        final String DATABASE_NAME1 = "TiendaInfo";
        final String USUARIO1 = "postgres";
        final String PASSWORD1 = "admin";

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName(DRIVER1);
            conexion = DriverManager.getConnection(URL1, USUARIO1, PASSWORD1);

            if (conexion != null) {
                try (Statement statement = conexion.createStatement()) {
                    String checkQuery = "SELECT datname FROM pg_database WHERE datname = '" + DATABASE_NAME1 + "'";
                    if (!statement.executeQuery(checkQuery).next()) {
                        String createQuery = "CREATE DATABASE " + DATABASE_NAME1;
                        statement.executeUpdate(createQuery);
                    } else {
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conexion != null) {
                    conexion.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void crearTablas() {
        Statement statement = null;

        try {
            // Conectar a la base de datos
            conectarBD();
            statement = conexion.createStatement();

            // Crear un array de consultas SQL para crear las tablas
            String[] queries = {
                    "CREATE TABLE IF NOT EXISTS tienda (" +
                            "cif VARCHAR PRIMARY KEY, " +
                            "nombre VARCHAR NOT NULL, " +
                            "direccion VARCHAR NOT NULL, " +
                            "telefono VARCHAR NOT NULL" +
                            ");",

                    "CREATE TABLE IF NOT EXISTS usuario (" +
                            "dni VARCHAR PRIMARY KEY, " +
                            "nombre VARCHAR NOT NULL, " +
                            "apellido VARCHAR NOT NULL, " +
                            "telefono VARCHAR NOT NULL, " +
                            "correo VARCHAR NOT NULL, " +
                            "activo BOOLEAN NOT NULL, " +
                            "contrasena VARCHAR NOT NULL, " +
                            "id_tienda VARCHAR REFERENCES tienda(cif), " +
                            "is_admin BOOLEAN NOT NULL, " +
                            "is_vendedor BOOLEAN NOT NULL, " +
                            "is_reponedor BOOLEAN NOT NULL" +
                            ");",

                    "CREATE TABLE IF NOT EXISTS producto (" +
                            "codigo_barras VARCHAR PRIMARY KEY, " +
                            "nombre VARCHAR NOT NULL, " +
                            "descripcion TEXT, " +
                            "cantidad_stock INTEGER NOT NULL, " +
                            "precio_unidad DOUBLE PRECISION NOT NULL, " +
                            "veces_comprado INTEGER NOT NULL, " +
                            "veces_devuelto INTEGER NOT NULL, " +
                            "imagen_producto BYTEA, " +
                            "id_tienda VARCHAR REFERENCES tienda(cif)" +
                            ");",

                    "CREATE TABLE IF NOT EXISTS ticket (" +
                            "codigo_barras_ticket VARCHAR PRIMARY KEY, " +
                            "total_precio DOUBLE PRECISION NOT NULL, " +
                            "fecha_ticket DATE NOT NULL, " +
                            "fecha_limite_devolucion DATE NOT NULL, " +
                            "isdevolucion BOOLEAN NOT NULL, " +
                            "isventa BOOLEAN NOT NULL, " +
                            "entregado DOUBLE PRECISION NOT NULL, " +
                            "devuelto DOUBLE PRECISION NOT NULL, " +
                            "cif VARCHAR REFERENCES tienda(cif)" +
                            ");",

                    "CREATE TABLE IF NOT EXISTS ticket_producto (" +
                            "codigo_barras VARCHAR REFERENCES producto(codigo_barras), " +
                            "codigo_barras_ticket VARCHAR REFERENCES ticket(codigo_barras_ticket), " +
                            "cantidad DOUBLE PRECISION NOT NULL, " +
                            "PRIMARY KEY (codigo_barras, codigo_barras_ticket)" +
                            ");",

                    "CREATE TABLE IF NOT EXISTS logs (" +
                            "id_log SERIAL PRIMARY KEY, " +
                            "accion VARCHAR NOT NULL, " +
                            "fecha_y_hora TIMESTAMP WITHOUT TIME ZONE NOT NULL, " +
                            "dni VARCHAR REFERENCES usuario(dni)" +
                            ");"
            };

            // Ejecutar cada consulta para crear las tablas
            for (String query : queries) {
                statement.executeUpdate(query);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (statement != null) statement.close();
                if (conexion != null) cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public boolean insertarUsuario(String dni, String nombre, String apellido, String telefono,
                                   String correo, String contrasena, String idTienda,
                                   boolean isAdmin, boolean isVendedor, boolean isReponedor) {
        boolean insertarOK = false;
        conectarBD();
        try {
            String query = "INSERT INTO Usuario (dni, nombre, apellido, telefono, correo, activo, contrasena, id_tienda, is_admin, is_vendedor, is_reponedor) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, dni);
            preparedStatement.setString(2, nombre);
            preparedStatement.setString(3, apellido);
            preparedStatement.setString(4, telefono);
            preparedStatement.setString(5, correo);
            preparedStatement.setBoolean(6, true); // Establecer activo como verdadero por defecto
            preparedStatement.setString(7, contrasena);
            preparedStatement.setString(8, idTienda);
            preparedStatement.setBoolean(9, isAdmin);
            preparedStatement.setBoolean(10, isVendedor);
            preparedStatement.setBoolean(11, isReponedor);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            insertarOK = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return insertarOK;
    }


    public boolean insertarTienda(String cif, String nombre, String direccion, String telefono) {
        boolean insertarOK = false;
        conectarBD();
        try {
            String query = "INSERT INTO Tienda (cif, nombre, direccion, telefono) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, cif);
            preparedStatement.setString(2, nombre);
            preparedStatement.setString(3, direccion);
            preparedStatement.setString(4, telefono);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            insertarOK = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return insertarOK;
    }

    public boolean comprobarRegistrosTienda() {
        boolean hayRegistros = false;
        conectarBD();
        try {
            String checkQuery = "SELECT COUNT(*) FROM tienda";
            PreparedStatement preparedStatement = conexion.prepareStatement(checkQuery);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) > 0) {
                hayRegistros = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hayRegistros;

    }

    public boolean insertarProducto(String codigoBarras, String nombre, String descripcion, int cantidadStock,
                                    double precioUnidad, int vecesComprado, int vecesDevuelto, byte[] imagenProducto,
                                    String idTienda) {
        boolean insertarOK = false;
        conectarBD();
        try {
            String query = "INSERT INTO Producto (codigo_barras, nombre, descripcion, cantidad_stock, precio_unidad, " +
                    "veces_comprado, veces_devuelto, imagen_producto, id_tienda) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, codigoBarras);
            preparedStatement.setString(2, nombre);
            preparedStatement.setString(3, descripcion);
            preparedStatement.setInt(4, cantidadStock);
            preparedStatement.setDouble(5, precioUnidad);
            preparedStatement.setInt(6, vecesComprado);
            preparedStatement.setInt(7, vecesDevuelto);
            preparedStatement.setBytes(8, imagenProducto);
            preparedStatement.setString(9, idTienda);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            insertarOK = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return insertarOK;
    }

    public boolean modificarStockProducto(String codigoBarras, int cantidad) {
        boolean modificacionOK = false;
        conectarBD();
        try {
            // Construir la consulta SQL para actualizar el stock del producto
            String query = "UPDATE producto SET cantidad_stock = cantidad_stock - ? WHERE codigo_barras = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setInt(1, cantidad); // La cantidad puede ser positiva (para incrementar el stock) o negativa (para reducir el stock)
            preparedStatement.setString(2, codigoBarras);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            modificacionOK = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return modificacionOK;
    }


    public boolean insertarTicket(TicketModel ticket) {
        boolean insertarOK = false;
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            // Construir la consulta SQL para insertar un nuevo ticket
            String query = "INSERT INTO ticket (codigo_barras_ticket, total_precio, fecha_ticket, fecha_limite_devolucion, isdevolucion, isventa, entregado, devuelto, cif) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, ticket.getCodigo_barras_ticket());
            preparedStatement.setDouble(2, ticket.getTotal_precio());
            preparedStatement.setDate(3, java.sql.Date.valueOf(String.valueOf(ticket.getFecha_ticket())));
            preparedStatement.setDate(4, java.sql.Date.valueOf(String.valueOf(ticket.getFecha_limite_devolucion())));
            preparedStatement.setBoolean(5, ticket.isDevolucion());
            preparedStatement.setBoolean(6, ticket.isVenta());
            preparedStatement.setDouble(7, ticket.getEntregado());
            preparedStatement.setDouble(8, ticket.getDevuelto());
            preparedStatement.setString(9, ticket.getId_ticket());

            preparedStatement.executeUpdate();
            preparedStatement.close();
            insertarOK = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return insertarOK;
    }

    public boolean insertarProductoTicket(Producto_TicketModel productoTicket) {
        boolean insertarOK = false;
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            // Construir la consulta SQL para insertar un nuevo producto en el ticket
            String query = "INSERT INTO ticket_producto (codigo_barras, codigo_barras_ticket, cantidad) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, productoTicket.getCodigoBarras_producto());
            preparedStatement.setString(2, productoTicket.getCodigoBarras_ticket());
            preparedStatement.setInt(3, productoTicket.getCantidad());

            preparedStatement.executeUpdate();
            preparedStatement.close();
            insertarOK = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return insertarOK;
    }


    public boolean modificarProducto(String nombreNuevo, String descripcionNueva,
                                     double precioUnidadNuevo, byte[] imagenNueva, String codigoBarras) {
        boolean modificarOK = false;
        conectarBD();
        try {
            String query = "UPDATE producto SET nombre = ?, descripcion = ?, precio_unidad = ?, imagen_producto = ? WHERE codigo_barras = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, nombreNuevo);
            preparedStatement.setString(2, descripcionNueva);
            preparedStatement.setDouble(3, precioUnidadNuevo);
            preparedStatement.setBytes(4, imagenNueva);
            preparedStatement.setString(5, codigoBarras);
            int filasModificadas = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (filasModificadas > 0) {
                modificarOK = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return modificarOK;
    }


    public boolean modificarTienda(String cif, String nombreNuevo,
                                   String direccionNueva, String telefonoNuevo) {
        boolean modificarOK = false;
        conectarBD();
        try {
            String query = "UPDATE tienda SET nombre = ?, direccion = ?, telefono = ? WHERE cif = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, nombreNuevo);
            preparedStatement.setString(2, direccionNueva);
            preparedStatement.setString(3, telefonoNuevo);
            preparedStatement.setString(4, cif);

            int filasModificadas = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (filasModificadas > 0) {
                modificarOK = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return modificarOK;
    }


    public boolean borrarProducto(String codigoBarras) {
        boolean borrarOK = false;
        conectarBD();
        try {
            String query = "DELETE FROM producto WHERE codigo_barras = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, codigoBarras);
            int filasBorradas = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (filasBorradas > 0) {
                borrarOK = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return borrarOK;
    }

    public boolean borrarTienda(String cifTienda) {
        boolean borrarOK = false;
        conectarBD();
        try {
            String query = "DELETE FROM tienda WHERE cif = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, cifTienda);
            int filasBorradas = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (filasBorradas > 0) {
                borrarOK = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return borrarOK;
    }


    public boolean incrementarCantidadStock(String codigoBarras) {
        boolean modificarOK = false;
        conectarBD();
        try {
            // Consulta SQL para incrementar la cantidad de stock en 1 unidad
            String query = "UPDATE producto SET cantidad_stock = cantidad_stock + 1 WHERE codigo_barras = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, codigoBarras);
            int filasModificadas = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (filasModificadas > 0) {
                modificarOK = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return modificarOK;
    }

    public boolean incrementarVecesDevuelto(String codigoBarras, int vecesDevuelto) {
        boolean modificarOK = false;
        conectarBD();
        try {
            String query = "UPDATE producto SET veces_devuelto = veces_devuelto + " + vecesDevuelto + " WHERE codigo_barras = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, codigoBarras);
            int filasModificadas = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (filasModificadas > 0) {
                modificarOK = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return modificarOK;
    }

    public boolean incrementarVecesComprado(String codigoBarras, int vecesComprado) {
        boolean modificarOK = false;
        conectarBD();
        try {
            String query = "UPDATE producto SET veces_comprado = veces_comprado + " + vecesComprado + " WHERE codigo_barras = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, codigoBarras);
            int filasModificadas = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (filasModificadas > 0) {
                modificarOK = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return modificarOK;
    }

    public boolean decrementarCantidadStock(String codigoBarras) {
        boolean modificarOK = false;
        conectarBD();
        try {
            // Consulta SQL para decrementar la cantidad de stock en 1 unidad
            String query = "UPDATE producto SET cantidad_stock = cantidad_stock - 1 WHERE codigo_barras = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, codigoBarras);
            int filasModificadas = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (filasModificadas > 0) {
                modificarOK = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return modificarOK;
    }


    public List<TiendaModel> obtenerListaTiendas() {
        List<TiendaModel> listaTiendas = new ArrayList<>();
        conectarBD();

        try {
            String query = "SELECT cif, nombre, direccion, telefono FROM Tienda";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String cif = resultSet.getString("cif");
                String nombre = resultSet.getString("nombre");
                String direccion = resultSet.getString("direccion");
                String telefono = resultSet.getString("telefono");

                TiendaModel tienda = new TiendaModel(cif, nombre, direccion, telefono);
                listaTiendas.add(tienda);
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaTiendas;
    }


    public List<String> obtenerListaCorreos() {
        List<String> listaCorreos = new ArrayList<>();
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            String query = "SELECT correo FROM usuario";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String correo = resultSet.getString("correo");
                listaCorreos.add(correo);
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaCorreos;
    }


    public UsuarioModel buscarUsuario(String nombre, String apellidos, String dni, String telefono) {
        UsuarioModel usuarioEncontrado = null;
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            // Construir la consulta SQL con los parámetros proporcionados
            String query = "SELECT * FROM usuario WHERE nombre = ? AND apellido = ? AND dni = ? AND telefono = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, nombre);
            preparedStatement.setString(2, apellidos);
            preparedStatement.setString(3, dni);
            preparedStatement.setString(4, telefono);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Si se encuentra al menos un resultado, el usuario existe
            if (resultSet.next()) {
                // Crear un objeto UsuarioModel con los datos del resultado
                usuarioEncontrado = new UsuarioModel(
                        resultSet.getString("dni"),
                        resultSet.getString("nombre"),
                        resultSet.getString("apellido"),
                        resultSet.getString("telefono"),
                        resultSet.getString("correo"),
                        resultSet.getBoolean("activo"),
                        resultSet.getString("contrasena"),
                        resultSet.getString("id_tienda"),
                        resultSet.getBoolean("is_admin"),
                        resultSet.getBoolean("is_vendedor"),
                        resultSet.getBoolean("is_reponedor")
                );
            }


            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return usuarioEncontrado;
    }

    public boolean actualizarUsuario(UsuarioModel usuarioActualizado) {
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            // Construir la consulta SQL para actualizar el usuario
            String query = "UPDATE usuario SET nombre = ?, apellido = ?, telefono = ?, correo = ?, activo = ?, contrasena = ?, id_tienda = ?, is_admin = ?, is_vendedor = ?, is_reponedor = ? WHERE dni = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            // Establecer los valores de los parámetros en la consulta SQL
            preparedStatement.setString(1, usuarioActualizado.getNombre());
            preparedStatement.setString(2, usuarioActualizado.getApellido());
            preparedStatement.setString(3, usuarioActualizado.getTelefono());
            preparedStatement.setString(4, usuarioActualizado.getCorreo());
            preparedStatement.setBoolean(5, usuarioActualizado.isActivo());
            preparedStatement.setString(6, usuarioActualizado.getContrasena());
            preparedStatement.setString(7, usuarioActualizado.getIdTienda());
            preparedStatement.setBoolean(8, usuarioActualizado.isAdmin());
            preparedStatement.setBoolean(9, usuarioActualizado.isVendedor());
            preparedStatement.setBoolean(10, usuarioActualizado.isReponedor());
            preparedStatement.setString(11, usuarioActualizado.getDni());

            // Ejecutar la consulta SQL para actualizar el usuario
            int filasActualizadas = preparedStatement.executeUpdate();

            // Verificar si se actualizaron filas (es decir, si el usuario se actualizó correctamente)
            if (filasActualizadas > 0) {
                return true; // El usuario se actualizó correctamente
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false; // La actualización del usuario falló
    }


    public String obtenerContraseñaPorCorreo(String correo) {
        String contraseña = null;
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            // Construir la consulta SQL con el correo proporcionado
            String query = "SELECT contrasena FROM usuario WHERE correo = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, correo);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Si se encuentra al menos un resultado, se obtiene la contraseña
            if (resultSet.next()) {
                contraseña = resultSet.getString("contrasena");
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return contraseña;
    }

    public UsuarioModel buscarUsuarioPorCorreo(String correo) {
        UsuarioModel usuarioEncontrado = null;
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            // Construir la consulta SQL con el correo proporcionado
            String query = "SELECT * FROM usuario WHERE correo = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, correo);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Si se encuentra al menos un resultado, el usuario existe
            if (resultSet.next()) {
                // Crear un objeto UsuarioModel con los datos del resultado
                usuarioEncontrado = new UsuarioModel(
                        resultSet.getString("dni"),
                        resultSet.getString("nombre"),
                        resultSet.getString("apellido"),
                        resultSet.getString("telefono"),
                        resultSet.getString("correo"),
                        resultSet.getBoolean("activo"),
                        resultSet.getString("contrasena"),
                        resultSet.getString("id_tienda"),
                        resultSet.getBoolean("is_admin"),
                        resultSet.getBoolean("is_vendedor"),
                        resultSet.getBoolean("is_reponedor")
                );
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return usuarioEncontrado;
    }

    public UsuarioModel buscarUsuarioPorDni(String dni) {
        UsuarioModel usuarioEncontrado = null;
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            // Construir la consulta SQL con el correo proporcionado
            String query = "SELECT * FROM usuario WHERE dni = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, dni);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Si se encuentra al menos un resultado, el usuario existe
            if (resultSet.next()) {
                // Crear un objeto UsuarioModel con los datos del resultado
                usuarioEncontrado = new UsuarioModel(
                        resultSet.getString("dni"),
                        resultSet.getString("nombre"),
                        resultSet.getString("apellido"),
                        resultSet.getString("telefono"),
                        resultSet.getString("correo"),
                        resultSet.getBoolean("activo"),
                        resultSet.getString("contrasena"),
                        resultSet.getString("id_tienda"),
                        resultSet.getBoolean("is_admin"),
                        resultSet.getBoolean("is_vendedor"),
                        resultSet.getBoolean("is_reponedor")
                );
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return usuarioEncontrado;
    }


    public List<String> obtenerListaCodigosBarras() {
        List<String> listaBarras = new ArrayList<>();
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            String query = "SELECT codigo_barras FROM producto";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String codigo = resultSet.getString("codigo_barras");
                listaBarras.add(codigo);
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaBarras;
    }


    public List<String> obtenerListaCodigosTicket() {
        List<String> listaCodigosTicket = new ArrayList<>();
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            String query = "SELECT DISTINCT codigo_barras_ticket FROM ticket";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String id = resultSet.getString("codigo_barras_ticket");
                listaCodigosTicket.add(id);
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaCodigosTicket;
    }

    public double obtenerPrecioUnidadporCodigo(String codigoBarras) {
        double precioUnidad = 0;
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            String query = "SELECT precio_unidad FROM producto WHERE codigo_barras = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, codigoBarras); // Establece el código de barras en la consulta
            ResultSet resultSet = preparedStatement.executeQuery();

            // Verifica si se encontró un resultado
            if (resultSet.next()) {
                precioUnidad = resultSet.getDouble("precio_unidad");
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return precioUnidad;
    }

    public int obtenerStockporCodigo(String codigoBarras) {
        int cantidadStock = 0;
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            String query = "SELECT cantidad_stock FROM producto WHERE codigo_barras = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, codigoBarras); // Establece el código de barras en la consulta
            ResultSet resultSet = preparedStatement.executeQuery();

            // Verifica si se encontró un resultado
            if (resultSet.next()) {
                cantidadStock = resultSet.getInt("cantidad_stock");
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return cantidadStock;
    }


    public List<String> obtenerListaCIF() {
        List<String> listaCif = new ArrayList<>();
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            String query = "SELECT cif FROM tienda";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String CIF = resultSet.getString("cif");
                listaCif.add(CIF);
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaCif;
    }

    public List<LogModel> obtenerListaLogs() {
        List<LogModel> listaLogs = new ArrayList<>();
        conectarBD();

        try {
            String query = "SELECT accion, fecha_y_hora, dni FROM logs";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                LogModel log = new LogModel();
                log.setFecha(resultSet.getDate("fecha_y_hora"));
                log.setAccion(resultSet.getString("accion"));
                log.setDni(resultSet.getString("dni"));

                listaLogs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaLogs;
    }

    public List<LogModel> obtenerListaLogsPorDni(String dni) {
        List<LogModel> listaLogs = new ArrayList<>();
        conectarBD();

        try {
            String query = "SELECT accion, fecha_y_hora, dni FROM logs WHERE dni = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, dni);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                LogModel log = new LogModel();
                log.setFecha(resultSet.getDate("fecha_y_hora"));
                log.setAccion(resultSet.getString("accion"));
                log.setDni(resultSet.getString("dni"));

                listaLogs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaLogs;
    }


    public List<LogModel> obtenerListaLogsPorFechaIgual(LocalDate fecha) {
        List<LogModel> listaLogs = new ArrayList<>();
        conectarBD();

        try {
            String query = "SELECT accion, fecha_y_hora, dni FROM logs WHERE DATE(fecha_y_hora) = ? ";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            String fechaString = fecha.toString();
            java.sql.Date sqlDate = java.sql.Date.valueOf(fechaString);
            preparedStatement.setDate(1, sqlDate);


            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                LogModel log = new LogModel();
                log.setFecha(resultSet.getDate("fecha_y_hora"));
                log.setAccion(resultSet.getString("accion"));
                log.setDni(resultSet.getString("dni"));

                listaLogs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaLogs;
    }

    public List<LogModel> obtenerListaLogsPorFechaMayor(LocalDate fecha) {
        List<LogModel> listaLogs = new ArrayList<>();
        conectarBD();

        try {
            String query = "SELECT accion, fecha_y_hora, dni FROM logs WHERE DATE(fecha_y_hora) > ? ";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            String fechaString = fecha.toString();
            java.sql.Date sqlDate = java.sql.Date.valueOf(fechaString);
            preparedStatement.setDate(1, sqlDate);


            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                LogModel log = new LogModel();
                log.setFecha(resultSet.getDate("fecha_y_hora"));
                log.setAccion(resultSet.getString("accion"));
                log.setDni(resultSet.getString("dni"));

                listaLogs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaLogs;
    }

    public List<LogModel> obtenerListaLogsPorFechaMenor(LocalDate fecha) {
        List<LogModel> listaLogs = new ArrayList<>();
        conectarBD();

        try {
            String query = "SELECT accion, fecha_y_hora, dni FROM logs WHERE DATE(fecha_y_hora) < ? ";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            String fechaString = fecha.toString();
            java.sql.Date sqlDate = java.sql.Date.valueOf(fechaString);
            preparedStatement.setDate(1, sqlDate);


            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                LogModel log = new LogModel();
                log.setFecha(resultSet.getDate("fecha_y_hora"));
                log.setAccion(resultSet.getString("accion"));
                log.setDni(resultSet.getString("dni"));

                listaLogs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaLogs;
    }

    public List<LogModel> obtenerListaLogsPorFechaIgualconDNI(LocalDate fecha, String dni) {
        List<LogModel> listaLogs = new ArrayList<>();
        conectarBD();

        try {
            String query = "SELECT accion, fecha_y_hora, dni FROM logs WHERE DATE(fecha_y_hora) = ? AND dni = ? ";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            String fechaString = fecha.toString();
            java.sql.Date sqlDate = java.sql.Date.valueOf(fechaString);
            preparedStatement.setDate(1, sqlDate);
            preparedStatement.setString(2, dni);


            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                LogModel log = new LogModel();
                log.setFecha(resultSet.getDate("fecha_y_hora"));
                log.setAccion(resultSet.getString("accion"));
                log.setDni(resultSet.getString("dni"));

                listaLogs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaLogs;
    }

    public List<LogModel> obtenerListaLogsPorFechaMayorconDNI(LocalDate fecha, String dni) {
        List<LogModel> listaLogs = new ArrayList<>();
        conectarBD();

        try {
            String query = "SELECT accion, fecha_y_hora, dni FROM logs WHERE DATE(fecha_y_hora) > ? AND dni = ? ";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            String fechaString = fecha.toString();
            java.sql.Date sqlDate = java.sql.Date.valueOf(fechaString);
            preparedStatement.setDate(1, sqlDate);
            preparedStatement.setString(2, dni);


            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                LogModel log = new LogModel();
                log.setFecha(resultSet.getDate("fecha_y_hora"));
                log.setAccion(resultSet.getString("accion"));
                log.setDni(resultSet.getString("dni"));

                listaLogs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaLogs;
    }

    public List<LogModel> obtenerListaLogsPorFechaMenorconDNI(LocalDate fecha, String dni) {
        List<LogModel> listaLogs = new ArrayList<>();
        conectarBD();

        try {
            String query = "SELECT accion, fecha_y_hora, dni FROM logs WHERE DATE(fecha_y_hora) < ? AND dni = ? ";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            String fechaString = fecha.toString();
            java.sql.Date sqlDate = java.sql.Date.valueOf(fechaString);
            preparedStatement.setDate(1, sqlDate);
            preparedStatement.setString(2, dni);


            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                LogModel log = new LogModel();
                log.setFecha(resultSet.getDate("fecha_y_hora"));
                log.setAccion(resultSet.getString("accion"));
                log.setDni(resultSet.getString("dni"));

                listaLogs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaLogs;
    }

    public List<String> obtenerDnis() {
        List<String> listaDnis = new ArrayList<>();
        conectarBD();

        try {
            String query = "SELECT DISTINCT dni FROM usuario";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String dni = resultSet.getString("dni");
                listaDnis.add(dni);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaDnis;
    }

    public List<ProductoModel> obtenerListaProductos(String idTienda) {
        List<ProductoModel> listaProductos = new ArrayList<>();
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            String query = "SELECT codigo_barras, nombre, descripcion, cantidad_stock, precio_unidad, veces_comprado, veces_devuelto, imagen_producto, id_tienda FROM producto WHERE id_tienda = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, idTienda);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ProductoModel producto = new ProductoModel();
                producto.setCodigoBarras(resultSet.getString("codigo_barras"));
                producto.setNombre(resultSet.getString("nombre"));
                producto.setDescripcion(resultSet.getString("descripcion"));
                producto.setCantidadStock(resultSet.getInt("cantidad_stock"));
                producto.setPrecioUnidad(resultSet.getDouble("precio_unidad"));
                producto.setVecesComprado(resultSet.getInt("veces_comprado"));
                producto.setVecesDevuelto(resultSet.getInt("veces_devuelto"));
                producto.setImagenProducto(resultSet.getBytes("imagen_producto"));
                producto.setIdTienda(resultSet.getString("id_tienda"));

                listaProductos.add(producto);
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaProductos;
    }

    public List<ProductoModel> obtenerListaProductosparaDevo(String codigoTicket) {
        List<ProductoModel> listaProductos = new ArrayList<>();
        conectarBD();

        try {
            String query = "SELECT p.codigo_barras, p.nombre, p.descripcion, p.cantidad_stock, p.precio_unidad, " +
                    "p.veces_comprado, p.veces_devuelto, p.imagen_producto, p.id_tienda, " +
                    "pt.cantidad " +
                    "FROM producto p " +
                    "LEFT JOIN ticket_producto pt ON p.codigo_barras = pt.codigo_barras " +
                    "WHERE pt.codigo_barras_ticket = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, codigoTicket);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ProductoModel producto = new ProductoModel();
                producto.setCodigoBarras(resultSet.getString("codigo_barras"));
                producto.setNombre(resultSet.getString("nombre"));
                producto.setDescripcion(resultSet.getString("descripcion"));
                producto.setCantidadStock(resultSet.getInt("cantidad_stock"));
                producto.setPrecioUnidad(resultSet.getDouble("precio_unidad"));
                producto.setVecesComprado(resultSet.getInt("veces_comprado"));
                producto.setVecesDevuelto(resultSet.getInt("veces_devuelto"));
                producto.setImagenProducto(resultSet.getBytes("imagen_producto"));
                producto.setIdTienda(resultSet.getString("id_tienda"));
                producto.setCantidad(resultSet.getInt("cantidad"));

                listaProductos.add(producto);
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaProductos;
    }

    public boolean verificarExistenciaTicket(String codigoTicket, String idTienda) {
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        boolean existe = false;

        try {
            String query = "SELECT COUNT(*) FROM ticket WHERE codigo_barras_ticket = ? AND cif = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, codigoTicket);
            preparedStatement.setString(2, idTienda);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                existe = count > 0;
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return existe;
    }

    public void actualizarCantidadProducto(String codigoBarrasProducto, String codigoBarrasTicket, int nuevaCantidad) {
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            String query = "UPDATE ticket_producto SET cantidad = ? WHERE codigo_barras = ? AND codigo_barras_ticket = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setInt(1, nuevaCantidad);
            preparedStatement.setString(2, codigoBarrasProducto);
            preparedStatement.setString(3, codigoBarrasTicket);
            preparedStatement.executeUpdate();

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void actualizarEstadoEmpleado(String dni, boolean estado) {
        conectarBD(); // Establecer la conexión a la base de datos

        try {
            String query = "UPDATE usuario SET activo = ? WHERE dni = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setBoolean(1, estado);
            preparedStatement.setString(2, dni);
            preparedStatement.executeUpdate();

            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<UsuarioModel> obtenerListaEmpleados() {
        List<UsuarioModel> listaEmpleados = new ArrayList<>();
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            String query = "SELECT * FROM usuario WHERE is_admin = false";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                UsuarioModel empleado = new UsuarioModel();
                empleado.setDni(resultSet.getString("dni"));
                empleado.setNombre(resultSet.getString("nombre"));
                empleado.setApellido(resultSet.getString("apellido"));
                empleado.setTelefono(resultSet.getString("telefono"));
                empleado.setCorreo(resultSet.getString("correo"));
                empleado.setActivo(resultSet.getBoolean("activo"));
                empleado.setContrasena(resultSet.getString("contrasena"));
                empleado.setIdTienda(resultSet.getString("id_tienda"));
                empleado.setAdmin(resultSet.getBoolean("is_admin"));
                empleado.setVendedor(resultSet.getBoolean("is_vendedor"));
                empleado.setReponedor(resultSet.getBoolean("is_reponedor"));

                listaEmpleados.add(empleado);
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaEmpleados;
    }

    public boolean modificarEmpleadoConContra(String dni, String nombreNuevo, String apellidoNuevo,
                                              String correoNuevo, String telefonoNuevo, String nuevaContra) {
        boolean modificarOK = false;
        conectarBD();
        try {
            String query = "UPDATE usuario SET nombre = ?, apellido = ?, telefono = ?, correo = ?, contrasena = ? WHERE dni = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, nombreNuevo);
            preparedStatement.setString(2, apellidoNuevo);
            preparedStatement.setString(3, telefonoNuevo);
            preparedStatement.setString(4, correoNuevo);
            preparedStatement.setString(5, nuevaContra);
            preparedStatement.setString(6, dni);
            int filasModificadas = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (filasModificadas > 0) {
                modificarOK = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return modificarOK;
    }

    public boolean modificarEmpleadoSinContra(String dni, String nombreNuevo, String apellidoNuevo,
                                              String correoNuevo, String telefonoNuevo) {
        boolean modificarOK = false;
        conectarBD();
        try {
            String query = "UPDATE usuario SET nombre = ?, apellido = ?, telefono = ?, correo = ? WHERE dni = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, nombreNuevo);
            preparedStatement.setString(2, apellidoNuevo);
            preparedStatement.setString(3, telefonoNuevo);
            preparedStatement.setString(4, correoNuevo);
            preparedStatement.setString(5, dni);
            int filasModificadas = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (filasModificadas > 0) {
                modificarOK = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return modificarOK;
    }


    public UsuarioModel obtenerEmpleado(String dni) {
        UsuarioModel usuarioEncontrado = null;
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            // Construir la consulta SQL con los parámetros proporcionados
            String query = "SELECT * FROM usuario WHERE dni = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, dni);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Si se encuentra al menos un resultado, el usuario existe
            if (resultSet.next()) {
                // Crear un objeto UsuarioModel con los datos del resultado
                usuarioEncontrado = new UsuarioModel(
                        resultSet.getString("dni"),
                        resultSet.getString("nombre"),
                        resultSet.getString("apellido"),
                        resultSet.getString("telefono"),
                        resultSet.getString("correo"),
                        resultSet.getBoolean("activo"),
                        resultSet.getString("contrasena"),
                        resultSet.getString("id_tienda"),
                        resultSet.getBoolean("is_admin"),
                        resultSet.getBoolean("is_vendedor"),
                        resultSet.getBoolean("is_reponedor")
                );
            }


            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return usuarioEncontrado;
    }

    public List<ProductoModel> obtenerProductosMasVendidos() {
        List<ProductoModel> listaProductos = new ArrayList<>();
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            // Construir la consulta SQL con los parámetros proporcionados
            String query = "SELECT * FROM producto ORDER BY veces_comprado DESC LIMIT 5";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            ResultSet resultSet = preparedStatement.executeQuery();


            while (resultSet.next()) {
                ProductoModel producto = new ProductoModel();
                producto.setCodigoBarras(resultSet.getString("codigo_barras"));
                producto.setNombre(resultSet.getString("nombre"));
                producto.setDescripcion(resultSet.getString("descripcion"));
                producto.setCantidadStock(resultSet.getInt("cantidad_stock"));
                producto.setPrecioUnidad(resultSet.getDouble("precio_unidad"));
                producto.setVecesComprado(resultSet.getInt("veces_comprado"));
                producto.setVecesDevuelto(resultSet.getInt("veces_devuelto"));
                producto.setImagenProducto(resultSet.getBytes("imagen_producto"));
                producto.setIdTienda(resultSet.getString("id_tienda"));

                listaProductos.add(producto);

            }
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaProductos;
    }


    public List<ProductoModel> obtenerProductosMasDevueltos() {
        List<ProductoModel> listaProductos = new ArrayList<>();
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            // Construir la consulta SQL con los parámetros proporcionados
            String query = "SELECT * FROM producto ORDER BY veces_devuelto DESC LIMIT 5";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            ResultSet resultSet = preparedStatement.executeQuery();


            while (resultSet.next()) {
                ProductoModel producto = new ProductoModel();
                producto.setCodigoBarras(resultSet.getString("codigo_barras"));
                producto.setNombre(resultSet.getString("nombre"));
                producto.setDescripcion(resultSet.getString("descripcion"));
                producto.setCantidadStock(resultSet.getInt("cantidad_stock"));
                producto.setPrecioUnidad(resultSet.getDouble("precio_unidad"));
                producto.setVecesComprado(resultSet.getInt("veces_comprado"));
                producto.setVecesDevuelto(resultSet.getInt("veces_devuelto"));
                producto.setImagenProducto(resultSet.getBytes("imagen_producto"));
                producto.setIdTienda(resultSet.getString("id_tienda"));

                listaProductos.add(producto);

            }
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaProductos;
    }

    public boolean insertarLog(String accion, LocalDateTime fechaHora, String dniEmpleado) {
        boolean insertarOK = false;
        conectarBD();

        try {

            // Construir la consulta SQL para insertar un nuevo log
            String query = "INSERT INTO logs (accion, fecha_y_hora, dni) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            // Separar fecha y hora de LocalDateTime
            preparedStatement.setString(1, accion);
            String fechaHoraString = fechaHora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Pasar la cadena a Timestamp.valueOf()
            preparedStatement.setTimestamp(2, Timestamp.valueOf(fechaHoraString));

            preparedStatement.setString(3, dniEmpleado);


            // Ejecutar la consulta
            preparedStatement.executeUpdate();
            preparedStatement.close();
            insertarOK = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return insertarOK;
    }

    public String obtenerNombreTienda(String idTienda) {
        String nombreTienda = "";

        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            // Construir la consulta SQL con los parámetros proporcionados
            String query = "SELECT nombre FROM tienda WHERE cif = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, idTienda);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Si se encuentra al menos un resultado, el usuario existe
            if (resultSet.next()) {
                nombreTienda = resultSet.getString("nombre");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return nombreTienda;
    }

    public Date obtenerFechaLimiteDevolucion(String idTicket) {
        Date fechaLimite = Date.from(Instant.now());

        conectarBD();

        try {
            // Construir la consulta SQL con los parámetros proporcionados
            String query = "SELECT fecha_limite_devolucion FROM ticket WHERE codigo_barras_ticket = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, idTicket);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Si se encuentra al menos un resultado, el usuario existe
            if (resultSet.next()) {
                fechaLimite = resultSet.getDate("fecha_limite_devolucion");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fechaLimite;
    }


    public Date obtenerFechaTicket(String idTicket) {
        Date fechaTicket = Date.from(Instant.now());

        conectarBD();

        try {
            // Construir la consulta SQL con los parámetros proporcionados
            String query = "SELECT fecha_ticket FROM ticket WHERE codigo_barras_ticket = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, idTicket);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Si se encuentra al menos un resultado, el usuario existe
            if (resultSet.next()) {
                fechaTicket = resultSet.getDate("fecha_ticket");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fechaTicket;
    }

    public boolean vaciarLogs() {
        boolean vaciarOK = false;
        conectarBD();

        try {
            // Construir la consulta SQL con los parámetros proporcionados
            String query = "TRUNCATE TABLE logs RESTART IDENTITY";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.executeUpdate(); // Ejecuta la consulta de actualización

            vaciarOK = true; // Si llega aquí, la operación fue exitosa
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return vaciarOK;
    }

    public double obtenerSumaVentas() {
        double ventas = 0;
        conectarBD();

        try {
            String query = "SELECT SUM(total_precio) AS suma_total_precio\n" +
                    "FROM ticket\n" +
                    "WHERE isventa = TRUE";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                ventas = resultSet.getDouble("suma_total_precio");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ventas;
    }

    public double obtenerSumaDevoluciones() {
        double devoluciones = 0;
        conectarBD();
        try {
            String query = "SELECT SUM(total_precio) AS suma_total_devo\n" +
                    "FROM ticket\n" +
                    "WHERE isdevolucion = TRUE";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                devoluciones = resultSet.getDouble("suma_total_devo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cerrarConnection(conexion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return devoluciones;
    }

}




