package com.example.chronoworks.dto.tarea;

import com.example.chronoworks.model.enums.TareaTipos;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RespuestaTareaDTO {
    private Integer idTarea;
    private String nombreTarea;
    private TareaTipos tipos;
}
