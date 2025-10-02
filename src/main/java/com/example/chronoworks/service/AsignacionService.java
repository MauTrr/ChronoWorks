package com.example.chronoworks.service;

import com.example.chronoworks.dto.asignacion.*;
import com.example.chronoworks.exception.IllegalStateException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.*;
import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
import com.example.chronoworks.model.enums.CampanaEstado;
import com.example.chronoworks.repository.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AsignacionService {

    private final AsignacionTareaRepository asignacionTareaRepository;
    private final AsignacionEmpleadoTareaRepository asignacionEmpleadoTareaRepository;
    private final AsignacionCampanaRepository asignacionCampanaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final CampanaRepository campanaRepository;
    private final TareaRepository tareaRepository;

    public AsignacionService(AsignacionTareaRepository asignacionTareaRepository,
                             AsignacionEmpleadoTareaRepository asignacionEmpleadoTareaRepository,
                             AsignacionCampanaRepository asignacionCampanaRepository,
                             EmpleadoRepository empleadoRepository,
                             CampanaRepository campanaRepository,
                             TareaRepository tareaRepository) {
        this.asignacionTareaRepository = asignacionTareaRepository;
        this.asignacionEmpleadoTareaRepository = asignacionEmpleadoTareaRepository;
        this.asignacionCampanaRepository = asignacionCampanaRepository;
        this.empleadoRepository = empleadoRepository;
        this.campanaRepository = campanaRepository;
        this.tareaRepository = tareaRepository;

    }

    @Transactional
    public RespuestaAsignacionCompletaDTO crearAsignacion(AsignacionCompletaDTO dto) {
        // 1. Validar y obtener entidades principales
        Tarea tarea = tareaRepository.findById(dto.getIdTarea())
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada."));
        Campana campana = campanaRepository.findById(dto.getIdCampana())
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada."));

        // 2. Crear la asignación de tarea principal
        AsignacionTarea nuevaAsignacionTarea = new AsignacionTarea();
        nuevaAsignacionTarea.setFecha(dto.getFecha());
        nuevaAsignacionTarea.setTarea(tarea);
        nuevaAsignacionTarea.setCampana(campana);
        nuevaAsignacionTarea.setEstado(dto.getEstado());

        AsignacionTarea asignacionTareaGuardada = asignacionTareaRepository.save(nuevaAsignacionTarea);

        // 3. Crear asignaciones para cada empleado
        List<RespuestaAsignacionEmpleadoDTO> empleadosAsignados = new ArrayList<>();
        if (dto.getEmpleados() != null && !dto.getEmpleados().isEmpty()) {
            for (AsignacionEmpleadoDTO empleadoDTO : dto.getEmpleados()) {
                Empleado empleado = empleadoRepository.findById(empleadoDTO.getIdEmpleado())
                        .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));

                AsignacionEmpleadoTarea asignacionEmpleado = new AsignacionEmpleadoTarea();
                asignacionEmpleado.setEmpleado(empleado);
                asignacionEmpleado.setAsignacionTarea(asignacionTareaGuardada);
                asignacionEmpleado.setFechaAsignacion(LocalDateTime.now());
                asignacionEmpleado.setFechaInicio(empleadoDTO.getFechaInicio());
                asignacionEmpleado.setFechaFin(empleadoDTO.getFechaFin());
                asignacionEmpleado.setEstado(empleadoDTO.getEstado());

                AsignacionEmpleadoTarea asignacionGuardada = asignacionEmpleadoTareaRepository.save(asignacionEmpleado);
                empleadosAsignados.add(mapToRespuestaAsignacionEmpleadoDTO(asignacionGuardada));

            }
        }
        return buildRespuestaCompleta(asignacionTareaGuardada, empleadosAsignados);
    }

    @Transactional(readOnly = true)
    public RespuestaAsignacionCompletaDTO obtenerAsignacion(Integer idAsignacion) {
        AsignacionTarea asignacionTarea = asignacionTareaRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));

        List<AsignacionEmpleadoTarea> asignacionesEmpleados =
                asignacionEmpleadoTareaRepository.findByAsignacionTarea(asignacionTarea);

        List<RespuestaAsignacionEmpleadoDTO> empleadosAsignados = asignacionesEmpleados.stream()
                .map(this::mapToRespuestaAsignacionEmpleadoDTO)
                .toList();

        return buildRespuestaCompleta(asignacionTarea, empleadosAsignados);
    }


    @Transactional(readOnly = true)
    public Page<RespuestaAsignacionCompletaDTO> listarAsignaciones(FiltroAsignacionCompletaDTO filtro, Pageable pageable) {
        Specification<AsignacionTarea> spec = crearSpecification(filtro);
        Page<AsignacionTarea> asignacionesPage = asignacionTareaRepository.findAll(spec, pageable);

        return asignacionesPage.map(asignacion -> {
            List<AsignacionEmpleadoTarea> asignacionesEmpleados =
                    asignacionEmpleadoTareaRepository.findByAsignacionTarea(asignacion);

            List<RespuestaAsignacionEmpleadoDTO> empleadosAsignados = asignacionesEmpleados.stream()
                    .map(this::mapToRespuestaAsignacionEmpleadoDTO)
                    .collect(Collectors.toList());

            return buildRespuestaCompleta(asignacion, empleadosAsignados);
        });
    }

    @Transactional
    public RespuestaAsignacionCompletaDTO actualizarAsignacion(Integer idAsignacion, AsignacionCompletaDTO dto) {
        AsignacionTarea asignacionExistente = asignacionTareaRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada."));

        if(dto.getFecha() != null) asignacionExistente.setFecha(dto.getFecha());
        if (dto.getEstado() != null) asignacionExistente.setEstado(dto.getEstado());
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

        AsignacionTarea asignacionActualizada = asignacionTareaRepository.save(asignacionExistente);

        List<RespuestaAsignacionEmpleadoDTO> empleadosAsignados = new ArrayList<>();
        if (dto.getEmpleados() != null) {
            List<AsignacionEmpleadoTarea> empleadosActuales = asignacionEmpleadoTareaRepository.findByAsignacionTarea(asignacionActualizada);
            empleadosAsignados = empleadosActuales.stream().map(this::mapToRespuestaAsignacionEmpleadoDTO).toList();
        }

        return buildRespuestaCompleta(asignacionActualizada, empleadosAsignados);
    }

    @Transactional
    public RespuestaAsignacionCompletaDTO agregarEmpleadoAsignacion(Integer idAsignacion, AsignacionEmpleadoDTO empleadoDTO) {
        AsignacionTarea asignacion = asignacionTareaRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada"));
        Empleado empleado = empleadoRepository.findById(empleadoDTO.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));

        boolean yaAsignado = asignacionEmpleadoTareaRepository.existsByEmpleadoAndAsignacionTarea(empleado, asignacion);
        if (yaAsignado) {
            throw new IllegalStateException("El empleado ya esta asignado a esta tarea");
        }

        AsignacionEmpleadoTarea nuevaAsignacion = new AsignacionEmpleadoTarea();
        nuevaAsignacion.setEmpleado(empleado);
        nuevaAsignacion.setAsignacionTarea(asignacion);
        nuevaAsignacion.setFechaAsignacion(LocalDateTime.now());
        nuevaAsignacion.setFechaInicio(empleadoDTO.getFechaInicio());
        nuevaAsignacion.setFechaFin(empleadoDTO.getFechaFin());
        nuevaAsignacion.setEstado(empleadoDTO.getEstado());

        asignacionEmpleadoTareaRepository.save(nuevaAsignacion);

        return obtenerAsignacion(idAsignacion);
    }

    @Transactional
    public RespuestaAsignacionCompletaDTO actualizarEmpleadoAsignacion(Integer idAsignacion, Integer idEmpleado, AsignacionEmpleadoDTO empleadoDTO) {
        AsignacionEmpleadoTarea asignacionEmpleado = asignacionEmpleadoTareaRepository.findByAsignacionTareaIdAsignacionAndEmpleadoIdEmpleado(idAsignacion, idEmpleado)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion de empleado no encontrada"));

        if (empleadoDTO.getFechaInicio() != null) {
            asignacionEmpleado.setFechaInicio((empleadoDTO.getFechaInicio()));
        }
        if (empleadoDTO.getFechaFin() != null) {
            asignacionEmpleado.setFechaFin(empleadoDTO.getFechaFin());
        }
        if (empleadoDTO.getEstado() != null) {
            asignacionEmpleado.setEstado(empleadoDTO.getEstado());
        }

        asignacionEmpleadoTareaRepository.save(asignacionEmpleado);

        return obtenerAsignacion(idAsignacion);
    }

    @Transactional
    public RespuestaAsignacionCompletaDTO cambiarEstado(Integer idAsignacion, AsignacionCampanaEstado nuevoEstado) {
        AsignacionTarea asignacion = asignacionTareaRepository.findById(idAsignacion)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada"));

        asignacion.setEstado(nuevoEstado);
        asignacionTareaRepository.save(asignacion);

        return obtenerAsignacion(idAsignacion);
    }

    private Specification<AsignacionTarea> crearSpecification(FiltroAsignacionCompletaDTO filtro) {
        return crearSpecification(filtro, false);
    }

    private Specification<AsignacionTarea> crearSpecification(FiltroAsignacionCompletaDTO filtro, boolean soloActivas) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Para filtrar por empleado, necesitas hacer join a través de AsignacionEmpleadoTarea
            if(filtro.getNombreEmpleado() != null && !filtro.getNombreEmpleado().trim().isEmpty()) {
                Join<AsignacionTarea, AsignacionEmpleadoTarea> empleadoTareaJoin = root.join("asignacionesEmpleados");
                Join<AsignacionEmpleadoTarea, Empleado> empleadoJoin = empleadoTareaJoin.join("empleado");

                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(empleadoJoin.get("nombre")),
                        "%" + filtro.getNombreEmpleado().toLowerCase() + "%"
                ));
            }

            if(filtro.getApellidoEmpleado() != null && !filtro.getApellidoEmpleado().trim().isEmpty()){
                Join<AsignacionTarea, AsignacionEmpleadoTarea> empleadoTareaJoin = root.join("asignacionesEmpleados");
                Join<AsignacionEmpleadoTarea, Empleado> empleadoJoin = empleadoTareaJoin.join("empleado");

                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(empleadoJoin.get("apellido")),
                        "%" + filtro.getApellidoEmpleado().toLowerCase() + "%"
                ));
            }

            // Para campaña, usa el nombre correcto de la relación
            if(filtro.getNombreCampana() != null && !filtro.getNombreCampana().trim().isEmpty()) {
                Join<AsignacionTarea, Campana> campanaJoin = root.join("campana"); // ← minúscula
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(campanaJoin.get("nombreCampana")),
                        "%" + filtro.getNombreCampana().toLowerCase() + "%"
                ));
            }

            if(filtro.getFechaAsignacion() != null) {
                predicates.add(criteriaBuilder.equal(root.get("fecha"), filtro.getFechaAsignacion()));
            }

            if(filtro.getEstado() != null) {
                predicates.add(criteriaBuilder.equal(root.get("estado"), filtro.getEstado()));
            }

            if(soloActivas) {
                predicates.add(criteriaBuilder.notEqual(root.get("estado"), AsignacionCampanaEstado.INACTIVA));
                predicates.add(criteriaBuilder.notEqual(root.get("estado"), AsignacionCampanaEstado.LIBERADA));
            }

            // Para evitar duplicados cuando hay múltiples empleados
            if (filtro.getNombreEmpleado() != null || filtro.getApellidoEmpleado() != null) {
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public List<RespuestaAsignacionCompletaDTO> obtenerAsignacionesPorLider(Integer idEmpleado) {
        // 1. Primero obtener la campaña del líder
        Optional<AsignacionCampana> asignacionLider = asignacionCampanaRepository
                .findByEmpleadoIdEmpleadoAndEsLiderAndEstadoAndCampanaEstadoIn(
                        idEmpleado,
                        true,
                        AsignacionCampanaEstado.ACTIVA,
                        Arrays.asList(CampanaEstado.ACTIVA, CampanaEstado.EN_PROCESO)
                );

        if (asignacionLider.isEmpty()) {
            return new ArrayList<>(); // Retorna lista vacía si no es líder de campaña activa
        }

        Campana campanaLider = asignacionLider.get().getCampana();

        // 2. Obtener todas las asignaciones de tareas de esa campaña
        List<AsignacionTarea> asignacionesCampana = asignacionTareaRepository
                .findByCampanaIdCampana(campanaLider.getIdCampana());

        // 3. Convertir a DTOs
        return asignacionesCampana.stream()
                .map(asignacion -> {
                    List<AsignacionEmpleadoTarea> asignacionesEmpleados =
                            asignacionEmpleadoTareaRepository.findByAsignacionTarea(asignacion);

                    List<RespuestaAsignacionEmpleadoDTO> empleadosAsignados = asignacionesEmpleados.stream()
                            .map(this::mapToRespuestaAsignacionEmpleadoDTO)
                            .collect(Collectors.toList());

                    return buildRespuestaCompleta(asignacion, empleadosAsignados);
                })
                .collect(Collectors.toList());
    }


    private RespuestaAsignacionEmpleadoDTO mapToRespuestaAsignacionEmpleadoDTO(AsignacionEmpleadoTarea asignacion) {
        return RespuestaAsignacionEmpleadoDTO.builder()
                .idAsignacion(asignacion.getIdAsignacionEmpleadoTarea())
                .idEmpleado(asignacion.getEmpleado().getIdEmpleado())
                .nombreEmpleado(asignacion.getEmpleado().getNombre())
                .apellidoEmpleado(asignacion.getEmpleado().getApellido())
                .fechaAsignacion(asignacion.getFechaAsignacion())
                .fechaInicio(asignacion.getFechaInicio())
                .fechaFin(asignacion.getFechaFin())
                .estado(asignacion.getEstado())
                .build();
    }

    private RespuestaAsignacionCompletaDTO buildRespuestaCompleta(AsignacionTarea asignacion, List<RespuestaAsignacionEmpleadoDTO> empleados) {
        return RespuestaAsignacionCompletaDTO.builder()
                .idAsignacion(asignacion.getIdAsignacion())
                .fecha(asignacion.getFecha())
                .estado(asignacion.getEstado())
                .idTarea(asignacion.getTarea().getIdTarea())
                .nombreTarea(asignacion.getTarea().getNombreTarea())
                .idCampana(asignacion.getCampana().getIdCampana())
                .nombreCampana(asignacion.getCampana().getNombreCampana())
                .empleados(empleados)
                .build();
    }
}
