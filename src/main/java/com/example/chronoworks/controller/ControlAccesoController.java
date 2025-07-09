package com.example.chronoworks.controller;

import com.example.chronoworks.dto.controlacceso.RegistroEntradaDTO;
import com.example.chronoworks.dto.controlacceso.RegistroSalidaDTO;
import com.example.chronoworks.dto.controlacceso.RespuestaRegistroDTO;
import com.example.chronoworks.service.ControlAccesoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/controlAcceso")
public class ControlAccesoController {
    private final ControlAccesoService controlAccesoService;

    public ControlAccesoController(ControlAccesoService controlAccesoService) {
        this.controlAccesoService = controlAccesoService;
    }

    @PostMapping("/entrada")
    public ResponseEntity<RespuestaRegistroDTO> registrarEntrada(@Valid @RequestBody RegistroEntradaDTO dto) {
        RespuestaRegistroDTO repuesta = controlAccesoService.registrarEntrada(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(repuesta);
    }

    @PostMapping("/salida")
    public ResponseEntity<RespuestaRegistroDTO> registrarSalida(@Valid @RequestBody RegistroSalidaDTO dto) {
        RespuestaRegistroDTO repuesta = controlAccesoService.registrarSalida(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(repuesta);
    }

    @PatchMapping("/{idControl}/observaciones")
    public ResponseEntity<RespuestaRegistroDTO> actualizarObservaciones(
            @PathVariable Integer idControl,
            @RequestParam(required = false) String observacionEntrada,
            @RequestParam(required = false) String observacionSalida) {
        RespuestaRegistroDTO respuesta = controlAccesoService.actualizarObservacion(
                idControl, observacionEntrada, observacionSalida);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/empleados/{idEmpleado}/estado")
    public ResponseEntity<Map<String, Object>> obtenerEstadoActual(
            @PathVariable Integer idEmpleado) {
        return ResponseEntity.ok(controlAccesoService.obtenerEstadoActual(idEmpleado));
    }

    @GetMapping("/empleado/{idEmpleado}/historial")
    public ResponseEntity<Page<RespuestaRegistroDTO>> obtenerHistorial(
            @PathVariable Integer idEmpleado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<RespuestaRegistroDTO> historial = controlAccesoService.obtenerHistorial(idEmpleado, pageable);
        return ResponseEntity.ok(historial);
    }

    @GetMapping
    public ResponseEntity<List<RespuestaRegistroDTO>> listarTodosLosRegistros() {
        return ResponseEntity.ok(controlAccesoService.listarTodos());
    }



}
