package com.example.tfg_sistematienda.modelos;

public class UsuarioModel {
    private String dni;
    private String nombre;
    private String apellido;
    private String telefono;
    private String correo;
    private boolean activo;
    private String contrasena;
    private String idTienda;
    private boolean isAdmin;
    private boolean isVendedor;
    private boolean isReponedor;


    public UsuarioModel() {
        // Constructor vacío
    }

    public UsuarioModel(String dni, String nombre, String apellido, String telefono, String correo, boolean activo,
                        String contrasena, String idTienda, boolean isAdmin, boolean isVendedor, boolean isReponedor) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correo = correo;
        this.activo = activo;
        this.contrasena = contrasena;
        this.idTienda = idTienda;
        this.isAdmin = isAdmin;
        this.isVendedor = isVendedor;
        this.isReponedor = isReponedor;
    }

    // Getters y Setters

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(String idTienda) {
        this.idTienda = idTienda;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isVendedor() {
        return isVendedor;
    }

    public void setVendedor(boolean vendedor) {
        isVendedor = vendedor;
    }

    public boolean isReponedor() {
        return isReponedor;
    }

    public void setReponedor(boolean reponedor) {
        isReponedor = reponedor;
    }
}

