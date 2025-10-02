package com.example.chronoworks.service;

import com.example.chronoworks.dto.asignacion.AsignacionConsultaDTO;
import com.example.chronoworks.dto.campana.*;
import com.example.chronoworks.dto.pdf.CampanaPDFDTO;
import com.example.chronoworks.dto.pdf.ReporteCampanaPDFDTO;
import com.example.chronoworks.dto.pdf.ResumenReporteDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.IllegalStateException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.*;
import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
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
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CampanaService {

    private final CampanaRepository campanaRepository;
    private final EmpresaRepository empresaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AsignacionCampanaRepository asignacionCampanaRepository;
    private final CredencialRepository credencialRepository;
    private final PDFGeneratorService pdfGeneratorService;

    public CampanaService(CampanaRepository campanaRepository,
                          EmpresaRepository empresaRepository,
                          EmpleadoRepository empleadoRepository,
                          AsignacionCampanaRepository asignacionCampanaRepository,
                          CredencialRepository credencialRepository,
                          PDFGeneratorService pdfGeneratorService) {
        this.campanaRepository = campanaRepository;
        this.empresaRepository = empresaRepository;
        this.empleadoRepository = empleadoRepository;
        this.asignacionCampanaRepository = asignacionCampanaRepository;
        this.credencialRepository = credencialRepository;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    @Transactional
    public RespuestaCampanaDTO crearCampana(CampanaDTO dto, List<AsignacionCampanaDTO> asignacionesDTO) {
        List<Campana> campanasConMismoNombre = campanaRepository.findByNombreCampana(dto.getNombreCampana());
        if (!campanasConMismoNombre.isEmpty()) {
            throw new BadRequestException("El nombre de la campaña ya esta en uso");
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

        validarDisponibilidadEmpleados(asignacionesDTO);

        for (AsignacionCampanaDTO asignacionDTO : asignacionesDTO) {
            Empleado empleado = empleadoRepository.findById(asignacionDTO.getIdEmpleado())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));

            Credencial credencial = credencialRepository.findByEmpleado(empleado)
                    .orElseThrow(() -> new ResourceNotFoundException("Credencial no encontrada para el empleado"));


            Rol rolEmpleado = credencial.getRol();

            // Validación para líder
            if (asignacionDTO.getEsLider()) {
                if (!"LIDER".equalsIgnoreCase(rolEmpleado.getNombreRol())) {
                    throw new BadRequestException("El empleado no tiene rol de Líder. Rol actual: " + rolEmpleado.getNombreRol());
                }
            }
            // Validación para agentes
            else {
                if (!"AGENTE".equalsIgnoreCase(rolEmpleado.getNombreRol())) {
                    throw new BadRequestException("El empleado no tiene rol de Agente. Rol actual: " + rolEmpleado.getNombreRol());
                }
            }

            AsignacionCampana asignacion = new AsignacionCampana();
            asignacion.setCampana(campana);
            asignacion.setEmpleado(empleado);
            asignacion.setEsLider(asignacionDTO.getEsLider());
            asignacion.setEstado(AsignacionCampanaEstado.ACTIVA);
            asignacion.setFechaAsignacion(LocalDateTime.now());

            asignacionCampanaRepository.save(asignacion);
        }
    }

    private void validarDisponibilidadEmpleados(List<AsignacionCampanaDTO> asignacionesDTO) {
        for (AsignacionCampanaDTO asignacionDTO : asignacionesDTO) {
            List<AsignacionCampana> asignacionesActivas = asignacionCampanaRepository.findByEmpleadoIdEmpleadoAndEstado(
                    asignacionDTO.getIdEmpleado(),
                    AsignacionCampanaEstado.ACTIVA
            );

            if (!asignacionesActivas.isEmpty()) {
                Empleado empleado = empleadoRepository.findById(asignacionDTO.getIdEmpleado())
                        .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado"));

                throw new BadRequestException("El empleado ya esta asignado a una campaña activa");
            }
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

        if (dto.getNombreCampana() != null) {
            campanaExistente.setNombreCampana(dto.getNombreCampana());
        }

        if (dto.getDescripcion() != null) {
            campanaExistente.setDescripcion(dto.getDescripcion());
        }

        if (dto.getFechaFin() != null) {
            campanaExistente.setFechaFin(dto.getFechaFin());
        }

        if (dto.getFechaInicio() != null) {
            campanaExistente.setFechaInicio(dto.getFechaInicio());
        }

        if (dto.getIdEmpresa() != null) {
            Empresa nuevaEmpresa = empresaRepository.findById(dto.getIdEmpresa())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
            campanaExistente.setEmpresa(nuevaEmpresa);
        }


        Campana campanaActualizada = campanaRepository.save(campanaExistente);

        if (asignacionesDTO != null && !asignacionesDTO.isEmpty()) {
            asignacionCampanaRepository.deleteByCampanaIdCampana(idCampana);

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
        List<AsignacionCampana> asignaciones = asignacionCampanaRepository.findByCampanaId(idCampana);
        asignaciones.forEach(asignacion -> {
            asignacion.setEstado(AsignacionCampanaEstado.LIBERADA);
            asignacionCampanaRepository.save(asignacion);
        });
    }

    @Transactional(readOnly = true)
    public List<AsignacionConsultaDTO> obtenerAsignaciones(Integer idCampana) {
        List<AsignacionCampana> asignaciones = asignacionCampanaRepository.findAgentesByCampanaId(idCampana);
        return asignaciones.stream()
                .map(this::convertirAsignacionCampanaADTO)
                .collect(Collectors.toList());
    }

    private AsignacionConsultaDTO convertirAsignacionCampanaADTO(AsignacionCampana asignacion) {
        AsignacionConsultaDTO dto = new AsignacionConsultaDTO();
        dto.setIdAsignacion(asignacion.getIdAsignacionCampana());
        dto.setIdEmpleado(asignacion.getEmpleado().getIdEmpleado());
        dto.setNombreEmpleado(asignacion.getEmpleado().getNombre());
        dto.setApellidoEmpleado(asignacion.getEmpleado().getApellido());
        dto.setEsLider(asignacion.getEsLider());
        dto.setEstadoAsignacion(asignacion.getEstado().name());
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

                List<AsignacionCampana> asignaciones = asignacionCampanaRepository.findByCampanaId(campana.getIdCampana());
                if (!asignaciones.isEmpty()) {
                    String asignacionesStr = asignaciones.stream()
                            .map(a -> a.getEmpleado().getNombre() + " " + a.getEmpleado().getApellido() +
                                    (a.getEsLider() ? " (Líder)" : ""))
                            .collect(Collectors.joining(", "));
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

    public byte[] generarReportePDFCampanas(FiltroCampanaDTO filtro) throws IOException {
        //Obtener las campañas con filtros
        Page<Campana> campanaPage = campanaRepository.findAll(
            crearSpecificationCampana(filtro, false),
            Pageable.unpaged()
        );
        List<Campana> campanas = campanaPage.getContent();

        //Preparar el DTO para el PDF
        ReporteCampanaPDFDTO reporte = new ReporteCampanaPDFDTO();
        reporte.setTitulo("Reporte de Campañas");
        reporte.setFechaGeneracion(LocalDate.now());
        reporte.setFiltros(filtro);

        //Mapeo de campaña a DTO para PDF
        List<CampanaPDFDTO> campanasPDF = campanas.stream()
                .map(this::mapearCampanaAPDFDTO)
                .toList();
        reporte.setCampanas(campanasPDF);

        //Calcular resumen
        reporte.setResumen(calcularResumen(campanas));

        //Generar PDF
        return  pdfGeneratorService.generarReporteCampanasPDF(reporte);
    }

    private CampanaPDFDTO mapearCampanaAPDFDTO(Campana campana) {
        CampanaPDFDTO dto = new CampanaPDFDTO();
        dto.setIdCampana(campana.getIdCampana());
        dto.setNombreCampana(campana.getNombreCampana());
        dto.setDescripcion(campana.getDescripcion());
        dto.setFechaInicio(campana.getFechaInicio());
        dto.setFechaFin(campana.getFechaFin());
        dto.setNombreEmpresa(campana.getEmpresa() != null ? campana.getEmpresa().getNombreEmpresa() : "N/A");
        dto.setEstado(campana.getEstado().toString());

        // Obtener líder
        Optional<AsignacionCampana> lider = asignacionCampanaRepository.findLiderByCampanaId(campana.getIdCampana());
        dto.setLider(lider.map(a -> a.getEmpleado().getNombre() + " " + a.getEmpleado().getApellido())
                .orElse("Sin líder"));

        // Obtener agentes
        List<AsignacionCampana> agentes = asignacionCampanaRepository.findAgentesByCampanaId(campana.getIdCampana());
        dto.setCantidadAgentes(agentes.size());
        dto.setAgentes(agentes.stream()
                .map(a -> a.getEmpleado().getNombre() + " " + a.getEmpleado().getApellido())
                .collect(Collectors.joining(", ")));

        return dto;
    }

    private ResumenReporteDTO calcularResumen(List<Campana> campanas) {
        ResumenReporteDTO resumen = new ResumenReporteDTO();
        resumen.setTotalCampanas(campanas.size());
        resumen.setActivas((int) campanas.stream().filter(c -> c.getEstado() == CampanaEstado.ACTIVA).count());
        resumen.setEnProceso((int) campanas.stream().filter(c -> c.getEstado() == CampanaEstado.EN_PROCESO).count());
        resumen.setFinalizadas((int) campanas.stream().filter(c -> c.getEstado() == CampanaEstado.FINALIZADA).count());
        resumen.setCanceladas((int) campanas.stream().filter(c -> c.getEstado() == CampanaEstado.CANCELADA).count());
        resumen.setArchivadas((int) campanas.stream().filter(c -> c.getEstado() == CampanaEstado.ARCHIVADA).count());

        return resumen;
    }

    @Transactional
    public CargaMasivaResponse procesarCargaMasiva(MultipartFile archivo) {
        CargaMasivaResponse response = new CargaMasivaResponse();

        try (Reader reader = new InputStreamReader(archivo.getInputStream());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                try {
                    procesarRegistroCampana(record, response);
                } catch (Exception e) {
                    response.agregarError(
                            (int) record.getRecordNumber(),
                            e.getMessage(),
                            String.join(",", record)
                    );
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error procesando archivo CSV: " + e.getMessage(), e);
        }

        return response;
    }

    private void procesarRegistroCampana(CSVRecord record, CargaMasivaResponse response) {
        // Validar campos obligatorios
        validarCamposObligatorios(record);

        // Mapear desde CSV
        CampanaCargaMasivaDTO dto = mapearDesdeCSV(record);

        // Validar y obtener empresa
        Empresa empresa = empresaRepository.findByNombreEmpresa(dto.getNombreEmpresa())
                .orElseThrow(() -> new BadRequestException("Empresa no encontrada: " + dto.getNombreEmpresa()));

        // Validar y obtener líder (solo por correo, sin relación con empresa)
        Empleado lider = empleadoRepository.findByCorreo(dto.getEmailLider())
                .orElseThrow(() -> new BadRequestException("Líder no encontrado: " + dto.getEmailLider()));

        // Validar rol del líder
        validarRolEmpleado(lider, "LIDER");

        // Validar disponibilidad del líder
        validarDisponibilidadEmpleado(lider);

        // Procesar agentes (solo por correo, sin relación con empresa)
        List<Empleado> agentes = procesarAgentes(dto.getEmailsAgentes());

        // Crear campaña
        CampanaDTO campanaDTO = new CampanaDTO();
        campanaDTO.setNombreCampana(dto.getNombreCampana());
        campanaDTO.setDescripcion(dto.getDescripcion());
        campanaDTO.setFechaInicio(dto.getFechaInicio());
        campanaDTO.setFechaFin(dto.getFechaFin());
        campanaDTO.setIdEmpresa(empresa.getIdEmpresa());

        // Crear asignaciones
        List<AsignacionCampanaDTO> asignaciones = new ArrayList<>();
        asignaciones.add(crearAsignacionLider(lider.getIdEmpleado()));
        asignaciones.addAll(crearAsignacionesAgentes(agentes));

        // Guardar campaña
        crearCampana(campanaDTO, asignaciones);

        response.incrementarExitoso();
    }

    private void validarCamposObligatorios(CSVRecord record) {
        if (!record.isSet("nombre_campana") || record.get("nombre_campana").isBlank()) {
            throw new BadRequestException("El nombre de la campaña es obligatorio");
        }
        if (!record.isSet("fecha_inicio") || record.get("fecha_inicio").isBlank()) {
            throw new BadRequestException("La fecha de inicio es obligatoria");
        }
        if (!record.isSet("fecha_fin") || record.get("fecha_fin").isBlank()) {
            throw new BadRequestException("La fecha de fin es obligatoria");
        }
        if (!record.isSet("empresa") || record.get("empresa").isBlank()) {
            throw new BadRequestException("La empresa es obligatoria");
        }
        if (!record.isSet("lider") || record.get("lider").isBlank()) {
            throw new BadRequestException("El líder es obligatorio");
        }
        if (!record.isSet("agentes") || record.get("agentes").isBlank()) {
            throw new BadRequestException("Los agentes son obligatorios");
        }
    }

    private CampanaCargaMasivaDTO mapearDesdeCSV(CSVRecord record) {
        CampanaCargaMasivaDTO dto = new CampanaCargaMasivaDTO();

        dto.setNombreCampana(record.get("nombre_campana"));
        dto.setDescripcion(record.get("descripcion"));
        dto.setFechaInicio(LocalDate.parse(record.get("fecha_inicio")));
        dto.setFechaFin(LocalDate.parse(record.get("fecha_fin")));
        dto.setNombreEmpresa(record.get("empresa"));
        dto.setEmailLider(record.get("lider"));
        dto.setEmailsAgentes(record.get("agentes"));

        return dto;
    }

    private List<Empleado> procesarAgentes(String emailsAgentes) {
        List<Empleado> agentes = new ArrayList<>();
        String[] emails = emailsAgentes.split(",");

        for (String email : emails) {
            String emailTrimmed = email.trim();
            Empleado agente = empleadoRepository.findByCorreo(emailTrimmed)
                    .orElseThrow(() -> new BadRequestException("Agente no encontrado: " + emailTrimmed));

            validarRolEmpleado(agente, "AGENTE");
            validarDisponibilidadEmpleado(agente);

            agentes.add(agente);
        }

        return agentes;
    }

    private void validarDisponibilidadEmpleado(Empleado empleado) {
        List<AsignacionCampana> asignacionesActivas = asignacionCampanaRepository
                .findByEmpleadoIdEmpleadoAndEstado(empleado.getIdEmpleado(), AsignacionCampanaEstado.ACTIVA);

        if (!asignacionesActivas.isEmpty()) {
            throw new BadRequestException("El empleado " + empleado.getCorreo() + " ya está asignado a una campaña activa");
        }
    }

    private void validarRolEmpleado(Empleado empleado, String rolEsperado) {
        Credencial credencial = credencialRepository.findByEmpleado(empleado)
                .orElseThrow(() -> new BadRequestException("Credencial no encontrada para el empleado"));

        if (!rolEsperado.equalsIgnoreCase(credencial.getRol().getNombreRol())) {
            throw new BadRequestException("El empleado " + empleado.getCorreo() +
                    " no tiene rol de " + rolEsperado +
                    ". Rol actual: " + credencial.getRol().getNombreRol());
        }
    }

    private void validarDisponibilidadAgente(Empleado agente) {
        List<AsignacionCampana> asignacionesActivas = asignacionCampanaRepository
                .findByEmpleadoIdEmpleadoAndEstado(agente.getIdEmpleado(), AsignacionCampanaEstado.ACTIVA);

        if (!asignacionesActivas.isEmpty()) {
            throw new BadRequestException("El agente " + agente.getCorreo() + " ya está asignado a una campaña activa");
        }
    }

    private AsignacionCampanaDTO crearAsignacionLider(Integer idEmpleado) {
        AsignacionCampanaDTO asignacion = new AsignacionCampanaDTO();
        asignacion.setIdEmpleado(idEmpleado);
        asignacion.setEsLider(true);
        return asignacion;
    }

    private List<AsignacionCampanaDTO> crearAsignacionesAgentes(List<Empleado> agentes) {
        return agentes.stream()
                .map(agente -> {
                    AsignacionCampanaDTO asignacion = new AsignacionCampanaDTO();
                    asignacion.setIdEmpleado(agente.getIdEmpleado());
                    asignacion.setEsLider(false);
                    return asignacion;
                })
                .collect(Collectors.toList());
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

        Optional<AsignacionCampana> liderAsignacion = asignacionCampanaRepository.findLiderByCampanaId(campana.getIdCampana());
        liderAsignacion.ifPresent(asignacion -> {
            dto.setIdLider(asignacion.getEmpleado().getIdEmpleado());
            dto.setNombreLider(asignacion.getEmpleado().getNombre() + " " + asignacion.getEmpleado().getApellido());
        });

        List<AsignacionCampana> agentesAsignaciones = asignacionCampanaRepository.findAgentesByCampanaId(campana.getIdCampana());
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
