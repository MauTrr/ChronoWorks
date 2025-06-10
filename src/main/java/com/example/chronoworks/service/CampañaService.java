package com.example.chronoworks.service;

import com.example.chronoworks.dto.campaña.CampañaDTO;
import com.example.chronoworks.dto.campaña.RespuestaCampañaDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Campaña;
import com.example.chronoworks.model.Empresa;
import com.example.chronoworks.model.enums.CampañaEstado;
import com.example.chronoworks.repository.CampañaRepository;
import com.example.chronoworks.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampañaService {

    private final CampañaRepository campañaRepository;
    private final EmpresaRepository empresaRepository;

    public CampañaService(CampañaRepository campañaRepository,
                          EmpresaRepository empresaRepository) {
        this.campañaRepository = campañaRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public RespuestaCampañaDTO crearCampaña(RespuestaCampañaDTO dto) {
        if(campañaRepository.findByNombreCampaña(dto.getNombreCampaña()).isPresent()) {
            throw new BadRequestException("El nombre de la campaña ya esta en uso");
        }

        Empresa empresa = empresaRepository.findById(dto.getIdEmpresa())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa con ID " + dto.getIdEmpresa() + " no encontrado."));

        Campaña nuevaCampaña = new Campaña();
        nuevaCampaña.setNombreCampaña(dto.getNombreCampaña());
        nuevaCampaña.setDescripcion(dto.getDescripcion());
        nuevaCampaña.setFechaInicio(dto.getFechaInicio());
        nuevaCampaña.setFechafin(dto.getFechaFin());
        nuevaCampaña.setEmpresa(empresa);
        nuevaCampaña.setEstado(dto.getEstado());

        Campaña campañaGuardada = campañaRepository.save(nuevaCampaña);

        return mapToRespuestaCampañaDTO(campañaGuardada);
    }

    @Transactional(readOnly = true)
    public RespuestaCampañaDTO obtenerCampaña(Integer idCampaña) {
        Campaña campaña = campañaRepository.findById(idCampaña)
                .orElseThrow(() -> new ResourceNotFoundException( "campaña con ID " + idCampaña + " no encontrada"));
        return mapToRespuestaCampañaDTO(campaña);
    }

    @Transactional(readOnly = true)
    public List<RespuestaCampañaDTO> listarCampañas() {
        return campañaRepository.findAll().stream().map(this::mapToRespuestaCampañaDTO).collect(Collectors.toList());
    }

    @Transactional
    public RespuestaCampañaDTO actualizarCampaña(Integer idCampaña, CampañaDTO dto, CampañaEstado nuevoEstado) {
        Campaña campañaExistente = campañaRepository.findById(idCampaña)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña con ID " + idCampaña + " no encontrada"));
        if(dto.getNombreCampaña() != null && dto.getNombreCampaña().equals(campañaExistente.getNombreCampaña())){
            throw new BadRequestException("El nuevo nombre de la campaña '" + dto.getNombreCampaña() + "' no encontrada");
        }
        if ((campañaExistente.getEstado() == CampañaEstado.FINALIZADA || campañaExistente.getEstado() == CampañaEstado.CANCELADA)
        && (nuevoEstado == CampañaEstado.ACTIVA || nuevoEstado == CampañaEstado.PLANEADA)) {
            throw new BadRequestException("No se puede cambiar el estado de una campaña cancelada o finalizada a activa a planificada.");
        }

        campañaExistente.setNombreCampaña(dto.getNombreCampaña());

        if(dto.getNombreCampaña() != null) campañaExistente.setNombreCampaña(dto.getNombreCampaña());
        if(dto.getDescripcion() != null) campañaExistente.setDescripcion(dto.getDescripcion());
        if(dto.getFechaInicio() != null) campañaExistente.setFechaInicio(dto.getFechaInicio());
        if(dto.getFechaFin() != null) campañaExistente.setFechafin(dto.getFechaFin());
        if(dto.getIdEmpresa() != null) {
            Empresa nuevaEmpresa = empresaRepository.findById(dto.getIdEmpresa())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa con ID " + dto.getIdEmpresa() + " no encontrada"));
        }
        campañaExistente.setEstado(nuevoEstado);
        Campaña campañaActualizada = campañaRepository.save(campañaExistente);
        return mapToRespuestaCampañaDTO(campañaActualizada);
    }

    @Transactional(readOnly = true)
    public List<RespuestaCampañaDTO> buscarCampañaporEstado(CampañaEstado estado) {
        return  campañaRepository.findByEstado(estado).stream()
                .map(this::mapToRespuestaCampañaDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<RespuestaCampañaDTO> buscarCampañaPorNombre(String nombreFiltro) {
        if (nombreFiltro == null || nombreFiltro.trim().isEmpty()) {
            return listarCampañas();
        }
        return campañaRepository.findByNombreCampañaContainingIgnoreCase(nombreFiltro).stream()
                .map(this::mapToRespuestaCampañaDTO)
                .collect(Collectors.toList());
    }

    private RespuestaCampañaDTO mapToRespuestaCampañaDTO(Campaña campaña) {
        return  RespuestaCampañaDTO.builder()
                .idCampaña(campaña.getIdCampaña())
                .nombreCampaña(campaña.getNombreCampaña())
                .descripcion(campaña.getDescripcion())
                .fechaInicio(campaña.getFechaInicio())
                .fechaFin(campaña.getFechafin())
                .idEmpresa(campaña.getEmpresa()!= null ? campaña.getEmpresa().getIdEmpresa() : null)
                .nombreEmpresa(campaña.getNombreCampaña()!= null ? campaña.getEmpresa().getNombreEmpresa():null)
                .build();
    }
}
