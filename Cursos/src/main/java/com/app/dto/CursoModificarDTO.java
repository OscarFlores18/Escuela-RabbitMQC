package com.app.dto;

public record CursoModificarDTO(
    String ciclo_lectivo,
    String division,
    String grado,
    String turno,
    String materia,
    String horario,
    Integer cupo_maximo,
    Long docenteId
) {}
