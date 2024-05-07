package com.example.tfg_sistematienda.modelos;

import java.math.BigDecimal;

public class ProductoModel {
    private String codigoBarras;
    private String nombre;
    private String descripcion;
    private int cantidadStock;
    private double precioUnidad;
    private int vecesComprado;
    private int vecesDevuelto;
    private byte[] imagenProducto;
    private String idTienda;
    private int cantidad;

    // Constructor
    public ProductoModel(String codigoBarras, String nombre, String descripcion, int cantidadStock, double precioUnidad, int vecesComprado, int vecesDevuelto, byte[] imagenProducto, String idTienda) {
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidadStock = cantidadStock;
        this.precioUnidad = precioUnidad;
        this.vecesComprado = vecesComprado;
        this.vecesDevuelto = vecesDevuelto;
        this.imagenProducto = imagenProducto;
        this.idTienda = idTienda;
        this.cantidad = 0;
    }

    public ProductoModel(){
        // Constructor vacío
        this.cantidad = 0;
    }

    public ProductoModel(String codigoBarras, String nombre, int cantidad){
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    // Getters y setters
    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidadStock() {
        return cantidadStock;
    }

    public void setCantidadStock(int cantidadStock) {
        this.cantidadStock = cantidadStock;
    }

    public double getPrecioUnidad() {
        return precioUnidad;
    }

    public void setPrecioUnidad(double precioUnidad) {
        this.precioUnidad = precioUnidad;
    }

    public int getVecesComprado() {
        return vecesComprado;
    }

    public void setVecesComprado(int vecesComprado) {
        this.vecesComprado = vecesComprado;
    }

    public int getVecesDevuelto() {
        return vecesDevuelto;
    }

    public void setVecesDevuelto(int vecesDevuelto) {
        this.vecesDevuelto = vecesDevuelto;
    }

    public byte[] getImagenProducto() {
        return imagenProducto;
    }

    public void setImagenProducto(byte[] imagenProducto) {
        this.imagenProducto = imagenProducto;
    }

    public String getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(String idTienda) {
        this.idTienda = idTienda;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
