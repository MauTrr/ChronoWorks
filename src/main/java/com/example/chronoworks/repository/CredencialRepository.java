package com.example.chronoworks.repository;

import com.example.chronoworks.model.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredencialRepository extends JpaRepository<Credencial, Integer> {

    Optional<Credencial> findByUsuario(String usuario);
}
