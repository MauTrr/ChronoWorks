package com.example.chronoworks.dto.tarea;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RespuestaTareaDTO {
    private Integer idTarea;
    private String nombreTarea;
    private String detalles;
}
