package com.example.chronoworks.dto.controlacceso;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalTime;

@Data
public class RegistroSalidaDTO {
    @NotNull(message = "El ID del empleado no puede ser nulo")
    @Positive(message = "El ID del empleado debe ser un numero positivo")
    private Integer idEmpleado;
    @PastOrPresent(message = "La hora de salida no puede ser futura")
    private String observacionSalida;
}

