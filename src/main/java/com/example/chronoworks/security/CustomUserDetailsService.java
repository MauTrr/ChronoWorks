package com.example.chronoworks.security;

import com.example.chronoworks.model.Credencial;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.model.Rol;
import com.example.chronoworks.repository.CredencialRepository;
import com.example.chronoworks.service.EmpleadoService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CredencialRepository credencialRepository;
    private final EmpleadoService empleadoService;

    public CustomUserDetailsService(CredencialRepository credencialRepository, EmpleadoService empleadoService) {
        this.credencialRepository = credencialRepository;
        this.empleadoService = empleadoService;
    }

    @Override
    public UserDetails loadUserByUsername(String usuario) throws UsernameNotFoundException {
        Credencial credencial = credencialRepository.findByUsuario(usuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Rol rol = credencial.getRol();
        String authorityString = "ROLE_" + rol.getNombreRol().toUpperCase();

        GrantedAuthority authority = new SimpleGrantedAuthority(authorityString);

        Empleado empleado = empleadoService.findByUsuario(usuario)
                .orElseThrow(() -> new UsernameNotFoundException("Empleado no encontrado"));

        return new org.springframework.security.core.userdetails.User(
                credencial.getUsuario(),
                credencial.getContrasena(),
                empleado.isActivo(),
                true,
                true,
                true,
                Collections.singletonList(authority)
        );
    }


    public Credencial getCredencialByUsuario(String usuario) {
        return credencialRepository.findByUsuario(usuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
