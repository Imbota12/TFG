package com.example.tfg_sistematienda.modelos;

import java.util.*;

public class TicketModel {

    private String codigo_barras_ticket;
    private double total_precio;
    private Date fecha_ticket;
    private Date fecha_limite_devolucion;
    private boolean isdevolucion;
    private boolean isventa;
    private double entregado;
    private double devuelto;


    public TicketModel(String codigo_barras_ticket, double total_precio, boolean isdevolucion, boolean isventa, double entregado, double devuelto) {
        this.codigo_barras_ticket = codigo_barras_ticket;
        this.total_precio = total_precio;
        this.isdevolucion = isdevolucion;
        this.isventa = isventa;
        this.entregado = entregado;
        this.devuelto = devuelto;
        this.fecha_ticket = new Date(); // Establecer la fecha actual como fecha de ticket
        this.fecha_limite_devolucion = calcularFechaLimiteDevolucion(); // Calcular la fecha límite de devolución
    }

    public TicketModel() {
        this.fecha_ticket = new Date(); // Establecer la fecha actual como fecha de ticket
        this.fecha_limite_devolucion = calcularFechaLimiteDevolucion(); // Calcular la fecha límite de devolución
    }

    private Date calcularFechaLimiteDevolucion() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha_ticket);
        calendar.add(Calendar.DATE, 15); // Agregar 15 días a la fecha de ticket
        return calendar.getTime();
    }

    public String getCodigo_barras_ticket() {
        return codigo_barras_ticket;
    }

    public void setCodigo_barras_ticket(String codigo_barras_ticket) {
        this.codigo_barras_ticket = codigo_barras_ticket;
    }

    public double getTotal_precio() {
        return total_precio;
    }

    public void setTotal_precio(double total_precio) {
        this.total_precio = total_precio;
    }

    public Date getFecha_ticket() {
        return fecha_ticket;
    }

    public void setFecha_ticket(Date fecha_ticket) {
        this.fecha_ticket = fecha_ticket;
    }

    public Date getFecha_limite_devolucion() {
        return fecha_limite_devolucion;
    }

    public void setFecha_limite_devolucion(Date fecha_limite_devolucion) {
        this.fecha_limite_devolucion = fecha_limite_devolucion;
    }

    public boolean isIsdevolucion() {
        return isdevolucion;
    }

    public void setIsdevolucion(boolean isdevolucion) {
        this.isdevolucion = isdevolucion;
    }

    public boolean isIsventa() {
        return isventa;
    }

    public void setIsventa(boolean isventa) {
        this.isventa = isventa;
    }

    public double getEntregado() {
        return entregado;
    }

    public void setEntregado(double entregado) {
        this.entregado = entregado;
    }

    public double getDevuelto() {
        return devuelto;
    }

    public void setDevuelto(double devuelto) {
        this.devuelto = devuelto;
    }
}
