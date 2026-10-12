package com.app.dto;

import com.app.model.Alumno;

// DTO de salida: expone solo los datos del alumno, sin entidades JPA internas.
public record AlumnoSalidaDTO(
    Long id,
    String nombre,
    String apellido,
    String dni,
    String email
) {

    public static AlumnoSalidaDTO from(Alumno alumno) {
        return new AlumnoSalidaDTO(
                alumno.getId(),
                alumno.getNombre(),
                alumno.getApellido(),
                alumno.getDni(),
                alumno.getEmail()
        );
    }
}
