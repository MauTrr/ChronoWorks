package com.example.chronoworks.dto.turno;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class TurnoDTO {

    @NotNull(message = "La hora de entrada no puede ser nula")
    private LocalTime horaEntrada;

    @NotNull(message = "La hora de salida no puede ser nula")
    private LocalTime horaSalida;
}
