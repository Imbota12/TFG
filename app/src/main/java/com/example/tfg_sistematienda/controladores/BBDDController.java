package com.example.tfg_sistematienda.controladores;

import com.example.tfg_sistematienda.BBDD.ConexionBBDD;
import com.example.tfg_sistematienda.modelos.ProductoModel;
import com.example.tfg_sistematienda.modelos.Producto_TicketModel;
import com.example.tfg_sistematienda.modelos.TicketModel;
import com.example.tfg_sistematienda.modelos.TiendaModel;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.math.BigDecimal;
import java.util.List;

public class BBDDController {

    private ConexionBBDD conexionBBDD;

    public BBDDController() {
        this.conexionBBDD = new ConexionBBDD();
    }

    public void crearBBDD(){conexionBBDD.crearBaseDeDatosSiNoExiste();}

    public void crearTablasBBDD(){conexionBBDD.crearTablas();}

    public List<TiendaModel> obtenerListaTiendas(){return conexionBBDD.obtenerListaTiendas();}

    public boolean insertarTienda(String cif, String nombre, String direccion, String telefono) {return conexionBBDD.insertarTienda(cif, nombre, direccion, telefono);}

    public boolean insertarUsuario(String dni, String nombre, String apellido, String telefono,
                                   String correo, String contrasena, String idTienda,
                                   boolean isAdmin, boolean isVendedor, boolean isReponedor){
        return conexionBBDD.insertarUsuario(dni, nombre, apellido, telefono, correo, contrasena, idTienda, isAdmin, isVendedor, isReponedor);
    }

    public List<String> obtenerListaCorreos() {return conexionBBDD.obtenerListaCorreos();}

    public UsuarioModel buscarUsuario(String nombre, String apellidos, String dni, String telefono) {return conexionBBDD.buscarUsuario(nombre, apellidos, dni, telefono);}

    public boolean actualizarUsuario(UsuarioModel usuarioActualizado) {return conexionBBDD.actualizarUsuario(usuarioActualizado);}

    public String obtenerContraseñaPorCorreo(String correo) { return conexionBBDD.obtenerContraseñaPorCorreo(correo);}

    public UsuarioModel buscarUsuarioPorCorreo(String correo) { return conexionBBDD.buscarUsuarioPorCorreo(correo);}

    public boolean insertarProducto(String codigoBarras, String nombre, String descripcion, int cantidadStock,
                                    double precioUnidad, int vecesComprado, int vecesDevuelto, byte[] imagenProducto,
                                    String idTienda) { return conexionBBDD.insertarProducto(codigoBarras, nombre, descripcion,cantidadStock,precioUnidad, vecesComprado, vecesDevuelto, imagenProducto, idTienda);}

    public List<String> obtenerListaCodigosBarras() {return conexionBBDD.obtenerListaCodigosBarras();}
    public List<String> obtenerListaCodigosTicket() {return conexionBBDD.obtenerListaCodigosTicket();}

    public List<String> obtenerListaCIF() {return conexionBBDD.obtenerListaCIF();}

    public List<ProductoModel> obtenerListaProductos() {return conexionBBDD.obtenerListaProductos();}

    public boolean modificarProducto(String nombreNuevo, String descripcionNueva,
                                     double precioUnidadNuevo, byte[] imagenNueva, String codigoBarras) {
        return conexionBBDD.modificarProducto(nombreNuevo, descripcionNueva, precioUnidadNuevo, imagenNueva, codigoBarras);}

    public boolean incrementarCantidadStock(String codigoBarras) {return conexionBBDD.incrementarCantidadStock(codigoBarras);}

    public boolean decrementarCantidadStock(String codigoBarras) {return conexionBBDD.decrementarCantidadStock(codigoBarras);}


    public boolean borrarProducto(String codigoBarras) {return conexionBBDD.borrarProducto(codigoBarras);}

    public double obtenerPrecioUnidadporCodigo(String codigoBarras) {return conexionBBDD.obtenerPrecioUnidadporCodigo(codigoBarras);}

    public int obtenerStockporCodigo(String codigoBarras) {return conexionBBDD.obtenerStockporCodigo(codigoBarras);}

    public boolean insertarTicket(TicketModel ticket) {return conexionBBDD.insertarTicket(ticket);}
    public boolean insertarProductoTicket(Producto_TicketModel productoTicket) {return conexionBBDD.insertarProductoTicket(productoTicket);}
    public boolean modificarStockProducto(String codigoBarras, int cantidad) {return conexionBBDD.modificarStockProducto(codigoBarras, cantidad);}

    }
