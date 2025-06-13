package com.example.chronoworks.service;

import com.example.chronoworks.dto.login.RequestLoginDTO;
import com.example.chronoworks.exception.BadRequestException;
import com.example.chronoworks.model.Credencial;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.repository.CredencialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private CredencialRepository credencialRepository;
    private PasswordEncoder passwordEncoder;

    public Empleado authenticate(RequestLoginDTO request) {
        Credencial credencial = credencialRepository.findByUsuario(request.getUsuario())
                .orElseThrow(() -> new BadRequestException("Usuario o contraseña incorrectos"));

        if(!passwordEncoder.matches(request.getContrasena(), credencial.getContrasena())) {
            throw new BadRequestException("Usuario o contraseña incorrectos");
        }

        Empleado empleado = credencial.getEmpleado();

        empleado.getCredencial().setContrasena(null);

        return empleado;
    }
}
