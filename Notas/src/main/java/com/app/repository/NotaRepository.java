package com.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.model.Nota;

public interface NotaRepository extends JpaRepository<Nota, Long> {

    List<Nota> findByAlumnoId(Long alumnoId);

    List<Nota> findByCursoId(Long cursoId);
}
