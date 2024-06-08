package com.example.tfg_sistematienda.modelos;

import java.time.LocalDate;

public class TicketModel {

    private String codigo_barras_ticket;
    private double total_precio;
    private LocalDate fecha_ticket;
    private LocalDate fecha_limite_devolucion;
    private boolean devolucion;
    private boolean venta;
    private double entregado;
    private double devuelto;
    private String id_ticket;


    public TicketModel(String codigo_barras_ticket, double total_precio, boolean devolucion, boolean venta, double entregado, double devuelto, String id_ticket) {
        this.codigo_barras_ticket = codigo_barras_ticket;
        this.total_precio = total_precio;
        this.devolucion = devolucion;
        this.venta = venta;
        this.entregado = entregado;
        this.devuelto = devuelto;
        this.fecha_ticket = LocalDate.now(); // Establecer la fecha actual como fecha de ticket
        this.fecha_limite_devolucion = calcularFechaLimiteDevolucion(); // Calcular la fecha límite de devolución
        this.id_ticket = id_ticket;
    }

    public TicketModel() {
        this.fecha_ticket = LocalDate.now(); // Establecer la fecha actual como fecha de ticket
        this.fecha_limite_devolucion = calcularFechaLimiteDevolucion(); // Calcular la fecha límite de devolución
    }

    private LocalDate calcularFechaLimiteDevolucion() {
        return fecha_ticket.plusDays(15); // Agregar 15 días a la fecha de ticket
    }

    // Getters y setters

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

    public LocalDate getFecha_ticket() {
        return fecha_ticket;
    }

    public void setFecha_ticket(LocalDate fecha_ticket) {
        this.fecha_ticket = fecha_ticket;
    }

    public LocalDate getFecha_limite_devolucion() {
        return fecha_limite_devolucion;
    }

    public void setFecha_limite_devolucion(LocalDate fecha_limite_devolucion) {
        this.fecha_limite_devolucion = fecha_limite_devolucion;
    }

    public boolean isDevolucion() {
        return devolucion;
    }

    public void setDevolucion(boolean devolucion) {
        this.devolucion = devolucion;
    }

    public boolean isVenta() {
        return venta;
    }

    public void setVenta(boolean venta) {
        this.venta = venta;
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

    public String getId_ticket() {
        return id_ticket;
    }

    public void setId_ticket(String id_ticket) {
        this.id_ticket = id_ticket;
    }
}
