package com.example.chronoworks.dto.asignacion;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AsignacionConsultaDTO {
    private Integer idAsignacion;
    private Integer idEmpleado;
    private String nombreEmpleado;
    private String apellidoEmpleado;
    private Boolean esLider;
    private String estadoAsignacion;
    private LocalDateTime fechaAsignacion;
}
