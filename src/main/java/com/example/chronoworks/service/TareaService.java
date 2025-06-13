package com.example.chronoworks.service;

import com.example.chronoworks.dto.tarea.RespuestaTareaDTO;
import com.example.chronoworks.dto.tarea.TareaDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Tarea;
import com.example.chronoworks.repository.TareaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TareaService {
    private final TareaRepository tareaRepository;

    public TareaService(TareaRepository tareaRepository) {
        this.tareaRepository = tareaRepository;
    }

    @Transactional
    public RespuestaTareaDTO crearTarea(TareaDTO dto) {
        if (tareaRepository.findByNombreTarea(dto.getNombreTarea()).isPresent()) {
            throw new BadRequestException("La tarea ya esta registrada");
        }

        Tarea nuevaTarea = new Tarea();
        nuevaTarea.setNombreTarea(dto.getNombreTarea());
        nuevaTarea.setDetalles(dto.getDetalles());

        Tarea tareaGuardada = tareaRepository.save(nuevaTarea);
        return  mapToRespuestaTareaDTO(tareaGuardada);
    }

    @Transactional(readOnly = true)
    public RespuestaTareaDTO obtenerTarea(Integer idTarea) {
        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(()-> new ResourceNotFoundException("Tarea no encontrada."));
        return mapToRespuestaTareaDTO(tarea);
    }

    @Transactional(readOnly = true)
    public Page<RespuestaTareaDTO> listarTarea(Pageable pageable) {
        return tareaRepository.findByActivoTrue(pageable).map(this::mapToRespuestaTareaDTO);
    }

    @Transactional
    public RespuestaTareaDTO actualizarTarea(Integer idTarea, TareaDTO dto) {
        Tarea tareaExistente = tareaRepository.findById(idTarea)
                .orElseThrow(()-> new ResourceNotFoundException("Tarea no encontrada."));

        if(dto.getNombreTarea() != null) tareaExistente.setNombreTarea(dto.getNombreTarea());
        if(dto.getDetalles() != null) tareaExistente.setDetalles(dto.getDetalles());

        Tarea tareaActualizada = tareaRepository.save(tareaExistente);
        return mapToRespuestaTareaDTO(tareaActualizada);
    }

    @Transactional
    public void desactivarTarea(Integer idTarea) {
        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(()-> new ResourceNotFoundException("Tarea no encontrada."));
        tarea.setActivo(false);
        tareaRepository.save(tarea);
    }

    private RespuestaTareaDTO mapToRespuestaTareaDTO(Tarea tarea) {
        return RespuestaTareaDTO.builder()
                .idTarea(tarea.getIdTarea())
                .nombreTarea(tarea.getNombreTarea())
                .detalles(tarea.getDetalles())
                .build();
    }

}
