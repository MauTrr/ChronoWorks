package com.example.chronoworks.service;

import com.example.chronoworks.dto.campana.CampanaDTO;
import com.example.chronoworks.dto.campana.FiltroCampanaDTO;
import com.example.chronoworks.dto.campana.RespuestaCampanaDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.IllegalStateException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Campana;
import com.example.chronoworks.model.Empresa;
import com.example.chronoworks.model.enums.CampanaEstado;
import com.example.chronoworks.repository.CampanaRepository;
import com.example.chronoworks.repository.EmpresaRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampanaService {

    private final CampanaRepository campanaRepository;
    private final EmpresaRepository empresaRepository;

    public CampanaService(CampanaRepository campanaRepository,
                          EmpresaRepository empresaRepository) {
        this.campanaRepository = campanaRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public RespuestaCampanaDTO crearCampana(CampanaDTO dto) {
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
        return mapToRespuestaCampanaDTO(campanaGuardada);
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

            if (filtro.getEstado() != null) {
                predicates.add(cb.equal(root.get("estado"), filtro.getEstado()));
            }

            if (soloActivas) {
                predicates.add(cb.notEqual(root.get("estado"), CampanaEstado.ARCHIVADA));
                predicates.add(cb.notEqual(root.get("estado"), CampanaEstado.CANCELADA));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public RespuestaCampanaDTO actualizarCampana(Integer idCampana, CampanaDTO dto) {
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
        return mapToRespuestaCampanaDTO(campanaActualizada);
    }

    @Transactional
    public RespuestaCampanaDTO iniciarCampana(Integer idCampana) {
        Campana campana = campanaRepository.findById(idCampana)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada."));

        if (campana.getEstado() != CampanaEstado.ACTIVA) {
            throw new IllegalStateException("Solo se pueden iniciar campañas en el estado ACTIVA");
        }

        campana.setEstado(CampanaEstado.EN_PROCESO);
        return mapToRespuestaCampanaDTO(campanaRepository.save(campana));
    }

    @Transactional
    public RespuestaCampanaDTO finalizarCampana(Integer idCampana) {
        Campana campana = campanaRepository.findById(idCampana)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada."));

        if (campana.getEstado() != CampanaEstado.EN_PROCESO) {
            throw new IllegalStateException("Solo se pueden finalizar campañas en el estado EN_PROCESO");
        }

        campana.setEstado(CampanaEstado.FINALIZADA);
        return mapToRespuestaCampanaDTO(campanaRepository.save(campana));
    }

    @Transactional
    public RespuestaCampanaDTO cancelarCampana(Integer idCampana) {
        Campana campana = campanaRepository.findById(idCampana)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada."));

        if (campana.getEstado() == CampanaEstado.FINALIZADA) {
            throw new IllegalStateException("No se puede cancelar una campaña ya finalizada");
        }

        campana.setEstado(CampanaEstado.CANCELADA);
        return mapToRespuestaCampanaDTO(campanaRepository.save(campana));
    }

    @Transactional
    public RespuestaCampanaDTO archivarCampana(Integer idCampana) {
        Campana campana = campanaRepository.findById(idCampana)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada."));

        if (campana.getEstado() != CampanaEstado.FINALIZADA &&
                campana.getEstado() != CampanaEstado.CANCELADA) {
            throw new IllegalStateException("Solo se pueden archivar campañas finalizadas o canceladas");
        }

        campana.setEstado(CampanaEstado.ARCHIVADA);
        return mapToRespuestaCampanaDTO(campanaRepository.save(campana));
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
        return RespuestaCampanaDTO.builder()
                .idCampana(campana.getIdCampana())
                .nombreCampana(campana.getNombreCampana())
                .descripcion(campana.getDescripcion())
                .fechaInicio(campana.getFechaInicio())
                .fechaFin(campana.getFechaFin())
                .idEmpresa(campana.getEmpresa() != null ? campana.getEmpresa().getIdEmpresa() : null)
                .nombreEmpresa(campana.getEmpresa() != null ? campana.getEmpresa().getNombreEmpresa() : null)
                .estado(campana.getEstado())
                .build();
    }
}
