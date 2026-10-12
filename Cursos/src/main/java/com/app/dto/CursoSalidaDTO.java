package com.app.dto;

import java.util.List;

import com.app.model.Alumno;
import com.app.model.Curso;

// DTO de salida: expone solo los datos del curso, sin entidades JPA internas.
public record CursoSalidaDTO(
    Long id,
    String ciclo_lectivo,
    String division,
    String grado,
    String turno,
    String materia,
    String horario,
    Integer cupo_maximo,
    Integer cantidadAlumnos,
    Long docenteId,
    List<Long> alumnoIds
) {

    public static CursoSalidaDTO from(Curso curso) {
        return new CursoSalidaDTO(
                curso.getId(),
                curso.getCiclo_lectivo(),
                curso.getDivision(),
                curso.getGrado(),
                curso.getTurno(),
                curso.getMateria(),
                curso.getHorario(),
                curso.getCupo_maximo(),
                curso.getCantidadAlumnos(),
                curso.getDocenteId(),
                curso.getAlumnos() == null
                        ? List.of()
                        : curso.getAlumnos().stream().map(Alumno::getId).toList()
        );
    }
}
