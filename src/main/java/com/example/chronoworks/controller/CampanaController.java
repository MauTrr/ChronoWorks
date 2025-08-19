package com.example.chronoworks.controller;

import com.example.chronoworks.dto.asignacion.AsignacionConsultaDTO;
import com.example.chronoworks.dto.asignacion.AsignacionCreacionDTO;
import com.example.chronoworks.dto.campana.*;
import com.example.chronoworks.model.enums.CampanaEstado;
import com.example.chronoworks.service.CampanaService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/campanas")
public class CampanaController {
    private final CampanaService campanaService;

    public CampanaController(CampanaService campanaService) {
        this.campanaService = campanaService;
    }

    @PostMapping
    public ResponseEntity<RespuestaCampanaDTO> crearCampana(@Valid @RequestBody CrearCampanaCompletaDTO request) {
        RespuestaCampanaDTO nuevaCampana = campanaService.crearCampana(
                request.getCampana(),
                request.getAsignaciones());
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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombreCampana") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(campanaService.listarCampanas(filtro, pageable));
    }


    @GetMapping("/todas")
    public ResponseEntity<List<RespuestaCampanaDTO>> listarTodasCampanas() {
        Page<RespuestaCampanaDTO> page = campanaService.listarCampanas(new FiltroCampanaDTO(), Pageable.unpaged());
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/activas")
    public ResponseEntity<Page<RespuestaCampanaDTO>> listarCampanasActivas(
            @ModelAttribute FiltroCampanaDTO filtro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(campanaService.listarCampanasActivas(filtro, pageable));
    }

    // Actualizar campaña (PUT)
    @PutMapping("/{idCampana}")
    public ResponseEntity<RespuestaCampanaDTO> actualizarCampana(
            @PathVariable Integer idCampana,
            @Valid @RequestBody ActualizarCampanaCompletaDTO request) {

        RespuestaCampanaDTO campanaActualizada = campanaService.actualizarCampana(
                idCampana,
                request.getCampana(),
                request.getAsignaciones());

        return ResponseEntity.ok(campanaActualizada);
    }

    @DeleteMapping("/{idCampana}")
    public ResponseEntity<Void> eliminarCampana(@PathVariable Integer idCampana) {
        campanaService.cambiarEstado(
                idCampana,
                CampanaEstado.CANCELADA,
                true);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{idCampana}/estado")
    public ResponseEntity<RespuestaCampanaDTO> cambiarEstado (
            @PathVariable Integer idCampana,
            @RequestBody CambiarEstadoDTO request) {

        RespuestaCampanaDTO respuesta = campanaService.cambiarEstado(
                idCampana,
                request.getEstado(),
                request.isLiberarEmpleados()
        );
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("{idCampana}/asignaciones")
    public ResponseEntity<List<AsignacionConsultaDTO>> getAsignaciondes(
            @PathVariable Integer idCampana) {
        return ResponseEntity.ok(campanaService.obtenerAsignaciones(idCampana));
    }

    @GetMapping("/reporte-excel")
    public ResponseEntity<ByteArrayResource> descargarReporteCampanasExcel(@ModelAttribute FiltroCampanaDTO filtro) throws IOException {

        byte[] excelContent= campanaService.generarReporteExcelCampanas(filtro);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=reporte_campanas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelContent.length)
                .body(new ByteArrayResource(excelContent));
    }
}
