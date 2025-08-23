package com.example.chronoworks.repository;

import com.example.chronoworks.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    @Query("SELECT r FROM Rol r WHERE r.nombreRol = :nombreRol")
    Optional<Rol> findByNombreRol(String nombreRol);
}
