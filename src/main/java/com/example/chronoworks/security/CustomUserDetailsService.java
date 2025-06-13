package com.example.chronoworks.security;

import com.example.chronoworks.model.Credencial;
import com.example.chronoworks.model.Rol;
import com.example.chronoworks.repository.CredencialRepository;
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

    public CustomUserDetailsService(CredencialRepository credencialRepository) {
        this.credencialRepository = credencialRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usuario) throws UsernameNotFoundException {
        // Buscar la credencial por el nombre del usuario
        Credencial credencial = credencialRepository.findByUsuario(usuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        //Construccion de la lista de roles
        Rol rol = credencial.getRol();
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rol.getNombreRol().toUpperCase());

        //Devuelve un objeto UserDetails
        return new org.springframework.security.core.userdetails.User(
                credencial.getUsuario(),
                credencial.getContrasena(),
                Collections.singletonList(authority)
        );
    }

}
