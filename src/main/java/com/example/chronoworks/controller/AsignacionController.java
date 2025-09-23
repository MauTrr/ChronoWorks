package com.example.chronoworks.controller;

import com.example.chronoworks.dto.asignacion.*;
import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
import com.example.chronoworks.service.AsignacionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionController {
    private final AsignacionService asignacionService;

    public AsignacionController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @PostMapping
    public ResponseEntity<RespuestaAsignacionCompletaDTO> crearAsignacion(@Valid @RequestBody AsignacionCompletaDTO dto) {
        RespuestaAsignacionCompletaDTO nuevaAsignacion = asignacionService.crearAsignacion(dto);
        return new ResponseEntity<>(nuevaAsignacion, HttpStatus.CREATED);
    }

    @GetMapping("/{idAsignacion}")
    public ResponseEntity<RespuestaAsignacionCompletaDTO> obtenerAsignacion(@PathVariable Integer idAsignacion) {
        RespuestaAsignacionCompletaDTO asignacion = asignacionService.obtenerAsignacion(idAsignacion);
        return ResponseEntity.ok(asignacion);
    }

    @GetMapping
    public ResponseEntity<Page<RespuestaAsignacionCompletaDTO>> listarAsignaciones(
            @ModelAttribute FiltroAsignacionCompletaDTO filtro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue =  "10") int size,
            @RequestParam(defaultValue = "idAsignacion") String sort,
            @RequestParam(required = false,defaultValue = "asc") String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ResponseEntity.ok(asignacionService.listarAsignaciones(filtro, pageable));
    }

    @PutMapping("/{idAsignacion}/actualizar")
    public ResponseEntity<RespuestaAsignacionCompletaDTO> actualizarAsignacion(@PathVariable Integer idAsignacion, @Valid @RequestBody AsignacionCompletaDTO dto) {
        RespuestaAsignacionCompletaDTO asignacionActualizada = asignacionService.actualizarAsignacion(idAsignacion, dto);
        return ResponseEntity.ok(asignacionActualizada);
    }

    @PostMapping("/{idAsignacion}/empleados")
    public ResponseEntity<RespuestaAsignacionCompletaDTO> agregarEmpleadoAsignacion(
            @PathVariable Integer idAsignacion,
            @Valid @RequestBody AsignacionEmpleadoDTO empleadoDTO) {
        RespuestaAsignacionCompletaDTO respuesta = asignacionService.agregarEmpleadoAsignacion(idAsignacion, empleadoDTO);
        return ResponseEntity.ok(respuesta);
    }

    // ACTUALIZAR EMPLEADO EN ASIGNACIÓN
    @PutMapping("/{idAsignacion}/empleados/{idEmpleado}")
    public ResponseEntity<RespuestaAsignacionCompletaDTO> actualizarEmpleadoAsignacion(
            @PathVariable Integer idAsignacion,
            @PathVariable Integer idEmpleado,
            @Valid @RequestBody AsignacionEmpleadoDTO empleadoDTO) {
        RespuestaAsignacionCompletaDTO respuesta = asignacionService.actualizarEmpleadoAsignacion(idAsignacion, idEmpleado, empleadoDTO);
        return ResponseEntity.ok(respuesta);
    }

    // CAMBIAR ESTADO DE ASIGNACIÓN
    @PatchMapping("/{idAsignacion}/estado")
    public ResponseEntity<RespuestaAsignacionCompletaDTO> cambiarEstado(
            @PathVariable Integer idAsignacion,
            @RequestParam AsignacionCampanaEstado nuevoEstado) {
        RespuestaAsignacionCompletaDTO respuesta = asignacionService.cambiarEstado(idAsignacion, nuevoEstado);
        return ResponseEntity.ok(respuesta);
    }

    // MÉTODOS ESPECÍFICOS DE ESTADO (opcionales - para mantener compatibilidad)
    @PutMapping("/{idAsignacion}/iniciar")
    public ResponseEntity<RespuestaAsignacionCompletaDTO> iniciarAsignacion(@PathVariable Integer idAsignacion) {
        RespuestaAsignacionCompletaDTO respuesta = asignacionService.cambiarEstado(idAsignacion, AsignacionCampanaEstado.ACTIVA);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{idAsignacion}/finalizar")
    public ResponseEntity<RespuestaAsignacionCompletaDTO> finalizarAsignacion(@PathVariable Integer idAsignacion) {
        RespuestaAsignacionCompletaDTO respuesta = asignacionService.cambiarEstado(idAsignacion, AsignacionCampanaEstado.LIBERADA);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{idAsignacion}/cancelar")
    public ResponseEntity<RespuestaAsignacionCompletaDTO> cancelarAsignacion(@PathVariable Integer idAsignacion) {
        RespuestaAsignacionCompletaDTO respuesta = asignacionService.cambiarEstado(idAsignacion, AsignacionCampanaEstado.INACTIVA);
        return ResponseEntity.ok(respuesta);
    }
}