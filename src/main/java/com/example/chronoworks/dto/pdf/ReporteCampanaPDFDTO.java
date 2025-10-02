package com.example.chronoworks.dto.pdf;

import com.example.chronoworks.dto.campana.FiltroCampanaDTO;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReporteCampanaPDFDTO {
    private String titulo;
    private LocalDate fechaGeneracion;
    private FiltroCampanaDTO filtros;
    private List<CampanaPDFDTO> campanas;
    private ResumenReporteDTO resumen;
}
