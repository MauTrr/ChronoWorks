package com.example.chronoworks.dto.empleado;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RespuestaEmpleadoDTO {

    private Integer idEmpleado;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private LocalDateTime fechaIngreso;
    private Integer idTurno;
    private String usuario;
    private String nombreRol;
    private Integer idRol;
    private Boolean activo;
}
