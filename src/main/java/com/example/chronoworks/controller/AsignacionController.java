package com.example.chronoworks.controller;

import com.example.chronoworks.dto.asignacion.AsignacionDTO;
import com.example.chronoworks.dto.asignacion.FiltroAsignacionDTO;
import com.example.chronoworks.dto.asignacion.RespuestaAsignacionDTO;
import com.example.chronoworks.service.AsignacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionController {
    private final AsignacionService asignacionService;

    public AsignacionController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @PostMapping
    public ResponseEntity<RespuestaAsignacionDTO> crearAsignacion(@Valid @RequestBody AsignacionDTO dto) {
        RespuestaAsignacionDTO nuevaAsignacion = asignacionService.crearAsignacion(dto);
        return new ResponseEntity<>(nuevaAsignacion, HttpStatus.CREATED);
    }

    @PostMapping("/{idAsignacion}")
    public ResponseEntity<RespuestaAsignacionDTO> obtenerAsignacion(@PathVariable Integer idAsignacion) {
        RespuestaAsignacionDTO asignacion = asignacionService.obtenerAsignacion(idAsignacion);
        return ResponseEntity.ok(asignacion);
    }

    @GetMapping
    public ResponseEntity<List<RespuestaAsignacionDTO>> listarAsignaciones(@ModelAttribute FiltroAsignacionDTO filtro) {
        List<RespuestaAsignacionDTO> asignaciones  = asignacionService.listarAsignaciones(filtro);
        return ResponseEntity.ok(asignaciones);
    }

    @GetMapping("/activas")
    public ResponseEntity<List<RespuestaAsignacionDTO>> listarAsignacionesActivas(@ModelAttribute FiltroAsignacionDTO filtro) {
        List<RespuestaAsignacionDTO> asignaciones = asignacionService.listarAsignacionesActivas(filtro);
        return ResponseEntity.ok(asignaciones);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaAsignacionDTO> actualizarAsignacion(@PathVariable Integer idAsignacion, @Valid @RequestBody AsignacionDTO dto) {
        RespuestaAsignacionDTO asignacionActualizada = asignacionService.actualizarAsignacion(idAsignacion, dto);
        return ResponseEntity.ok(asignacionActualizada);
    }

    @PutMapping("/{idAsignacion}/iniciar")
    public ResponseEntity<RespuestaAsignacionDTO> iniciarAsignacion(@PathVariable Integer idAsignacion) {
        RespuestaAsignacionDTO respuesta = asignacionService.iniciarAsignacion(idAsignacion);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{idAsignacion}/finalizar")
    public ResponseEntity<RespuestaAsignacionDTO> finalizarAsignacion(@PathVariable Integer idAsignacion) {
        RespuestaAsignacionDTO respuesta = asignacionService.finalizarAsignacion(idAsignacion);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{idAsignacion}/cancelar")
    public ResponseEntity<RespuestaAsignacionDTO> cancelarAsignacion(@PathVariable Integer idAsignacion) {
        RespuestaAsignacionDTO respuesta = asignacionService.cancelarAsignacion(idAsignacion);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{idAsignacion}/archivar")
    public ResponseEntity<RespuestaAsignacionDTO> archivarAsignacion(@PathVariable Integer idAsignacion) {
        RespuestaAsignacionDTO respuesta = asignacionService.archivarAsignacion(idAsignacion);
        return ResponseEntity.ok(respuesta);
    }

}
