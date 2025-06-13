package com.example.chronoworks.controller;

import com.example.chronoworks.dto.turno.RespuestaTurnoDTO;
import com.example.chronoworks.dto.turno.TurnoDTO;
import com.example.chronoworks.service.TurnoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turno")
public class TurnoController {
    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @PostMapping
    public ResponseEntity<RespuestaTurnoDTO> crearTurno(@Valid @RequestBody TurnoDTO dto) {
        RespuestaTurnoDTO nuevoTurno = turnoService.crearTurno(dto);
        return new ResponseEntity<>(nuevoTurno, HttpStatus.CREATED);
    }

    @GetMapping("/{idTurno}")
    public ResponseEntity<RespuestaTurnoDTO> obtenerTurno(@PathVariable Integer idTurno) {
        RespuestaTurnoDTO turno = turnoService.obtenerTurno(idTurno);
        return ResponseEntity.ok(turno);
    }

    @GetMapping
    public ResponseEntity<List<RespuestaTurnoDTO>> listarTurno() {
        List<RespuestaTurnoDTO> turnos = turnoService.listarTurno();
        return ResponseEntity.ok(turnos);
    }

    @PutMapping("/{idTurno}")
    public ResponseEntity<RespuestaTurnoDTO> actualizarTurno(@PathVariable Integer idTurno, @Valid @RequestBody TurnoDTO dto) {
        RespuestaTurnoDTO turnoActualizado = turnoService.actualizarTurno(idTurno, dto);
        return ResponseEntity.ok(turnoActualizado);
    }

    @DeleteMapping("/{idTurno}")
    public ResponseEntity<RespuestaTurnoDTO> eliminarTurno(@PathVariable Integer idTurno) {
        turnoService.eliminarTurno(idTurno);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
