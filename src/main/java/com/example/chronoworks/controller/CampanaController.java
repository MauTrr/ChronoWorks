package com.example.chronoworks.controller;

import com.example.chronoworks.dto.asignacion.AsignacionConsultaDTO;
import com.example.chronoworks.dto.campana.*;
import com.example.chronoworks.dto.empleado.EmpleadoDisponibleDTO;
import com.example.chronoworks.dto.empleado.RespuestaEmpleadoDTO;
import com.example.chronoworks.dto.tarea.TareaDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.AsignacionCampana;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.enums.AsignacionCampanaEstado;
import com.example.chronoworks.model.enums.CampanaEstado;
import com.example.chronoworks.repository.AsignacionCampanaRepository;
import com.example.chronoworks.repository.EmpleadoRepository;
import com.example.chronoworks.service.CampanaService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/campanas")
public class CampanaController {
    private final CampanaService campanaService;
    private final EmpleadoRepository empleadoRepository;
    private final AsignacionCampanaRepository asignacionCampanaRepository;

    public CampanaController(CampanaService campanaService,
                             EmpleadoRepository empleadoRepository,
                             AsignacionCampanaRepository asignacionCampanaRepository) {
        this.campanaService = campanaService;
        this.empleadoRepository = empleadoRepository;
        this.asignacionCampanaRepository = asignacionCampanaRepository;
    }

