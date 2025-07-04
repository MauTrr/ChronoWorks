package com.example.chronoworks.temp;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("123"); // Cambia "123" por la contraseña deseada
        System.out.println("Contraseña encriptada: " + encodedPassword);
    }
}
