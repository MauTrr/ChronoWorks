package com.example.chronoworks.dto.tarea;

import com.example.chronoworks.model.enums.TareaTipos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TareaDTO {

    private Integer idTarea;

    @NotBlank(message = "El nombre de la tarea no puede estar vacio")
    private String nombreTarea;

    @NotNull(message = "El tipo de tarea es obligatorio")
    private TareaTipos tipo;
}
