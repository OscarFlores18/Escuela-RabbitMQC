package com.app.dto;

import java.time.LocalDate;

import com.app.model.Nota;

// DTO de salida: expone solo los datos de la nota, sin entidades JPA internas.
public record NotaSalidaDTO(
    Long id,
    Long alumnoId,
    Long cursoId,
    String materia,
    Double nota,
    LocalDate fecha
) {

    public static NotaSalidaDTO from(Nota nota) {
        return new NotaSalidaDTO(
                nota.getId(),
                nota.getAlumnoId(),
                nota.getCursoId(),
                nota.getMateria(),
                nota.getNota(),
                nota.getFecha()
        );
    }
}
