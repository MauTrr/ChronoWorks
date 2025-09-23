package com.example.chronoworks.dto.asignacion;

import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FiltroAsignacionCompletaDTO {

    //Filtros de asignacion
    private String nombreTarea;
    private String nombreCampana;
    private LocalDateTime fechaAsignacion;
    private AsignacionCampanaEstado estado;

    //Filtros de empleado
    private String nombreEmpleado;
    private String apellidoEmpleado;
    private AsignacionCampanaEstado estadoEmpleado;

}
