package com.example.tfg_sistematienda.modelos;

import java.time.LocalDate;

public class LogModel {
    String accion;
    LocalDate fecha;
    String dni;

    public LogModel(String accion, LocalDate fecha, String dni) {
        this.accion = accion;
        this.fecha = fecha;
        this.dni = dni;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
}
