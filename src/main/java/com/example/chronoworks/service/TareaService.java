package com.example.chronoworks.service;

import com.example.chronoworks.dto.tarea.RespuestaTareaDTO;
import com.example.chronoworks.dto.tarea.TareaDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Tarea;
import com.example.chronoworks.repository.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TareaService {
    private final TareaRepository tareaRepository;

    public TareaService(TareaRepository tareaRepository) {
        this.tareaRepository = tareaRepository;
    }

    @Transactional
    public RespuestaTareaDTO crearTarea(TareaDTO dto) {
        if (tareaRepository.findByNombreTarea(dto.getNombreTarea()).isPresent()) {
            throw new BadRequestException("La tarea con el nombre " + dto.getNombreTarea() + " ya esta registrada");
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
                .orElseThrow(()-> new ResourceNotFoundException("Tarea con ID " + idTarea + " no encontrado"));
        return mapToRespuestaTareaDTO(tarea);
    }

    @Transactional(readOnly = true)
    public List<RespuestaTareaDTO> listarTarea() {
        return tareaRepository.findAll().stream().map(this::mapToRespuestaTareaDTO).collect(Collectors.toList());
    }

    @Transactional
    public RespuestaTareaDTO actualizarTarea(Integer idTarea, TareaDTO dto) {
        Tarea tareaExistente = tareaRepository.findById(idTarea)
                .orElseThrow(()-> new ResourceNotFoundException("Tarea con ID " + idTarea + " no encontrado"));

        if(dto.getNombreTarea() != null) tareaExistente.setNombreTarea(dto.getNombreTarea());
        if(dto.getDetalles() != null) tareaExistente.setDetalles(dto.getDetalles());

        Tarea tareaActualizada = tareaRepository.save(tareaExistente);
        return mapToRespuestaTareaDTO(tareaActualizada);
    }

    @Transactional
    public void eliminarTarea(Integer idTarea) {
        if(!tareaRepository.existsById(idTarea)) {
            throw new ResourceNotFoundException("Tarea con ID " + idTarea + "no se ha encontrado para eliminar.");
        }
        tareaRepository.deleteById(idTarea);
    }

    private RespuestaTareaDTO mapToRespuestaTareaDTO(Tarea tarea) {
        return RespuestaTareaDTO.builder()
                .idTarea(tarea.getIdTarea())
                .nombreTarea(tarea.getNombreTarea())
                .detalles(tarea.getDetalles())
                .build();
    }

}
