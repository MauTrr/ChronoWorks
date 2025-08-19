package com.example.chronoworks.repository;

import com.example.chronoworks.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    @Query("SELECT r FROM Rol r JOIN r.empleados e WHERE r.nombreRol = :nombre AND e.turno.id = :idTurno")
    Optional<Rol> findByNombreRolAndEmpresa(@Param("nombre") String nombre, @Param("idEmpresa") Integer idEmpresa);
}
