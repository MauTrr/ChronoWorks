package com.example.chronoworks.dto.campana;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CargaMasivaResponse {
    private int totalRegistros;
    private int exitosos;
    private int fallidos;
    private List<ErrorCarga> errores = new ArrayList<>();

    public void incrementarExitoso() {
        totalRegistros++;
        exitosos++;
    }

    public void agregarError(int linea, String mensaje, String registro) {
        totalRegistros++;
        fallidos++;
        errores.add(new ErrorCarga(linea, mensaje, registro));
    }
}

@Data
class ErrorCarga {
    private int numeroLinea;
    private String mensajeError;
    private String registro;
    private String timestamp;

    public ErrorCarga(int numeroLinea, String mensajeError, String registro) {
        this.numeroLinea = numeroLinea;
        this.mensajeError = mensajeError;
        this.registro = registro;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }
}