package com.example.chronoworks.service;

import com.example.chronoworks.dto.asignacion.AsignacionConsultaDTO;
import com.example.chronoworks.dto.campana.AsignacionCampanaDTO;
import com.example.chronoworks.dto.campana.CampanaDTO;
import com.example.chronoworks.dto.campana.FiltroCampanaDTO;
import com.example.chronoworks.dto.campana.RespuestaCampanaDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.IllegalStateException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.*;
import com.example.chronoworks.model.enums.AsignacionEstado;
import com.example.chronoworks.model.enums.CampanaEstado;
import com.example.chronoworks.repository.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CampanaService {

    private final CampanaRepository campanaRepository;
    private final EmpresaRepository empresaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AsignacionRepository asignacionRepository;
    private final RolRepository rolRepository;

    public CampanaService(CampanaRepository campanaRepository,
                          EmpresaRepository empresaRepository,
                          EmpleadoRepository empleadoRepository,
                          AsignacionRepository asignacionRepository,
                          RolRepository rolRepository) {
        this.campanaRepository = campanaRepository;
        this.empresaRepository = empresaRepository;
        this.empleadoRepository = empleadoRepository;
        this.asignacionRepository = asignacionRepository;
        this.rolRepository = rolRepository;
    }

    @Transactional
    public RespuestaCampanaDTO crearCampana(CampanaDTO dto, List<AsignacionCampanaDTO> asignacionesDTO) {
        if (campanaRepository.findByNombreCampana(dto.getNombreCampana()).isPresent()) {
            throw new BadRequestException("El nombre de la campaña ya está en uso");
        }

        Empresa empresa = empresaRepository.findById(dto.getIdEmpresa())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada."));

        Campana nuevaCampana = new Campana();
        nuevaCampana.setNombreCampana(dto.getNombreCampana());
        nuevaCampana.setDescripcion(dto.getDescripcion());
        nuevaCampana.setFechaInicio(dto.getFechaInicio());
        nuevaCampana.setFechaFin(dto.getFechaFin());
        nuevaCampana.setEmpresa(empresa);
        nuevaCampana.setEstado(CampanaEstado.ACTIVA);

        Campana campanaGuardada = campanaRepository.save(nuevaCampana);

        procesarAsignaciones(campanaGuardada, asignacionesDTO, empresa);

        return mapToRespuestaCampanaDTO(campanaGuardada);
    }

    private void procesarAsignaciones(Campana campana, List<AsignacionCampanaDTO> asignacionesDTO, Empresa empresa) {
        long cuentaLideres = asignacionesDTO.stream().filter(AsignacionCampanaDTO::getEsLider).count();

        if (cuentaLideres != 1) {
            throw new BadRequestException("Debe haber exactamente un lider por campaña");
        }

        Rol rolLider = rolRepository.findByNombreRolAndEmpresa("LIDER", empresa.getIdEmpresa())
                .orElseThrow(() -> new ResourceNotFoundException("Rol Líder no configurado"));

        Rol rolAgente = rolRepository.findByNombreRolAndEmpresa("AGENTE", empresa.getIdEmpresa())
                .orElseThrow(() -> new ResourceNotFoundException("Rol Agente no configurado"));

        for (AsignacionCampanaDTO asignacionDTO : asignacionesDTO) {
            Empleado empleado = empleadoRepository.findById(asignacionDTO.getIdEmpleado())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));

            if (asignacionDTO.getEsLider() && !empleado.getRoles().contains(rolLider)) {
                throw new BadRequestException("El empleado no tiene rol de Líder");
            }

            if (asignacionDTO.getEsLider() && !empleado.getRoles().contains(rolAgente)) {
                throw new BadRequestException("El empleado no tiene rol de Agente");
            }

            Asignacion asignacion = new Asignacion();
            asignacion.setCampana(campana);
            asignacion.setEmpleado(empleado);
            asignacion.setEsLider(asignacionDTO.getEsLider());
            asignacion.setEstado(AsignacionEstado.ACTIVA);
            asignacion.setFecha(LocalDateTime.now());
            asignacionRepository.save(asignacion);
        }
    }

    @Transactional(readOnly = true)
    public RespuestaCampanaDTO obtenerCampana(Integer idCampana) {
        Campana campana = campanaRepository.findById(idCampana)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada."));
        return mapToRespuestaCampanaDTO(campana);
    }

    @Transactional(readOnly = true)
    public Page<RespuestaCampanaDTO> listarCampanas(FiltroCampanaDTO filtro, Pageable pageable) {
        return listarCampanasConFiltros(filtro, false, pageable);
    }

    @Transactional(readOnly = true)
    public Page<RespuestaCampanaDTO> listarCampanasActivas(FiltroCampanaDTO filtro, Pageable pageable) {
        return listarCampanasConFiltros(filtro, true, pageable);
    }

    @Transactional
    public Page<RespuestaCampanaDTO> listarCampanasConFiltros(FiltroCampanaDTO filtro, boolean soloActivas, Pageable pageable) {
        Specification<Campana> spec = crearSpecificationCampana(filtro, soloActivas);
        Page<Campana> campanasPage = campanaRepository.findAll(spec, pageable);
        return campanasPage.map(this::mapToRespuestaCampanaDTO);
    }

    @Transactional
    public Specification<Campana> crearSpecificationCampana(FiltroCampanaDTO filtro, boolean soloActivas) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getNombreCampana() != null && !filtro.getNombreCampana().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nombreCampana")), "%" + filtro.getNombreCampana().toLowerCase() + "%"));
            }

            if (filtro.getNombreEmpresa() != null && !filtro.getNombreEmpresa().trim().isEmpty()) {
                Join<Campana, Empresa> empresaJoin = root.join("empresa");
                predicates.add(cb.like(cb.lower(empresaJoin.get("nombreEmpresa")),
                        "%" + filtro.getNombreEmpresa().toLowerCase() + "%"));
            }

            if (filtro.getEstados() != null && !filtro.getEstados().isEmpty()) {
                predicates.add(root.get("estado").in(filtro.getEstados()));
            } else if (Boolean.TRUE.equals(filtro.getExcluirArchivadas())) {
                predicates.add(cb.notEqual(root.get("estado"), CampanaEstado.ARCHIVADA));
            }

            if (soloActivas) {
                predicates.add(cb.notEqual(root.get("estado"), CampanaEstado.ARCHIVADA));
                predicates.add(cb.notEqual(root.get("estado"), CampanaEstado.CANCELADA));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public RespuestaCampanaDTO actualizarCampana(
            Integer idCampana,
            CampanaDTO dto,
            List<AsignacionCampanaDTO> asignacionesDTO) {

        Campana campanaExistente = campanaRepository.findById(idCampana)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada."));

        if (!dto.getNombreCampana().equals(campanaExistente.getNombreCampana()) &&
                campanaRepository.findByNombreCampana(dto.getNombreCampana()).isPresent()) {
            throw new BadRequestException("El nuevo nombre de la campaña ya está en uso");
        }

        campanaExistente.setNombreCampana(dto.getNombreCampana());
        campanaExistente.setDescripcion(dto.getDescripcion());
        campanaExistente.setFechaInicio(dto.getFechaInicio());
        campanaExistente.setFechaFin(dto.getFechaFin());

        if (dto.getIdEmpresa() != null) {
            Empresa nuevaEmpresa = empresaRepository.findById(dto.getIdEmpresa())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
            campanaExistente.setEmpresa(nuevaEmpresa);
        }

        Campana campanaActualizada = campanaRepository.save(campanaExistente);

        if (asignacionesDTO != null && !asignacionesDTO.isEmpty()) {
            asignacionRepository.deleteByCampanaIdCampana(idCampana);

            procesarAsignaciones(campanaActualizada, asignacionesDTO, campanaActualizada.getEmpresa());
        }
        return mapToRespuestaCampanaDTO(campanaActualizada);
    }



    @Transactional
    public RespuestaCampanaDTO cambiarEstado(Integer idCampana, CampanaEstado nuevoEstado, boolean liberarEmpleados) {
        Campana campana = campanaRepository.findById(idCampana)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada."));

        validarTransicionEstado(campana.getEstado(), nuevoEstado);

        if (liberarEmpleados && (nuevoEstado == CampanaEstado.FINALIZADA || nuevoEstado == CampanaEstado.CANCELADA)) {
            liberarEmpleadosDeCampana(idCampana);
        }

        campana.setEstado(nuevoEstado);
        return mapToRespuestaCampanaDTO(campanaRepository.save(campana));
    }

    private void validarTransicionEstado(CampanaEstado estadoActual, CampanaEstado nuevoEstado) {
        switch (estadoActual) {
            case ACTIVA:
                if (nuevoEstado != CampanaEstado.EN_PROCESO) {
                    throw new IllegalStateException("Solo se puede cambiar de ACTIVA a EN_PROCESO");
                }
                break;
            case EN_PROCESO:
                if (nuevoEstado != CampanaEstado.FINALIZADA && nuevoEstado != CampanaEstado.CANCELADA) {
                    throw new IllegalStateException("Solo se puede finalizar o cancelar una campaña EN_PROCESO");
                }
                break;
            case FINALIZADA:
            case CANCELADA:
                if (nuevoEstado != CampanaEstado.ARCHIVADA) {
                    throw new IllegalStateException("Solo se puede archivar una campaña FINALIZADA o CANCELADA");
                }
                break;
            case ARCHIVADA:
                throw new IllegalStateException("No se puede cambiar el estado de una campaña ARCHIVADA");
        }
    }

    private void liberarEmpleadosDeCampana(Integer idCampana) {
        List<Asignacion> asignaciones = asignacionRepository.findByCampanaId(idCampana);
        asignaciones.forEach(asignacion -> {
            asignacion.setEstado(AsignacionEstado.LIBERADA);
            asignacionRepository.save(asignacion);
        });
    }

    @Transactional(readOnly = true)
    public List<AsignacionConsultaDTO> obtenerAsignaciones(Integer idCampana) {
        List<Asignacion> asignaciones = asignacionRepository.findAgentesByCampanaId(idCampana);
        return asignaciones.stream()
                .map(this::convertirAsignacionADTO)
                .collect(Collectors.toList());
    }

    private AsignacionConsultaDTO convertirAsignacionADTO(Asignacion asignacion) {
        AsignacionConsultaDTO dto = new AsignacionConsultaDTO();
        dto.setIdAsignacion(asignacion.getIdAsignacion());
        dto.setIdEmpleado(asignacion.getEmpleado().getIdEmpleado());
        dto.setNombreEmpleado(asignacion.getEmpleado().getNombre());
        dto.setApellidoEmpleado(asignacion.getEmpleado().getApellido());
        dto.setEsLider(asignacion.getEsLider());
        dto.setEstadoAsignacion(asignacion.getEstado().name());
        dto.setFechaAsignacion(asignacion.getFecha());
        return dto;
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteExcelCampanas(FiltroCampanaDTO filtro) throws IOException {
        Page<Campana> campanaPage = campanaRepository.findAll(
                crearSpecificationCampana(filtro, false),
                Pageable.unpaged()
        );
        List<Campana> campanas = campanaPage.getContent();

        try(Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte Campañas");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {
                    "Nombre Campaña",
                    "Descripción",
                    "Fecha Inicio",
                    "Fecha Fin",
                    "Estado",
                    "Empresa",
                    "Asignaciones"
            };

            Row headerRow = sheet.createRow(0);
            for(int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Campana campana : campanas) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(campana.getNombreCampana());
                row.createCell(1).setCellValue(campana.getDescripcion() != null ? campana.getDescripcion() : "");
                row.createCell(2).setCellValue(campana.getFechaInicio().toString());
                row.createCell(3).setCellValue(campana.getFechaFin().toString());
                row.createCell(4).setCellValue(campana.getEstado().toString());
                row.createCell(5).setCellValue(campana.getEmpresa() != null ? campana.getEmpresa().getNombreEmpresa() : "Sin empresa");

                if (campana.getAsignaciones() != null && !campana.getAsignaciones().isEmpty()) {
                    String asignaciones = campana.getAsignaciones().stream()
                            .map(asignacion -> {
                                return asignacion.getEmpleado() != null ? asignacion.getEmpleado().getNombre() : "Empleado no asignado";
                            })
                            .collect(Collectors.joining(", "));
                    row.createCell(6).setCellValue(asignaciones);
                } else {
                    row.createCell(6).setCellValue("Sin asignaciones");
                }
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private RespuestaCampanaDTO mapToRespuestaCampanaDTO(Campana campana) {
        RespuestaCampanaDTO dto = RespuestaCampanaDTO.builder()
                .idCampana(campana.getIdCampana())
                .nombreCampana(campana.getNombreCampana())
                .descripcion(campana.getDescripcion())
                .fechaInicio(campana.getFechaInicio())
                .fechaFin(campana.getFechaFin())
                .idEmpresa(campana.getEmpresa() != null ? campana.getEmpresa().getIdEmpresa() : null)
                .nombreEmpresa(campana.getEmpresa() != null ? campana.getEmpresa().getNombreEmpresa() : null)
                .estado(campana.getEstado())
                .build();

        Optional<Asignacion> liderAsignacion = asignacionRepository.findLiderByCampanaId(campana.getIdCampana());
        liderAsignacion.ifPresent(asignacion -> {
            dto.setIdLider(asignacion.getEmpleado().getIdEmpleado());
            dto.setNombreLider(asignacion.getEmpleado().getNombre() + " " + asignacion.getEmpleado().getApellido());
        });

        List<Asignacion> agentesAsignaciones = asignacionRepository.findAgentesByCampanaId(campana.getIdCampana());
        if (!agentesAsignaciones.isEmpty()) {
            dto.setIdsAgentes(agentesAsignaciones.stream()
                    .map(a -> a.getEmpleado().getIdEmpleado())
                    .collect(Collectors.toList()));

            dto.setNombresAgentes(agentesAsignaciones.stream()
                    .map(a -> a.getEmpleado().getNombre() + " " + a.getEmpleado().getApellido())
                    .collect(Collectors.joining(", ")));
        }
        return dto;
    }
}
