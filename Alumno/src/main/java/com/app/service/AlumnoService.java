package com.app.service;

import java.util.List;
import java.util.Optional;

import com.app.dto.AlumnoConCursoDTO;
import com.app.dto.AlumnoModificarDTO;
import com.app.model.Alumno;

public interface AlumnoService {
    Alumno altaAlumno(Alumno alumno, Long cursoId);
    List<Alumno> listarAlumnos();
    AlumnoConCursoDTO obtenerConCurso(Long alumnoId);

    Optional<Alumno> modificarAlumno(
        Long id,
        AlumnoModificarDTO dto
    );

    boolean eliminarAlumno(Long id);
}