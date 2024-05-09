package com.example.tfg_sistematienda.BBDD;

import android.os.StrictMode;

import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.modelos.Producto_TicketModel;
import com.example.tfg_sistematienda.modelos.TicketModel;
import com.example.tfg_sistematienda.modelos.TiendaModel;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ConexionBBDD {

    private Connection conexion = null;
    private static final String DRIVER = "org.postgresql.Driver";
    private static final String URL = "jdbc:postgresql://192.168.129.244:5432/TiendaInfo";
    //private static final String URL = "jdbc:postgresql://10.0.2.2:5432/TiendaInfo";
    private static final String USUARIO = "postgres";
    private static final String PASSWORD = "admin";


    public void conectarBD(){
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName(DRIVER);
            conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    protected void cerrarConnection(Connection conexion) throws Exception {
        if (conexion != null){
            try {
                conexion.close();
            }catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void crearBaseDeDatosSiNoExiste() {
        Connection conexion = null;
        final String DRIVER1 = "org.postgresql.Driver";
        //final String URL1 = "jdbc:postgresql://10.0.2.2:5432/";
        final String URL1 = "jdbc:postgresql://192.168.129.244:5432/";
        final String DATABASE_NAME1 = "TFG";
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
        conectarBD();
        try {
            Statement statement = conexion.createStatement();
            String[] queries = {
                    "CREATE TABLE IF NOT EXISTS Tienda (" +
                            "cif VARCHAR(20) PRIMARY KEY," +
                            "nombre VARCHAR(255)," +
                            "direccion VARCHAR(255)," +
                            "logo VARCHAR(255)," +
                            "telefono VARCHAR(20)" +
                            ")",
                    "CREATE TABLE IF NOT EXISTS Usuario (" +
                            "dni VARCHAR(20) PRIMARY KEY," +
                            "nombre VARCHAR(255)," +
                            "apellido VARCHAR(255)," +
                            "telefono VARCHAR(20)," +
                            "correo VARCHAR(255)," +
                            "activo BOOLEAN," +
                            "contraseña VARCHAR(255)," +
                            "id_tienda VARCHAR(20)," +
                            "FOREIGN KEY (id_tienda) REFERENCES Tienda(cif)" +
                            ")",
                    "CREATE TABLE IF NOT EXISTS Admin (" +
                            "id_usuario VARCHAR(20) PRIMARY KEY," +
                            "isAdmin BOOLEAN," +
                            "FOREIGN KEY (id_usuario) REFERENCES Usuario(dni)" +
                            ")",
                    "CREATE TABLE IF NOT EXISTS Vendedor (" +
                            "id_usuario VARCHAR(20) PRIMARY KEY," +
                            "numero_clientes_atendidos INT," +
                            "is_vendedor BOOLEAN," +
                            "FOREIGN KEY (id_usuario) REFERENCES Usuario(dni)" +
                            ")",
                    "CREATE TABLE IF NOT EXISTS Reponedor (" +
                            "id_usuario VARCHAR(20) PRIMARY KEY," +
                            "numero_productos_repuestos INT," +
                            "is_reponedor BOOLEAN," +
                            "FOREIGN KEY (id_usuario) REFERENCES Usuario(dni)" +
                            ")",
                    "CREATE TABLE IF NOT EXISTS Producto (" +
                            "codigo_barras VARCHAR(50) PRIMARY KEY," +
                            "nombre VARCHAR(255)," +
                            "descripcion TEXT," +
                            "cantidad_stock INT," +
                            "precio_unidad DECIMAL(10, 2)," +
                            "veces_comprado INT," +
                            "veces_devuelto INT," +
                            "imagen_producto VARCHAR(255)" +
                            ")",
                    "CREATE TABLE IF NOT EXISTS Logs (" +
                            "id_log INT PRIMARY KEY," +
                            "accion VARCHAR(255)," +
                            "fecha_y_hora TIMESTAMP," +
                            "dni VARCHAR(20)," +
                            "FOREIGN KEY (dni) REFERENCES Usuario(dni)" +
                            ")",
                    "CREATE TABLE IF NOT EXISTS Ticket (" +
                            "id_ticket SERIAL PRIMARY KEY," +
                            "total_precio DECIMAL(10, 2)," +
                            "entregado BOOLEAN," +
                            "devuelto BOOLEAN," +
                            "fecha_ticket DATE," +
                            "codigo_barras_ticket VARCHAR(50)," +
                            "fechalimiteDevolucion DATE," +
                            "isDevolucion BOOLEAN," +
                            "isVenta BOOLEAN" +
                            ")",
                    "CREATE TABLE IF NOT EXISTS Ticket_Producto (" +
                            "id_ticket INT," +
                            "codigo_barras VARCHAR(50)," +
                            "cantidad INT," +
                            "PRIMARY KEY (id_ticket, codigo_barras)," +
                            "FOREIGN KEY (id_ticket) REFERENCES Ticket(id_ticket)," +
                            "FOREIGN KEY (codigo_barras) REFERENCES Producto(codigo_barras)" +
                            ")",
                    // Relaciones adicionales
                    "ALTER TABLE Admin ADD CONSTRAINT fk_admin_usuario FOREIGN KEY (id_usuario) REFERENCES Usuario(dni)",
                    "ALTER TABLE Vendedor ADD CONSTRAINT fk_vendedor_usuario FOREIGN KEY (id_usuario) REFERENCES Usuario(dni)",
                    "ALTER TABLE Reponedor ADD CONSTRAINT fk_reponedor_usuario FOREIGN KEY (id_usuario) REFERENCES Usuario(dni)"
            };
            for (String query : queries) {
                statement.executeUpdate(query);
            }
            statement.close();
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

    public boolean insertarUsuario(String dni, String nombre, String apellido, String telefono,
                                   String correo, String contrasena, String idTienda,
                                   boolean isAdmin, boolean isVendedor, boolean isReponedor) {
        boolean insertarOK = false;
        conectarBD();
        try {
            String query = "INSERT INTO Usuario (dni, nombre, apellido, telefono, correo, activo, contraseña, id_tienda, is_admin, is_vendedor, is_reponedor) " +
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
            String query = "UPDATE Producto SET cantidad_stock = cantidad_stock - ? WHERE codigo_barras = ?";
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
            String query = "INSERT INTO ticket (codigo_barras_ticket, total_precio, fecha_ticket, fecha_limite_devolucion, isdevolucion, isventa, entregado, devuelto) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, ticket.getCodigo_barras_ticket());
            preparedStatement.setDouble(2, ticket.getTotal_precio());
            preparedStatement.setDate(3, java.sql.Date.valueOf(String.valueOf(ticket.getFecha_ticket())));
            preparedStatement.setDate(4, java.sql.Date.valueOf(String.valueOf(ticket.getFecha_limite_devolucion())));
            preparedStatement.setBoolean(5, ticket.isDevolucion());
            preparedStatement.setBoolean(6, ticket.isVenta());
            preparedStatement.setDouble(7, ticket.getEntregado());
            preparedStatement.setDouble(8, ticket.getDevuelto());

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
                        resultSet.getString("contraseña"),
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
            String query = "UPDATE usuario SET nombre = ?, apellido = ?, telefono = ?, correo = ?, activo = ?, contraseña = ?, id_tienda = ?, is_admin = ?, is_vendedor = ?, is_reponedor = ? WHERE dni = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);

            // Establecer los valores de los parámetros en la consulta SQL
            preparedStatement.setString(1, usuarioActualizado.getNombre());
            preparedStatement.setString(2, usuarioActualizado.getApellido());
            preparedStatement.setString(3, usuarioActualizado.getTelefono());
            preparedStatement.setString(4, usuarioActualizado.getCorreo());
            preparedStatement.setBoolean(5, usuarioActualizado.isActivo());
            preparedStatement.setString(6, usuarioActualizado.getContraseña());
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
            String query = "SELECT contraseña FROM usuario WHERE correo = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, correo);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Si se encuentra al menos un resultado, se obtiene la contraseña
            if (resultSet.next()) {
                contraseña = resultSet.getString("contraseña");
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
                        resultSet.getString("contraseña"),
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



    public List<ProductoModel> obtenerListaProductos() {
        List<ProductoModel> listaProductos = new ArrayList<>();
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        try {
            String query = "SELECT codigo_barras, nombre, descripcion, cantidad_stock, precio_unidad, veces_comprado, veces_devuelto, imagen_producto, id_tienda FROM producto";
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
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaProductos;
    }
    public List<ProductoModel> obtenerListaProductosparaDevo(String codigoTicket) {
        List<ProductoModel> listaProductos = new ArrayList<>();
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

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
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return listaProductos;
    }

    public boolean verificarExistenciaTicket(String codigoTicket) {
        conectarBD(); // Supongamos que esta función establece la conexión a la base de datos

        boolean existe = false;

        try {
            String query = "SELECT COUNT(*) FROM ticket WHERE codigo_barras_ticket = ?";
            PreparedStatement preparedStatement = conexion.prepareStatement(query);
            preparedStatement.setString(1, codigoTicket);
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
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
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
                cerrarConnection(conexion); // Supongo que tienes un método cerrarConnection() para cerrar la conexión
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }





}




