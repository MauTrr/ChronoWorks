package com.example.chronoworks.repository;

import com.example.chronoworks.model.Credencial;
import com.example.chronoworks.model.Empleado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer>, JpaSpecificationExecutor<Empleado> {

    Optional<Empleado> findByCorreo(String correo);

    Optional<Empleado> findByCredencialUsuario(String usuario);

    @Query("SELECT e FROM Empleado e JOIN e.credencial c JOIN c.rol r WHERE r.nombreRol = :nombreRol")
    List<Empleado> findByNombreRol(@Param("nombreRol") String nombreRol);
}
