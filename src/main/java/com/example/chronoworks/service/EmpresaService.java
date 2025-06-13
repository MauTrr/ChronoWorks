package com.example.chronoworks.service;

import com.example.chronoworks.dto.empresa.EmpresaDTO;
import com.example.chronoworks.dto.empresa.RespuestaEmpresaDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Empresa;
import com.example.chronoworks.repository.EmpresaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {
    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public RespuestaEmpresaDTO registrarEmpresa(EmpresaDTO dto) {
        if (empresaRepository.findByNombreEmpresa(dto.getNombreEmpresa()).isPresent()) {
            throw new BadRequestException("La empresa ya esta registrada");
        }

        Empresa nuevaEmpresa = new Empresa();
        nuevaEmpresa.setNombreEmpresa(dto.getNombreEmpresa());
        nuevaEmpresa.setDireccion(dto.getDireccion());
        nuevaEmpresa.setTelefono(dto.getTelefono());
        nuevaEmpresa.setSector(dto.getSector());
        nuevaEmpresa.setEncargado(dto.getEncargado());

        Empresa empresaGuardada = empresaRepository.save(nuevaEmpresa);
        return mapToRespuestaEmpresaDTO(empresaGuardada);
    }

    @Transactional(readOnly = true)
    public RespuestaEmpresaDTO obtenerEmpresa(Integer idEmpresa) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException ("Empresa no encontrada."));
        return mapToRespuestaEmpresaDTO(empresa);
    }

    @Transactional(readOnly = true)
    public Page<RespuestaEmpresaDTO> listarEmpresas(Pageable pageable) {
        return empresaRepository.findByActivoTrue(pageable).map(this::mapToRespuestaEmpresaDTO);
    }

    @Transactional
    public RespuestaEmpresaDTO actualizarEmpresa(Integer idEmpresa, EmpresaDTO dto) {
        Empresa empresaExistente = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada."));

        if (dto.getNombreEmpresa() != null && !dto.getNombreEmpresa().equals(empresaExistente.getNombreEmpresa())){
            if (empresaRepository.findByNombreEmpresa(dto.getNombreEmpresa()).isPresent()) {
                throw new BadRequestException("El nuevo nombre de empresa ya esta en uso");
            }
            empresaExistente.setNombreEmpresa(dto.getNombreEmpresa());
        }
        if (dto.getDireccion()!= null) empresaExistente.setDireccion(dto.getDireccion());
        if (dto.getTelefono()!= null) empresaExistente.setTelefono(dto.getTelefono());
        if (dto.getSector()!= null) empresaExistente.setSector(dto.getSector());
        if (dto.getEncargado()!= null) empresaExistente.setEncargado(dto.getEncargado());

        Empresa empresaActualizada = empresaRepository.save(empresaExistente);
        return mapToRespuestaEmpresaDTO(empresaActualizada);
    }

    @Transactional
    public void desactivarEmpresa(Integer idEmpresa) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                        .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
        empresa.setActivo(false);
        empresaRepository.save(empresa);
    }

    private  RespuestaEmpresaDTO mapToRespuestaEmpresaDTO(Empresa empresa) {
        return RespuestaEmpresaDTO.builder()
                .idEmpresa(empresa.getIdEmpresa())
                .nombreEmpresa(empresa.getNombreEmpresa())
                .direccion(empresa.getDireccion())
                .telefono(empresa.getTelefono())
                .sector(empresa.getSector())
                .encargado(empresa.getEncargado())
                .build();
    }
}
