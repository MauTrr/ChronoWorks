package com.example.chronoworks.dto.empleado;

import lombok.Data;

@Data
public class EmpleadoDisponibleDTO {
    private Integer idEmpleado;
    private String nombre;
    private String apellido;
    private String correo;
}
