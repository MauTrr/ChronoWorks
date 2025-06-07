package com.example.chronoworks.dto.asignacion;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RespuestaAsignacionDTO {
    private Integer idAsignacion;
    private LocalDate fecha;
    private Integer idTarea;
    private String nombreTarea;
    private String detallesTarea;
    private Integer idCampaña;
    private String nombreCampaña;
    private String descripcionCampaña;
    private Integer idEmpleado;
    private String nombre;
    private String apellido;

}
