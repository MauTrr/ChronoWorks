package com.example.chronoworks.service;

import com.example.chronoworks.dto.empresa.EmpresaDTO;
import com.example.chronoworks.dto.empresa.RespuestaEmpresaDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.exception.ResourceNotFoundException;
import com.example.chronoworks.model.Empresa;
import com.example.chronoworks.repository.EmpresaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
            throw new BadRequestException("La empresa ya está registrada");
        }
        Empresa empresa = new Empresa();
        empresa.setNombreEmpresa(dto.getNombreEmpresa());
        empresa.setNitEmpresa(dto.getNitEmpresa());
        empresa.setDireccion(dto.getDireccion());
        empresa.setTelefono(dto.getTelefono());
        empresa.setSector(dto.getSector());
        empresa.setLider(dto.getLider());
        empresa.setActivo(true);
        return mapToDTO(empresaRepository.save(empresa));
    }

    @Transactional(readOnly = true)
    public Page<RespuestaEmpresaDTO> listarEmpresas(Pageable pageable, String nombre, String sector, Boolean activo) {
        Specification<Empresa> spec = Specification.where(null);

        if (nombre != null) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nombreEmpresa")), "%" + nombre.toLowerCase() + "%"));
        }
        if (sector != null) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("sector")), "%" + sector.toLowerCase() + "%"));
        }
        if (activo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("activo"), activo));
        }

        return empresaRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    @Transactional
    public RespuestaEmpresaDTO actualizarEmpresa(Integer id, EmpresaDTO dto) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        empresa.setNombreEmpresa(dto.getNombreEmpresa());
        empresa.setNitEmpresa(dto.getNitEmpresa());
        empresa.setDireccion(dto.getDireccion());
        empresa.setTelefono(dto.getTelefono());
        empresa.setSector(dto.getSector());
        empresa.setLider(dto.getLider());
        return mapToDTO(empresaRepository.save(empresa));
    }

    @Transactional
    public void eliminarEmpresa(Integer id) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
        empresa.setActivo(false);
        empresaRepository.save(empresa);
    }

    @Transactional(readOnly = true)
    public RespuestaEmpresaDTO obtenerEmpresa(Integer id) {
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
        return mapToDTO(empresa);
    }

    private RespuestaEmpresaDTO mapToDTO(Empresa e) {
        return RespuestaEmpresaDTO.builder()
                .id(e.getIdEmpresa())
                .nombreEmpresa(e.getNombreEmpresa())
                .nitEmpresa(e.getNitEmpresa())
                .direccion(e.getDireccion())
                .telefono(e.getTelefono())
                .sector(e.getSector())
                .lider(e.getLider())
                .activo(e.isActivo())
                .build();
    }
}