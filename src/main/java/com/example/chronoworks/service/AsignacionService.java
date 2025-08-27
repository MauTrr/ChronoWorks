package com.example.chronoworks.service;

import com.example.chronoworks.dto.asignacion.AsignacionCreacionDTO;
import com.example.chronoworks.dto.asignacion.FiltroAsignacionDTO;
import com.example.chronoworks.dto.asignacion.RespuestaAsignacionDTO;
import com.example.chronoworks.exception.IllegalStateException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.AsignacionTarea;
import com.example.chronoworks.model.Campana;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.Tarea;
import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
import com.example.chronoworks.repository.AsignacionTareaRepository;
import com.example.chronoworks.repository.CampanaRepository;
import com.example.chronoworks.repository.EmpleadoRepository;
import com.example.chronoworks.repository.TareaRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AsignacionService {

    private final AsignacionTareaRepository asignacionTareaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final CampanaRepository campanaRepository;
    private final TareaRepository tareaRepository;

    public AsignacionService(AsignacionTareaRepository asignacionTareaRepository,
                             EmpleadoRepository empleadoRepository,
                             CampanaRepository campanaRepository,
                             TareaRepository tareaRepository) {
        this.asignacionTareaRepository = asignacionTareaRepository;
        this.empleadoRepository = empleadoRepository;
        this.campanaRepository = campanaRepository;
        this.tareaRepository = tareaRepository;

    }

    @Transactional
    public RespuestaAsignacionDTO crearAsignacion(AsignacionCreacionDTO dto) {

        Tarea tarea = tareaRepository.findById(dto.getIdTarea())
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada."));
        Empleado empleado = empleadoRepository.findById(dto.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado."));
        Campana campana = campanaRepository.findById(dto.getIdCampana())
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada."));

        AsignacionTarea nuevaAsignacionTarea = new AsignacionTarea();
        nuevaAsignacionTarea.setFecha(dto.getFecha());
        nuevaAsignacionTarea.setObservaciones(dto.getObservaciones());
        nuevaAsignacionTarea.setTarea(tarea);
        nuevaAsignacionTarea.setEmpleado(empleado);
        nuevaAsignacionTarea.setCampana(campana);
        nuevaAsignacionTarea.setEstado(AsignacionCampanaEstado.ACTIVA);

        AsignacionTarea asignacionTareaGuardada = asignacionTareaRepository.save(nuevaAsignacionTarea);
        return mapToRespuestaAsignacionDTO(asignacionTareaGuardada);
    }

    @Transactional(readOnly = true)
    public RespuestaAsignacionDTO obtenerAsignacion(Integer idAsignacion) {
        AsignacionTarea asignacionTarea = asignacionTareaRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));
        return mapToRespuestaAsignacionDTO(asignacionTarea);
    }


    @Transactional(readOnly = true)
    public Page<RespuestaAsignacionDTO> listarAsignaciones(FiltroAsignacionDTO filtro, Pageable pageable) {
        return listarAsignacionesConFiltros(filtro, false, pageable);
    }

    @Transactional(readOnly = true)
    public Page<RespuestaAsignacionDTO> listarAsignacionesActivas(FiltroAsignacionDTO filtro, Pageable pageable) {
        return listarAsignacionesConFiltros(filtro, true, pageable);
    }

    private Page<RespuestaAsignacionDTO> listarAsignacionesConFiltros(FiltroAsignacionDTO filtro, boolean soloActivas, Pageable pageable) {
        Specification<AsignacionTarea> spec = crearSpecificationAsignacion(filtro, soloActivas);
        Page<AsignacionTarea> asignacionesPage = asignacionTareaRepository.findAll(spec, pageable);
        return asignacionesPage.map(this::mapToRespuestaAsignacionDTO);
    }

    private Specification<AsignacionTarea> crearSpecificationAsignacion(FiltroAsignacionDTO filtro, boolean soloActivas) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(filtro.getNombreEmpleado()!= null && !filtro.getNombreEmpleado().trim().isEmpty()) {
                Join<AsignacionTarea, Empleado> empleadoJoin = root.join("Empleado");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(empleadoJoin.get("nombre")),
                        "%" + filtro.getNombreEmpleado().toLowerCase() + "%"));
            }

            if(filtro.getApellidoEmpleado()!= null && !filtro.getApellidoEmpleado().trim().isEmpty()){
                Join<AsignacionTarea, Empleado> empleadoJoin  = root.join("Empleado");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(empleadoJoin.get("apellido")),
                        "%" + filtro.getApellidoEmpleado().toLowerCase() + "%"));
            }

            if(filtro.getNombreCampana()!= null && !filtro.getNombreCampana().trim().isEmpty()) {
                Join<AsignacionTarea, Campana> campanaJoin = root.join("Campana");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(campanaJoin.get("nombreCampana")),
                        "%" + filtro.getNombreCampana().toLowerCase() + "%"));
            }

            if(filtro.getFechaAsignacion()!= null) {
                predicates.add(criteriaBuilder.equal(root.get("fecha"), filtro.getFechaAsignacion()));
            }

            if(filtro.getEstado()!= null) {
                predicates.add(criteriaBuilder.equal(root.get("estado"), filtro.getEstado()));
            }

            if(soloActivas) {
                predicates.add(criteriaBuilder.notEqual(root.get("estado"), AsignacionCampanaEstado.INACTIVA));
                predicates.add(criteriaBuilder.notEqual(root.get("estado"), AsignacionCampanaEstado.LIBERADA));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public RespuestaAsignacionDTO actualizarAsignacion(Integer idAsignacion, AsignacionCreacionDTO dto) {
        AsignacionTarea asignacionTareaExistente = asignacionTareaRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));

        if(dto.getFecha() != null) asignacionTareaExistente.setFecha(dto.getFecha());
        if(dto.getObservaciones()!= null) asignacionTareaExistente.setObservaciones(dto.getObservaciones());
        if(dto.getIdTarea()!= null) {
            Tarea nuevaTarea = tareaRepository.findById(dto.getIdTarea())
                    .orElseThrow(()-> new ResourceNotFoundException("Tarea no encontrada."));
            asignacionTareaExistente.setTarea(nuevaTarea);
        }
        if(dto.getIdCampana()!=  null) {
            Campana nuevaCampana = campanaRepository.findById(dto.getIdCampana())
                    .orElseThrow(() -> new ResourceNotFoundException("Campaaña no encontrada."));
            asignacionTareaExistente.setCampana(nuevaCampana);
        }
        if(dto.getIdEmpleado()!= null) {
            Empleado nuevoEmpleado = empleadoRepository.findById(dto.getIdEmpleado())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado."));
            asignacionTareaExistente.setEmpleado(nuevoEmpleado);
        }

        AsignacionTarea asignacionTareaActualizada = asignacionTareaRepository.save(asignacionTareaExistente);
        return mapToRespuestaAsignacionDTO(asignacionTareaActualizada);
    }

    @Transactional
    public RespuestaAsignacionDTO iniciarAsignacion(Integer idAsignacion) {
        AsignacionTarea asignacionTarea = asignacionTareaRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));

        if(asignacionTarea.getEstado()!= AsignacionCampanaEstado.ACTIVA) {
            throw new IllegalStateException("Solo se pueden iniciar asignaciones en el estado ACTIVA");
        }

        asignacionTarea.setEstado(AsignacionCampanaEstado.ACTIVA);
        return mapToRespuestaAsignacionDTO(asignacionTareaRepository.save(asignacionTarea));
    }

    @Transactional
    public RespuestaAsignacionDTO finalizarAsignacion(Integer idAsignacion) {
        AsignacionTarea asignacionTarea = asignacionTareaRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));

        if(asignacionTarea.getEstado()!= AsignacionCampanaEstado.ACTIVA) {
            throw new IllegalStateException("Solo se pueden finalizar asignaciones en el estado EN_PROCESO");
        }

        asignacionTarea.setEstado(AsignacionCampanaEstado.LIBERADA);
        return mapToRespuestaAsignacionDTO(asignacionTareaRepository.save(asignacionTarea));
    }

    @Transactional
    public RespuestaAsignacionDTO cancelarAsignacion(Integer idAsignacion) {
        AsignacionTarea asignacionTarea = asignacionTareaRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));

        if(asignacionTarea.getEstado() == AsignacionCampanaEstado.LIBERADA) {
            throw new IllegalStateException("No se puede cancelar una asignacion ya finalizada");
        }

        asignacionTarea.setEstado(AsignacionCampanaEstado.INACTIVA);

        AsignacionTarea asignacionTareaActualizada = asignacionTareaRepository.save(asignacionTarea);
        return mapToRespuestaAsignacionDTO(asignacionTareaActualizada);
    }

    @Transactional
    public RespuestaAsignacionDTO archivarAsignacion(Integer idAsignacion) {
        AsignacionTarea asignacionTarea = asignacionTareaRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));

        if(asignacionTarea.getEstado()!= AsignacionCampanaEstado.LIBERADA && asignacionTarea.getEstado()!= AsignacionCampanaEstado.INACTIVA) {
            throw new IllegalStateException("Solo  se pueden archivar asignaciones finalizadas o canceladas");
        }

        asignacionTarea.setEstado(AsignacionCampanaEstado.INACTIVA);
        AsignacionTarea asignacionTareaActualizada = asignacionTareaRepository.save(asignacionTarea);
        return  mapToRespuestaAsignacionDTO(asignacionTareaActualizada);
    }

    private RespuestaAsignacionDTO mapToRespuestaAsignacionDTO(AsignacionTarea asignacionTarea)  {
        return RespuestaAsignacionDTO.builder()
                .idAsignacion(asignacionTarea.getIdAsignacion())
                .fecha(asignacionTarea.getFecha())
                .idTarea(asignacionTarea.getTarea() != null ? asignacionTarea.getTarea().getIdTarea():  null)
                .nombreTarea(asignacionTarea.getTarea()!= null ? asignacionTarea.getTarea().getNombreTarea(): null)
                .detalles(asignacionTarea.getTarea()!= null ? asignacionTarea.getTarea().getDetalles(): null)
                .idCampana(asignacionTarea.getCampana() != null ? asignacionTarea.getCampana().getIdCampana(): null)
                .nombreCampana(asignacionTarea.getCampana()!= null ? asignacionTarea.getCampana().getNombreCampana(): null)
                .estado(asignacionTarea.getEstado())
                .build();
    }
}
