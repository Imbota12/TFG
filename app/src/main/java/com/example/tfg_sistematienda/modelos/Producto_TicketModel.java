package com.example.tfg_sistematienda.modelos;

public class Producto_TicketModel {
    String codigoBarras_producto;
    String codigoBarras_ticket;
    int cantidad;

    public Producto_TicketModel(String codigoBarras_producto, String codigoBarras_ticket, int cantidad) {
        this.codigoBarras_producto = codigoBarras_producto;
        this.codigoBarras_ticket = codigoBarras_ticket;
        this.cantidad = cantidad;
    }

    public String getCodigoBarras_producto() {
        return codigoBarras_producto;
    }

    public void setCodigoBarras_producto(String codigoBarras_producto) {
        this.codigoBarras_producto = codigoBarras_producto;
    }

    public String getCodigoBarras_ticket() {
        return codigoBarras_ticket;
    }

    public void setCodigoBarras_ticket(String codigoBarras_ticket) {
        this.codigoBarras_ticket = codigoBarras_ticket;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
