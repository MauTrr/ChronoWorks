package com.example.chronoworks.controller;

import com.example.chronoworks.dto.empleado.ActualizarEmpleadoDTO;
import com.example.chronoworks.dto.empleado.RegistrarEmpleadoDTO;
import com.example.chronoworks.dto.empleado.RespuestaEmpleadoDTO;
import com.example.chronoworks.service.EmpleadoService;
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
@RequestMapping("/api/empleados")
public class EmpleadoController {
    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @PostMapping
    public ResponseEntity<RespuestaEmpleadoDTO> crearEmpleado(@Valid @RequestBody RegistrarEmpleadoDTO dto) {
        RespuestaEmpleadoDTO nuevoEmpleado = empleadoService.crearEmpleado(dto);
        return new ResponseEntity<>(nuevoEmpleado, HttpStatus.CREATED);
    }

    @PostMapping("/{idAsignacion}")
    public ResponseEntity<RespuestaEmpleadoDTO> obtenerEmpleado(@PathVariable Integer idEmpleado) {
        RespuestaEmpleadoDTO empleado = empleadoService.obtenerEmpleado(idEmpleado);
        return ResponseEntity.ok(empleado);
    }

    @GetMapping()
    public ResponseEntity<Page<RespuestaEmpleadoDTO>> listarEmpleados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "idEmpleado") String sort,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ResponseEntity.ok(empleadoService.listarEmpleados(pageable));
    }

    @PutMapping("/{idEmpleado}")
    public ResponseEntity<RespuestaEmpleadoDTO> actualizarEmpleado(@PathVariable Integer idEmpleado, @Valid @RequestBody ActualizarEmpleadoDTO dto) {
        RespuestaEmpleadoDTO empleadoActualizado = empleadoService.actualizarEmpleado(idEmpleado, dto);
        return ResponseEntity.ok(empleadoActualizado);
    }

    @PatchMapping("/{idEmpleado}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivarEmpleado(@PathVariable Integer idEmpleado) {
        empleadoService.desactivarEmpleado(idEmpleado);
        return ResponseEntity.noContent().build();
    }

}
