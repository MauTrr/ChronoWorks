package com.example.chronoworks.dto;

public class ClienteForm {

    private String nombre;
    private String email;
    private String telefono;
    private String direccion;
    private String cedula;

    // 🔥 Constructor vacío OBLIGATORIO para Jackson
    public ClienteForm() {}

    // Opcional: constructor completo
    public ClienteForm(String nombre, String email, String telefono, String direccion, String cedula) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.cedula = cedula;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
}

