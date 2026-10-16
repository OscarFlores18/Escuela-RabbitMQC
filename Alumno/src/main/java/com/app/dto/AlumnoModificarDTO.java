package com.app.dto;

public record AlumnoModificarDTO(
    String nombre,
    String apellido,
    String dni,
    String email,
    Long cursoId
) {}
