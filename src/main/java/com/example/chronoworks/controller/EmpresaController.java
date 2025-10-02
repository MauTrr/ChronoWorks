package com.example.chronoworks.controller;

import com.example.chronoworks.dto.empresa.EmpresaDTO;
import com.example.chronoworks.dto.empresa.FiltroEmpresaDTO;
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

    @GetMapping("/{idEmpresa}")
    public ResponseEntity<RespuestaEmpresaDTO> obtenerEmpresa(@PathVariable Integer idEmpresa) {
        return ResponseEntity.ok(empresaService.obtenerEmpresa(idEmpresa));
    }

    @GetMapping
    public ResponseEntity<Page<RespuestaEmpresaDTO>> listarEmpresas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombreEmpresa,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) Boolean activo) {
        if (page < 0) page = 0;
        if (size < 1) size = 10;

        Pageable pageable = PageRequest.of(page, size);
        FiltroEmpresaDTO filtro = new FiltroEmpresaDTO();
        filtro.setNombreEmpresa(nombreEmpresa);
        filtro.setSector(sector);
        filtro.setActivo(activo);
        return ResponseEntity.ok(empresaService.listarEmpresas(filtro, pageable));
    }


    @PutMapping("/{idEmpresa}/actualizar")
    public ResponseEntity<RespuestaEmpresaDTO> actualizarEmpresa(@PathVariable Integer idEmpresa, @Valid @RequestBody EmpresaDTO dto) {
        RespuestaEmpresaDTO empresaActualizada = empresaService.actualizarEmpresa(idEmpresa, dto);
        return ResponseEntity.ok(empresaActualizada);
    }

    @PatchMapping("/{idEmpresa}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivarEmpresa(@PathVariable Integer idEmpresa) {
        empresaService.desactivarEmpresa(idEmpresa);
        return ResponseEntity.noContent().build();
    }

    
}
