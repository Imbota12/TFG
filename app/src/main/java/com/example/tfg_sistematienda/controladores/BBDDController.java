package com.example.tfg_sistematienda.controladores;

import com.example.tfg_sistematienda.BBDD.ConexionBBDD;
import com.example.tfg_sistematienda.modelos.TiendaModel;
import com.example.tfg_sistematienda.modelos.UsuarioModel;

import java.util.List;

public class BBDDController {

    private ConexionBBDD conexionBBDD;

    public BBDDController() {
        this.conexionBBDD = new ConexionBBDD();
    }

    public void crearBBDD(){conexionBBDD.crearBaseDeDatosSiNoExiste();}

    public void crearTablasBBDD(){conexionBBDD.crearTablas();}

    public List<TiendaModel> obtenerListaTiendas(){return conexionBBDD.obtenerListaTiendas();}

    public boolean insertarUsuario(String dni, String nombre, String apellido, String telefono,
                                   String correo, String contrasena, String idTienda,
                                   boolean isAdmin, boolean isVendedor, boolean isReponedor){
        return conexionBBDD.insertarUsuario(dni, nombre, apellido, telefono, correo, contrasena, idTienda, isAdmin, isVendedor, isReponedor);
    }

    public List<String> obtenerListaCorreos() {return conexionBBDD.obtenerListaCorreos();}

    public UsuarioModel buscarUsuario(String nombre, String apellidos, String dni, String telefono) {return conexionBBDD.buscarUsuario(nombre, apellidos, dni, telefono);}

    public boolean actualizarUsuario(UsuarioModel usuarioActualizado) {return conexionBBDD.actualizarUsuario(usuarioActualizado);}


}