    @PostMapping
    public ResponseEntity<?> crearCampana(@Valid @RequestBody CrearCampanaCompletaDTO request) {
        try {
            System.out.println("=== CREAR CAMPAÑA ===");
            System.out.println("Request recibido:");
            System.out.println("- Nombre campaña: " + request.getCampana().getNombreCampana());
            System.out.println("- ID Empresa: " + request.getCampana().getIdEmpresa());
            System.out.println("- Número de asignaciones: " +
                    (request.getAsignaciones() != null ? request.getAsignaciones().size() : 0));

            if (request.getAsignaciones() != null) {
                request.getAsignaciones().forEach(a -> {
                    System.out.println("  - Empleado ID: " + a.getIdEmpleado() + ", Es líder: " + a.getEsLider());
                });
            }

            RespuestaCampanaDTO nuevaCampana = campanaService.crearCampana(
                    request.getCampana(),
                    request.getAsignaciones());

            System.out.println("Campaña creada exitosamente: ID " + nuevaCampana.getIdCampana());
            return new ResponseEntity<>(nuevaCampana, HttpStatus.CREATED);

        } catch (BadRequestException e) {
            System.err.println("BadRequestException: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Solicitud inválida");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (ResourceNotFoundException e) {
            System.err.println("ResourceNotFoundException: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Recurso no encontrado");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (IllegalStateException e) {
            System.err.println("IllegalStateException: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Estado inválido");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);

        } catch (Exception e) {
            System.err.println("ERROR INESPERADO AL CREAR CAMPAÑA:");
            e.printStackTrace();

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @GetMapping("/{idCampana}")
    public ResponseEntity<RespuestaCampanaDTO> obtenerCampana(@PathVariable Integer idCampana) {
        RespuestaCampanaDTO campana = campanaService.obtenerCampana(idCampana);
        return ResponseEntity.ok(campana);
    }

    @GetMapping
    public ResponseEntity<Page<RespuestaCampanaDTO>> listarCampanas(
            @ModelAttribute FiltroCampanaDTO filtro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombreCampana") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        return ResponseEntity.ok(campanaService.listarCampanas(filtro, pageable));
    }


    @GetMapping("/todas")
    public ResponseEntity<List<RespuestaCampanaDTO>> listarTodasCampanas() {
        Page<RespuestaCampanaDTO> page = campanaService.listarCampanas(new FiltroCampanaDTO(), Pageable.unpaged());
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/activas")
    public ResponseEntity<Page<RespuestaCampanaDTO>> listarCampanasActivas(
            @ModelAttribute FiltroCampanaDTO filtro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(campanaService.listarCampanasActivas(filtro, pageable));
    }

    // Actualizar campaña (PUT)
    @PutMapping("/{idCampana}")
    public ResponseEntity<RespuestaCampanaDTO> actualizarCampana(
            @PathVariable Integer idCampana,
            @Valid @RequestBody ActualizarCampanaCompletaDTO request) {

        RespuestaCampanaDTO campanaActualizada = campanaService.actualizarCampana(
                idCampana,
                request.getCampana(),
                request.getAsignaciones());

        return ResponseEntity.ok(campanaActualizada);
    }

    @DeleteMapping("/{idCampana}")
    public ResponseEntity<Void> eliminarCampana(@PathVariable Integer idCampana) {
        campanaService.cambiarEstado(
                idCampana,
                CampanaEstado.CANCELADA,
                true);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{idCampana}/estado")
    public ResponseEntity<RespuestaCampanaDTO> cambiarEstado (
            @PathVariable Integer idCampana,
            @RequestBody CambiarEstadoDTO request) {

        RespuestaCampanaDTO respuesta = campanaService.cambiarEstado(
                idCampana,
                request.getEstado(),
                request.isLiberarEmpleados()
        );
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("{idCampana}/asignaciones")
    public ResponseEntity<List<AsignacionConsultaDTO>> getAsignaciondes(
            @PathVariable Integer idCampana) {
        return ResponseEntity.ok(campanaService.obtenerAsignaciones(idCampana));
    }

    @GetMapping("/empleados/disponibles")
    public ResponseEntity<List<EmpleadoDisponibleDTO>> getEmpleadosDisponibles(
            @RequestParam String rol,
            @RequestParam(required = false) Integer idEmpresa
    ) {
        try {
            // 1. Obtener todos los empleados activos con el rol especificado
            List<Empleado> empleados = empleadoRepository.findByNombreRolIgnoreCase(rol);

            empleados = empleados.stream()
                    .filter(Empleado::isActivo)
                    .toList();

            // 2. Si se especifica empresa, filtrar empleados que NO estén asignados
            //    a campaña ACTIVA de esa empresa
            if (idEmpresa != null) {
                empleados = empleados.stream()
                        .filter(empleado -> {
                            // Obtener todas las asignaciones activas del empleado
                            List<AsignacionCampana> asignacionesActivas = asignacionCampanaRepository
                                    .findByEmpleadoIdEmpleadoAndEstado(
                                            empleado.getIdEmpleado(),
                                            AsignacionCampanaEstado.ACTIVA
                                    );

                            // Si no tiene asignaciones activas, está disponible
                            if (asignacionesActivas.isEmpty()) {
                                return true;
                            }

                            // Si tiene asignaciones activas, verificar que NINGUNA sea
                            // de la empresa especificada
                            return asignacionesActivas.stream()
                                    .noneMatch(asignacion ->
                                            asignacion.getCampana() != null &&
                                                    asignacion.getCampana().getEmpresa() != null &&
                                                    asignacion.getCampana().getEmpresa().getIdEmpresa()
                                                            .equals(idEmpresa)
                                    );
                        })
                        .toList();
            } else {
                // Sin empresa especificada, devolver empleados sin asignaciones activas
                empleados = empleados.stream()
                        .filter(empleado -> {
                            List<AsignacionCampana> asignacionesActivas = asignacionCampanaRepository
                                    .findByEmpleadoIdEmpleadoAndEstado(
                                            empleado.getIdEmpleado(),
                                            AsignacionCampanaEstado.ACTIVA
                                    );
                            return asignacionesActivas.isEmpty();
                        })
                        .toList();
            }

            // 3. Mapear a DTO
            List<EmpleadoDisponibleDTO> respuesta = empleados.stream()
                    .map(empleado -> {
                        EmpleadoDisponibleDTO dto = new EmpleadoDisponibleDTO();
                        dto.setIdEmpleado(empleado.getIdEmpleado());
                        dto.setNombre(empleado.getNombre());
                        dto.setApellido(empleado.getApellido());
                        dto.setCorreo(empleado.getCorreo());
                        return dto;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    @GetMapping("/reporte-pdf")
    public ResponseEntity<byte[]> descargarReporteCampanasPDF(@ModelAttribute FiltroCampanaDTO filtro) {
        try {
            byte[] pdfContent = campanaService.generarReportePDFCampanas(filtro);

            String nombreArchivo = "reporte_campanas_" + LocalDate.now() + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfContent.length)
                    .body(pdfContent);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/carga-masiva", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CargaMasivaResponse> cargarCampanasMasivo(
            @RequestParam("archivo") MultipartFile archivo) {

        try {
            // Validar tipo de archivo
            if (archivo.isEmpty()) {
                return ResponseEntity.badRequest().body(crearResponseError("El archivo está vacío"));
            }

            if (!Objects.equals(archivo.getContentType(), "text/csv") &&
                    !archivo.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().body(crearResponseError("Solo se permiten archivos CSV"));
            }

            // Procesar carga masiva
            CargaMasivaResponse response = campanaService.procesarCargaMasiva(archivo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            CargaMasivaResponse errorResponse = new CargaMasivaResponse();
            errorResponse.agregarError(0, "Error general: " + e.getMessage(), "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private CargaMasivaResponse crearResponseError(String mensaje) {
        CargaMasivaResponse response = new CargaMasivaResponse();
        response.agregarError(0, mensaje, "");
        return response;
    }

    @GetMapping("/reporte-excel")
    public ResponseEntity<ByteArrayResource> descargarReporteCampanasExcel(@ModelAttribute FiltroCampanaDTO filtro) throws IOException {

        byte[] excelContent= campanaService.generarReporteExcelCampanas(filtro);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=reporte_campanas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelContent.length)
                .body(new ByteArrayResource(excelContent));
    }

    @GetMapping("/lider/{idEmpleado}")
    public ResponseEntity<RespuestaCampanaDTO> getCampanaPorLider(@PathVariable Integer idEmpleado) {
        try {
            RespuestaCampanaDTO campana = campanaService.obtenerCampanaPorLider(idEmpleado);
            return ResponseEntity.ok(campana);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{idCampana}/tareas-lider")
    public ResponseEntity<List<TareaDTO>> getTareasPorCampanaLider(@PathVariable Integer idCampana) {
        // Devuelve todas las tareas de la campaña que el líder puede gestionar
        List<TareaDTO> tareas = campanaService.obtenerTareasPorCampana(idCampana);
        return ResponseEntity.ok(tareas);
    }

    @GetMapping("/agente/{idEmpleado}")
    public ResponseEntity<RespuestaCampanaDTO> getCampanaPorAgente(@PathVariable Integer idEmpleado) {
        try {
            RespuestaCampanaDTO campana = campanaService.obtenerCampanaPorAgente(idEmpleado);
            return ResponseEntity.ok(campana);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
