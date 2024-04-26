package com.example.tfg_sistematienda.modelos;

import java.math.BigDecimal;

public class ProductoModel {
    private String codigoBarras;
    private String nombre;
    private String descripcion;
    private int cantidadStock;
    private BigDecimal precioUnidad;
    private int vecesComprado;
    private int vecesDevuelto;
    private byte[] imagenProducto;
    private String idTienda;

    // Constructor
    public ProductoModel(String codigoBarras, String nombre, String descripcion, int cantidadStock, BigDecimal precioUnidad, int vecesComprado, int vecesDevuelto, byte[] imagenProducto, String idTienda) {
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidadStock = cantidadStock;
        this.precioUnidad = precioUnidad;
        this.vecesComprado = vecesComprado;
        this.vecesDevuelto = vecesDevuelto;
        this.imagenProducto = imagenProducto;
        this.idTienda = idTienda;
    }

    public ProductoModel(){
        // Constructor vacío
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

    public BigDecimal getPrecioUnidad() {
        return precioUnidad;
    }

    public void setPrecioUnidad(BigDecimal precioUnidad) {
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
}
