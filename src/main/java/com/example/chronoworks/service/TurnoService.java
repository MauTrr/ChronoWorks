package com.example.chronoworks.service;


import com.example.chronoworks.dto.turno.RespuestaTurnoDTO;
import com.example.chronoworks.dto.turno.TurnoDTO;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Tarea;
import com.example.chronoworks.model.Turno;
import com.example.chronoworks.repository.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TurnoService {
    private final TurnoRepository turnoRepository;

    public TurnoService(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    @Transactional
    public RespuestaTurnoDTO crearTurno(TurnoDTO dto) {

        Turno nuevoTurno = new Turno();
        nuevoTurno.setHoraEntrada(dto.getHoraEntrada());
        nuevoTurno.setHoraSalida(dto.getHoraSalida());

        Turno turnoGuardado = turnoRepository.save(nuevoTurno);
        return  mapToRespuestaTurnoDTO(turnoGuardado);
    }

    @Transactional(readOnly = true)
    public RespuestaTurnoDTO obtenerTurno(Integer idTurno) {
        Turno turno = turnoRepository.findById(idTurno)
                .orElseThrow(()-> new ResourceNotFoundException("Turno con ID " + idTurno + " no encontrado"));
        return mapToRespuestaTurnoDTO(turno);
    }

    @Transactional(readOnly = true)
    public List<RespuestaTurnoDTO> listarTurno() {
        return turnoRepository.findAll().stream().map(this::mapToRespuestaTurnoDTO).collect(Collectors.toList());
    }

    @Transactional
    public RespuestaTurnoDTO actualizarTurno(Integer idTurno, TurnoDTO dto) {
        Turno turnoExistente = turnoRepository.findById(idTurno)
                .orElseThrow(()-> new ResourceNotFoundException("Turno con ID " + idTurno + " no encontrado"));

        if(dto.getHoraEntrada() != null) turnoExistente.setHoraEntrada(dto.getHoraEntrada());
        if(dto.getHoraSalida() != null) turnoExistente.setHoraSalida(dto.getHoraSalida());

        Turno turnoActualizado = turnoRepository.save(turnoExistente);
        return mapToRespuestaTurnoDTO(turnoActualizado);
    }

    @Transactional
    public void eliminarTurno(Integer idTurno) {
        if(!turnoRepository.existsById(idTurno)) {
            throw new ResourceNotFoundException("Tarea con ID " + idTurno + "no se ha encontrado para eliminar.");
        }
        turnoRepository.deleteById(idTurno);
    }

    private RespuestaTurnoDTO mapToRespuestaTurnoDTO(Turno turno) {
        return RespuestaTurnoDTO.builder()
                .idTurno(turno.getIdTurno())
                .horaEntrada(turno.getHoraEntrada())
                .horaSalida(turno.getHoraSalida())
                .build();
    }
}
