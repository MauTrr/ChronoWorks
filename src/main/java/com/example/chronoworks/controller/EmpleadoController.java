package com.example.chronoworks.controller;

import com.example.chronoworks.dto.empleado.ActualizarEmpleadoDTO;
import com.example.chronoworks.dto.empleado.FiltroEmpleadoDTO;
import com.example.chronoworks.dto.empleado.RegistrarEmpleadoDTO;
import com.example.chronoworks.dto.empleado.RespuestaEmpleadoDTO;
import com.example.chronoworks.service.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/{idEmpleado}")
    public ResponseEntity<RespuestaEmpleadoDTO> obtenerEmpleado(@PathVariable Integer idEmpleado) {
        RespuestaEmpleadoDTO empleado = empleadoService.obtenerEmpleado(idEmpleado);
        return ResponseEntity.ok(empleado);
    }

    @GetMapping()
    public ResponseEntity<Page<RespuestaEmpleadoDTO>> listarEmpleados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String nombreRol,
            @RequestParam(required = false) Boolean activo) {
        if (page < 0) page = 0;
        if (size < 1) size = 10;

        Pageable pageable = PageRequest.of(page, size);
        FiltroEmpleadoDTO filtro = new FiltroEmpleadoDTO();
        filtro.setSearch(search);
        filtro.setNombreRol(nombreRol);
        filtro.setActivo(activo);

        return ResponseEntity.ok(empleadoService.listarEmpleados(filtro, pageable));
    }

    @PutMapping("/{idEmpleado}/actualizar")
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
