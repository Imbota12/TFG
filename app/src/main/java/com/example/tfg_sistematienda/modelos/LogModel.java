package com.example.tfg_sistematienda.modelos;

import java.util.Date;

public class LogModel {
    String accion;
    Date fecha;
    String dni;

    public LogModel(String accion, Date fecha, String dni) {
        this.accion = accion;
        this.fecha = fecha;
        this.dni = dni;
    }

    public LogModel() {

    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
}
