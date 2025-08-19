package com.example.chronoworks.dto.campana;

import lombok.Data;

@Data
public class AsignacionDetalleDTO {
    private Integer idAsignacion;
    private Integer idEmpleado;
    private String nombreEmpleado;
    private boolean esLider;
    private String estadoAsignacion;
}
