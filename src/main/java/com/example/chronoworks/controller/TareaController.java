package com.example.chronoworks.controller;

import com.example.chronoworks.dto.tarea.RespuestaTareaDTO;
import com.example.chronoworks.dto.tarea.TareaDTO;
import com.example.chronoworks.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {
    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @PostMapping
    public ResponseEntity<RespuestaTareaDTO> crearTarea(@Valid @RequestBody TareaDTO dto) {
        RespuestaTareaDTO nuevaTarea = tareaService.crearTarea(dto);
        return new ResponseEntity<>(nuevaTarea, HttpStatus.CREATED);
    }

    @GetMapping("/{idTarea}")
    public ResponseEntity<RespuestaTareaDTO> obtenerTarea(@PathVariable Integer idTarea) {
        RespuestaTareaDTO tarea = tareaService.obtenerTarea(idTarea);
        return ResponseEntity.ok(tarea);
    }

    @GetMapping
    public ResponseEntity<Page<RespuestaTareaDTO>> listarTarea (
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "idTarea") String sort,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ResponseEntity.ok(tareaService.listarTarea(pageable));
    }

    @PutMapping("/{idTarea}/actualizar")
    public ResponseEntity<RespuestaTareaDTO> actualizarTarea(@PathVariable Integer idTarea, @Valid @RequestBody TareaDTO dto) {
        RespuestaTareaDTO tareaActualizada = tareaService.actualizarTarea(idTarea, dto);
        return ResponseEntity.ok(tareaActualizada);
    }

    @DeleteMapping("/{idTarea}")
    @PreAuthorize("hasRole('ADMIN') or hasRole ('LIDER')")
    public ResponseEntity<RespuestaTareaDTO> eliminarTarea(@PathVariable Integer idTarea) {
        tareaService.eliminarTarea(idTarea);
        return ResponseEntity.noContent().build();
    }
}
