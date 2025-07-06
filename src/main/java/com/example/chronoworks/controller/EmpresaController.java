package com.example.chronoworks.controller;

import com.example.chronoworks.dto.empresa.EmpresaDTO;
import com.example.chronoworks.dto.empresa.RespuestaEmpresaDTO;
import com.example.chronoworks.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresa")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping
    public ResponseEntity<RespuestaEmpresaDTO> registrarEmpresa(@Valid @RequestBody EmpresaDTO dto) {
        return new ResponseEntity<>(empresaService.registrarEmpresa(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<RespuestaEmpresaDTO>> listarEmpresas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombreEmpresa,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) Boolean activo) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(empresaService.listarEmpresas(pageable, nombreEmpresa, sector, activo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaEmpresaDTO> obtenerEmpresa(@PathVariable Integer id) {
        return ResponseEntity.ok(empresaService.obtenerEmpresa(id));
    }

    @PutMapping("/{id}/actualizar")
    public ResponseEntity<RespuestaEmpresaDTO> actualizarEmpresa(@PathVariable Integer id, @Valid @RequestBody EmpresaDTO dto) {
        return ResponseEntity.ok(empresaService.actualizarEmpresa(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarEmpresa(@PathVariable Integer id) {
        empresaService.eliminarEmpresa(id);
        return ResponseEntity.noContent().build();
    }
}
