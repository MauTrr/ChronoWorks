package com.example.chronoworks.controller;

import com.example.chronoworks.dto.empresa.EmpresaDTO;
import com.example.chronoworks.dto.empresa.RespuestaEmpresaDTO;
import com.example.chronoworks.service.EmpresaService;
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
@RequestMapping("/api/empresa")
public class EmpresaController {
    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping
    public ResponseEntity<RespuestaEmpresaDTO> registrarEmpresa(@Valid @RequestBody EmpresaDTO dto) {
        RespuestaEmpresaDTO nuevaEmpresa = empresaService.registrarEmpresa(dto);
        return new ResponseEntity<>(nuevaEmpresa, HttpStatus.CREATED);
    }

    @GetMapping("/{idEmpresa}")
    public ResponseEntity<RespuestaEmpresaDTO> obtenerEmpresa(@PathVariable Integer idEmpresa) {
        RespuestaEmpresaDTO empresa = empresaService.obtenerEmpresa(idEmpresa);
        return ResponseEntity.ok(empresa);
    }

    @GetMapping
    public ResponseEntity<Page<RespuestaEmpresaDTO>> listarEmpresas (
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "idEmpresa") String sort,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ResponseEntity.ok(empresaService.listarEmpresas(pageable));
    }

    @PutMapping("/{idEmpresa}/actualizar")
    public ResponseEntity<RespuestaEmpresaDTO> actualizarEmpresa(@PathVariable Integer idEmpresa, @Valid @RequestBody EmpresaDTO dto) {
        RespuestaEmpresaDTO empresaActualizada = empresaService.actualizarEmpresa(idEmpresa, dto);
        return ResponseEntity.ok(empresaActualizada);
    }

    @PatchMapping("/{idEmpresa}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RespuestaEmpresaDTO> desactivarEmpresa(@PathVariable Integer idEmpresa) {
        empresaService.desactivarEmpresa(idEmpresa);
        return ResponseEntity.noContent().build();
    }

}
