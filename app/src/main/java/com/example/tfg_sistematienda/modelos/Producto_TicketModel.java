package com.example.tfg_sistematienda.modelos;

public class Producto_TicketModel {
    String codigoBarras_producto;
    String id_ticket;
    int cantidad;

    public Producto_TicketModel(String codigoBarras_producto, String id_ticket, int cantidad) {
        this.codigoBarras_producto = codigoBarras_producto;
        this.id_ticket = id_ticket;
        this.cantidad = cantidad;
    }

    public String getCodigoBarras_producto() {
        return codigoBarras_producto;
    }

    public void setCodigoBarras_producto(String codigoBarras_producto) {
        this.codigoBarras_producto = codigoBarras_producto;
    }

    public String getId_ticket() {
        return id_ticket;
    }

    public void setId_ticket(String id_ticket) {
        this.id_ticket = id_ticket;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
