package com.example.chronoworks.controller;

import com.example.chronoworks.dto.campana.CampanaDTO;
import com.example.chronoworks.dto.campana.FiltroCampanaDTO;
import com.example.chronoworks.dto.campana.RespuestaCampanaDTO;
import com.example.chronoworks.service.CampanaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campanas")
public class CampanaController {
    private final CampanaService campanaService;

    public CampanaController(CampanaService campanaService) {
        this.campanaService = campanaService;
    }

    @PostMapping
    public ResponseEntity<RespuestaCampanaDTO> crearCampana(@Valid @RequestBody CampanaDTO dto) {
        RespuestaCampanaDTO nuevaCampana = campanaService.crearCampana(dto);
        return new ResponseEntity<>(nuevaCampana, HttpStatus.CREATED);
    }

    @GetMapping("/{idCampana}")
    public ResponseEntity<RespuestaCampanaDTO> obtenerCampana(@PathVariable Integer idCampana) {
        RespuestaCampanaDTO campana = campanaService.obtenerCampana(idCampana);
        return ResponseEntity.ok(campana);
    }

    @GetMapping
    public ResponseEntity<Page<RespuestaCampanaDTO>> listarCampanas(
            @ModelAttribute FiltroCampanaDTO filtro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue =  "10") int size,
            @RequestParam(defaultValue = "nombreCampaña") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(campanaService.listarCampanas(filtro, pageable));
    }

    @GetMapping("/activas")
    public ResponseEntity<Page<RespuestaCampanaDTO>> listarCampanasActivas(
            @ModelAttribute FiltroCampanaDTO filtro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue =  "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(campanaService.listarCampanasActivas(filtro, pageable));
    }

    @GetMapping("/{idCampana}")
    public ResponseEntity<RespuestaCampanaDTO> actualizarCampana(@PathVariable Integer idCampana, @Valid @RequestBody CampanaDTO dto) {
        RespuestaCampanaDTO campanaActualizada = campanaService.actualizarCampana(idCampana, dto);
        return ResponseEntity.ok(campanaActualizada);
    }

    @PutMapping("/{idCampana}/iniciar")
    public ResponseEntity<RespuestaCampanaDTO> iniciarCampana(@PathVariable Integer idCampana) {
        RespuestaCampanaDTO respuesta = campanaService.iniciarCampana(idCampana);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{idCampana}/finalizar")
    public ResponseEntity<RespuestaCampanaDTO> finalizarCampana(@PathVariable Integer idCampana) {
        RespuestaCampanaDTO respuesta = campanaService.finalizarCampana(idCampana);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{idCampana}/cancelar")
    public ResponseEntity<RespuestaCampanaDTO> cancelarCampana(@PathVariable Integer idCampana) {
        RespuestaCampanaDTO respuesta = campanaService.cancelarCampana(idCampana);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{idCampana}/iniciar")
    public ResponseEntity<RespuestaCampanaDTO> archivarCampana(@PathVariable Integer idCampana) {
        RespuestaCampanaDTO respuesta = campanaService.archivarCampana(idCampana);
        return ResponseEntity.ok(respuesta);
    }
}
