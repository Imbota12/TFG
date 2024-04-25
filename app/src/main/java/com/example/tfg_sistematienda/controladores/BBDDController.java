package com.example.tfg_sistematienda.controladores;

import com.example.tfg_sistematienda.BBDD.ConexionBBDD;

public class BBDDController {

    private ConexionBBDD conexionBBDD;

    public BBDDController() {
        this.conexionBBDD = new ConexionBBDD();
    }

    public void crearBBDD(){conexionBBDD.crearBaseDeDatosSiNoExiste();}

    public void crearTablasBBDD(){conexionBBDD.crearTablas();}




}
