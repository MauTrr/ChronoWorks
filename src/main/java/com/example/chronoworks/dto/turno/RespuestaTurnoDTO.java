package com.example.chronoworks.dto.turno;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class RespuestaTurnoDTO {
    private Integer idTurno;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
}
