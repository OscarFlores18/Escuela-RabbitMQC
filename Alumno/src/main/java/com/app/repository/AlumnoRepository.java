package com.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.model.Alumno;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    List<Alumno> findByActivoTrue();

    Optional<Alumno> findByIdAndActivoTrue(Long id);
}
