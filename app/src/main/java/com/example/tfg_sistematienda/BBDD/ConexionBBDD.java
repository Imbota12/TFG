package com.example.tfg_sistematienda.BBDD;

import android.os.StrictMode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionBBDD {

    private Connection conexion = null;
    private static final String DRIVER = "org.postgresql.Driver";
    private static final String URL = "jdbc:postgresql://10.0.2.2:5432/TFG";
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
        final String URL1 = "jdbc:postgresql://10.0.2.2:5432/";
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
                                    double precioUnidad, int vecesComprado, int vecesDevuelto, String imagenProducto,
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
            preparedStatement.setString(8, imagenProducto);
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




}
