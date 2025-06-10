package com.example.chronoworks.service;

import com.example.chronoworks.dto.asignacion.RespuestaAsignacionDTO;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Asignacion;
import com.example.chronoworks.model.Campaña;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.Tarea;
import com.example.chronoworks.repository.AsignacionRepository;
import com.example.chronoworks.repository.CampañaRepository;
import com.example.chronoworks.repository.EmpleadoRepository;
import com.example.chronoworks.repository.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsignacionService {

    private final AsignacionRepository asignacionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final CampañaRepository campañaRepository;
    private final TareaRepository tareaRepository;

    public AsignacionService(AsignacionRepository asignacionRepository,
                             EmpleadoRepository empleadoRepository,
                             CampañaRepository campañaRepository,
                             TareaRepository tareaRepository) {
        this.asignacionRepository = asignacionRepository;
        this.empleadoRepository = empleadoRepository;
        this.campañaRepository = campañaRepository;
        this.tareaRepository = tareaRepository;

    }

    @Transactional
    public RespuestaAsignacionDTO crearAsignacion(RespuestaAsignacionDTO dto) {

        Tarea tarea = tareaRepository.findById(dto.getIdTarea())
                .orElseThrow(() -> new ResourceNotFoundException("Tarea con ID " + dto.getIdTarea() + " no encontrada."));
        Empleado empleado = empleadoRepository.findById(dto.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado con ID " + dto.getIdEmpleado() + " no encontrado."));
        Campaña campaña = campañaRepository.findById(dto.getIdCampaña())
                .orElseThrow(() -> new ResourceNotFoundException("Campaña con ID " + dto.getIdCampaña() + " no encontrada."));

        Asignacion nuevaAsignacion = new Asignacion();
        nuevaAsignacion.setFecha(dto.getFecha());
        nuevaAsignacion.setObservaciones(dto.getObservaciones());
        nuevaAsignacion.setTarea(tarea);
        nuevaAsignacion.setEmpleado(empleado);
        nuevaAsignacion.setCampaña(campaña);
        nuevaAsignacion.setEstado(dto.getEstado());

        Asignacion asignacionGuardada = asignacionRepository.save(nuevaAsignacion);
        return
    }

    private RespuestaAsignacionDTO mapToRespuestaAsignacionDTO(Asignacion asignacion)  {
        return RespuestaAsignacionDTO.builder()
                .idAsignacion(asignacion.getIdAsignacion())
                .fecha(asignacion.getFecha())
                .observaciones(asignacion.getObservaciones())
                .idTarea(asignacion.getTarea() != null ? asignacion.getTarea().getIdTarea():  null)
                .nombreTarea(asignacion.getTarea()!= null ? asignacion.getTarea().getNombreTarea(): null)
                .idCampaña(asignacion.getCampaña() != null ? asignacion.getCampaña().getIdCampaña(): null)
                .estado(asignacion.getEstado())
                .build();
    }
}
