package com.example.chronoworks.service;

import com.example.chronoworks.dto.asignacion.AsignacionDTO;
import com.example.chronoworks.dto.asignacion.RespuestaAsignacionDTO;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Asignacion;
import com.example.chronoworks.model.Campana;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.Tarea;
import com.example.chronoworks.repository.AsignacionRepository;
import com.example.chronoworks.repository.CampanaRepository;
import com.example.chronoworks.repository.EmpleadoRepository;
import com.example.chronoworks.repository.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AsignacionService {

    private final AsignacionRepository asignacionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final CampanaRepository campanaRepository;
    private final TareaRepository tareaRepository;

    public AsignacionService(AsignacionRepository asignacionRepository,
                             EmpleadoRepository empleadoRepository,
                             CampanaRepository campanaRepository,
                             TareaRepository tareaRepository) {
        this.asignacionRepository = asignacionRepository;
        this.empleadoRepository = empleadoRepository;
        this.campanaRepository = campanaRepository;
        this.tareaRepository = tareaRepository;

    }

    @Transactional
    public RespuestaAsignacionDTO crearAsignacion(RespuestaAsignacionDTO dto) {

        Tarea tarea = tareaRepository.findById(dto.getIdTarea())
                .orElseThrow(() -> new ResourceNotFoundException("Tarea con ID " + dto.getIdTarea() + " no encontrada."));
        Empleado empleado = empleadoRepository.findById(dto.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado con ID " + dto.getIdEmpleado() + " no encontrado."));
        Campana campana = campanaRepository.findById(dto.getIdCampana())
                .orElseThrow(() -> new ResourceNotFoundException("Campaña con ID " + dto.getIdCampana() + " no encontrada."));

        Asignacion nuevaAsignacion = new Asignacion();
        nuevaAsignacion.setFecha(dto.getFecha());
        nuevaAsignacion.setObservaciones(dto.getObservaciones());
        nuevaAsignacion.setTarea(tarea);
        nuevaAsignacion.setEmpleado(empleado);
        nuevaAsignacion.setCampana(campana);
        nuevaAsignacion.setEstado(dto.getEstado());

        Asignacion asignacionGuardada = asignacionRepository.save(nuevaAsignacion);
        return mapToRespuestaAsignacionDTO(asignacionGuardada);
    }

    @Transactional(readOnly = true)
    public RespuestaAsignacionDTO obtenerAsignacion(Integer idAsignacion) {
        Asignacion asignacion = asignacionRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion con ID " + idAsignacion + " no encontrada."));
        return mapToRespuestaAsignacionDTO(asignacion);
    }

    @Transactional(readOnly = true)
    public List<RespuestaAsignacionDTO> listarAsignaciones() {
        return asignacionRepository.findAll().stream().map(this::mapToRespuestaAsignacionDTO).collect(Collectors.toList());
    }

    @Transactional
    public RespuestaAsignacionDTO actualizarAsignacion(Integer idAsignacion, AsignacionDTO dto) {
        Asignacion asignacionExistente = asignacionRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion con ID " + idAsignacion + " no encontrada."));

        if(dto.getFecha() != null) asignacionExistente.setFecha(dto.getFecha());
        if(dto.getObservaciones()!= null) asignacionExistente.setObservaciones(dto.getObservaciones());
        if(dto.getIdTarea()!= null) {
            Tarea nuevaTarea = tareaRepository.findById(dto.getIdTarea())
                    .orElseThrow(()-> new ResourceNotFoundException("Tarea  con ID " + dto.getIdTarea() + " no encontrada."));
            asignacionExistente.setTarea(nuevaTarea);
        }
        if(dto.getIdCampana()!=  null) {
            Campana nuevaCampana = campanaRepository.findById(dto.getIdCampana())
                    .orElseThrow(() -> new ResourceNotFoundException("Campaaña con ID " + dto.getIdCampana() + " no encontrada."));
            asignacionExistente.setCampana(nuevaCampana);
        }
        if(dto.getIdEmpleado()!= null) {
            Empleado nuevoEmpleado = empleadoRepository.findById(dto.getIdEmpleado())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado con ID " + dto.getIdEmpleado() + " no encontrado."));
            asignacionExistente.setEmpleado(nuevoEmpleado);
        }

        Asignacion asignacionActualizada = asignacionRepository.save(asignacionExistente);
        return mapToRespuestaAsignacionDTO(asignacionActualizada);
    }



    private RespuestaAsignacionDTO mapToRespuestaAsignacionDTO(Asignacion asignacion)  {
        return RespuestaAsignacionDTO.builder()
                .idAsignacion(asignacion.getIdAsignacion())
                .fecha(asignacion.getFecha())
                .observaciones(asignacion.getObservaciones())
                .idTarea(asignacion.getTarea() != null ? asignacion.getTarea().getIdTarea():  null)
                .nombreTarea(asignacion.getTarea()!= null ? asignacion.getTarea().getNombreTarea(): null)
                .detalles(asignacion.getTarea()!= null ? asignacion.getTarea().getDetalles(): null)
                .idCampana(asignacion.getCampana() != null ? asignacion.getCampana().getIdCampana(): null)
                .nombreCampana(asignacion.getCampana()!= null ? asignacion.getCampana().getNombreCampana(): null)
                .idEmpleado(asignacion.getEmpleado()!= null ? asignacion.getEmpleado().getIdEmpleado(): null)
                .nombre(asignacion.getTarea()!= null ? asignacion.getEmpleado().getNombre(): null)
                .apellido(asignacion.getEmpleado()!= null ? asignacion.getEmpleado().getApellido(): null)
                .estado(asignacion.getEstado())
                .build();
    }
}
