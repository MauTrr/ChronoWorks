package com.example.chronoworks.dto.asignacion;

import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RespuestaAsignacionEmpleadoDTO {
    private Integer idAsignacionEmpleadoTarea;
    private Integer idEmpleado;
    private String nombreEmpleado;
    private String apellidoEmpleado;
    private Integer idAsignacion;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private AsignacionCampanaEstado estado;
    private Integer idTarea;
    private String nombreTarea;
    private Integer idCampana;
    private String nombreCampana;
}