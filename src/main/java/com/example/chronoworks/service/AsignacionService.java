package com.example.chronoworks.service;

import com.example.chronoworks.dto.asignacion.AsignacionCreacionDTO;
import com.example.chronoworks.dto.asignacion.FiltroAsignacionDTO;
import com.example.chronoworks.dto.asignacion.RespuestaAsignacionDTO;
import com.example.chronoworks.exception.IllegalStateException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Asignacion;
import com.example.chronoworks.model.Campana;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.Tarea;
import com.example.chronoworks.model.enums.AsignacionEstado;
import com.example.chronoworks.repository.AsignacionRepository;
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
    public RespuestaAsignacionDTO crearAsignacion(AsignacionCreacionDTO dto) {

        Tarea tarea = tareaRepository.findById(dto.getIdTarea())
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada."));
        Empleado empleado = empleadoRepository.findById(dto.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado."));
        Campana campana = campanaRepository.findById(dto.getIdCampana())
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada."));

        Asignacion nuevaAsignacion = new Asignacion();
        nuevaAsignacion.setFecha(dto.getFecha());
        nuevaAsignacion.setObservaciones(dto.getObservaciones());
        nuevaAsignacion.setTarea(tarea);
        nuevaAsignacion.setEmpleado(empleado);
        nuevaAsignacion.setCampana(campana);
        nuevaAsignacion.setEstado(AsignacionEstado.ACTIVA);

        Asignacion asignacionGuardada = asignacionRepository.save(nuevaAsignacion);
        return mapToRespuestaAsignacionDTO(asignacionGuardada);
    }

    @Transactional(readOnly = true)
    public RespuestaAsignacionDTO obtenerAsignacion(Integer idAsignacion) {
        Asignacion asignacion = asignacionRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));
        return mapToRespuestaAsignacionDTO(asignacion);
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
        Specification<Asignacion> spec = crearSpecificationAsignacion(filtro, soloActivas);
        Page<Asignacion> asignacionesPage = asignacionRepository.findAll(spec, pageable);
        return asignacionesPage.map(this::mapToRespuestaAsignacionDTO);
    }

    private Specification<Asignacion> crearSpecificationAsignacion(FiltroAsignacionDTO filtro, boolean soloActivas) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(filtro.getNombreEmpleado()!= null && !filtro.getNombreEmpleado().trim().isEmpty()) {
                Join<Asignacion, Empleado> empleadoJoin = root.join("Empleado");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(empleadoJoin.get("nombre")),
                        "%" + filtro.getNombreEmpleado().toLowerCase() + "%"));
            }

            if(filtro.getApellidoEmpleado()!= null && !filtro.getApellidoEmpleado().trim().isEmpty()){
                Join<Asignacion, Empleado> empleadoJoin  = root.join("Empleado");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(empleadoJoin.get("apellido")),
                        "%" + filtro.getApellidoEmpleado().toLowerCase() + "%"));
            }

            if(filtro.getNombreCampana()!= null && !filtro.getNombreCampana().trim().isEmpty()) {
                Join<Asignacion, Campana> campanaJoin = root.join("Campana");
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
                predicates.add(criteriaBuilder.notEqual(root.get("estado"), AsignacionEstado.INACTIVA));
                predicates.add(criteriaBuilder.notEqual(root.get("estado"), AsignacionEstado.LIBERADA));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public RespuestaAsignacionDTO actualizarAsignacion(Integer idAsignacion, AsignacionCreacionDTO dto) {
        Asignacion asignacionExistente = asignacionRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));

        if(dto.getFecha() != null) asignacionExistente.setFecha(dto.getFecha());
        if(dto.getObservaciones()!= null) asignacionExistente.setObservaciones(dto.getObservaciones());
        if(dto.getIdTarea()!= null) {
            Tarea nuevaTarea = tareaRepository.findById(dto.getIdTarea())
                    .orElseThrow(()-> new ResourceNotFoundException("Tarea no encontrada."));
            asignacionExistente.setTarea(nuevaTarea);
        }
        if(dto.getIdCampana()!=  null) {
            Campana nuevaCampana = campanaRepository.findById(dto.getIdCampana())
                    .orElseThrow(() -> new ResourceNotFoundException("Campaaña no encontrada."));
            asignacionExistente.setCampana(nuevaCampana);
        }
        if(dto.getIdEmpleado()!= null) {
            Empleado nuevoEmpleado = empleadoRepository.findById(dto.getIdEmpleado())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado."));
            asignacionExistente.setEmpleado(nuevoEmpleado);
        }

        Asignacion asignacionActualizada = asignacionRepository.save(asignacionExistente);
        return mapToRespuestaAsignacionDTO(asignacionActualizada);
    }

    @Transactional
    public RespuestaAsignacionDTO iniciarAsignacion(Integer idAsignacion) {
        Asignacion asignacion= asignacionRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));

        if(asignacion.getEstado()!= AsignacionEstado.ACTIVA) {
            throw new IllegalStateException("Solo se pueden iniciar asignaciones en el estado ACTIVA");
        }

        asignacion.setEstado(AsignacionEstado.ACTIVA);
        return mapToRespuestaAsignacionDTO(asignacionRepository.save(asignacion));
    }

    @Transactional
    public RespuestaAsignacionDTO finalizarAsignacion(Integer idAsignacion) {
        Asignacion asignacion= asignacionRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));

        if(asignacion.getEstado()!= AsignacionEstado.ACTIVA) {
            throw new IllegalStateException("Solo se pueden finalizar asignaciones en el estado EN_PROCESO");
        }

        asignacion.setEstado(AsignacionEstado.LIBERADA);
        return mapToRespuestaAsignacionDTO(asignacionRepository.save(asignacion));
    }

    @Transactional
    public RespuestaAsignacionDTO cancelarAsignacion(Integer idAsignacion) {
        Asignacion asignacion = asignacionRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));

        if(asignacion.getEstado() == AsignacionEstado.LIBERADA) {
            throw new IllegalStateException("No se puede cancelar una asignacion ya finalizada");
        }

        asignacion.setEstado(AsignacionEstado.INACTIVA);

        Asignacion asignacionActualizada = asignacionRepository.save(asignacion);
        return mapToRespuestaAsignacionDTO(asignacionActualizada);
    }

    @Transactional
    public RespuestaAsignacionDTO archivarAsignacion(Integer idAsignacion) {
        Asignacion asignacion = asignacionRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));

        if(asignacion.getEstado()!= AsignacionEstado.LIBERADA && asignacion.getEstado()!= AsignacionEstado.INACTIVA) {
            throw new IllegalStateException("Solo  se pueden archivar asignaciones finalizadas o canceladas");
        }

        asignacion.setEstado(AsignacionEstado.INACTIVA);
        Asignacion asignacionActualizada = asignacionRepository.save(asignacion);
        return  mapToRespuestaAsignacionDTO(asignacionActualizada);
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
