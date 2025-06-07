package com.example.chronoworks.dto.turno;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class TurnoRespuestaDTO {
    private Integer idTurno;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
}
