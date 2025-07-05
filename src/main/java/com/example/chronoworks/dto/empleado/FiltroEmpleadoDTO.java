package com.example.chronoworks.dto.empleado;

import lombok.Data;

@Data
public class FiltroEmpleadoDTO {
    private String search;
    private String nombreRol;
    private Boolean activo;
}
