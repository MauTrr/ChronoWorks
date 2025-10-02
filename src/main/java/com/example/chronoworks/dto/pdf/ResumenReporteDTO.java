package com.example.chronoworks.dto.pdf;

import lombok.Data;

@Data
public class ResumenReporteDTO {
    private Integer totalCampanas;
    private Integer activas;
    private Integer enProceso;
    private Integer finalizadas;
    private Integer canceladas;
    private Integer archivadas;
}
