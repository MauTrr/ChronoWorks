package com.example.chronoworks.dto.campana;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class CampanaCargaMasivaDTO {

    @NotBlank(message = "El nombre de la campaña es obligatorio")
    private String nombreCampana;

    private String descripcion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    private String nombreEmpresa;

    @NotBlank(message = "El email del líder es obligatorio")
    private String emailLider;

    @NotBlank(message = "Debe haber al menos un agente")
    private String emailsAgentes;
}