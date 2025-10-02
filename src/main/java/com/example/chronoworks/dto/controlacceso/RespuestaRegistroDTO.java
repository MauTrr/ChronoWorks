package com.example.chronoworks.dto.controlacceso;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class RespuestaRegistroDTO {

    private Integer idControl;
    private LocalDate fecha;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private String observacionEntrada;
    private String observacionSalida;
    private Integer idEmpleado;
    private String nombre;

    // 🔹 Nuevo campo para sincronizar con el frontend
    private String estado; // "En turno" o "Fuera de turno"
}
