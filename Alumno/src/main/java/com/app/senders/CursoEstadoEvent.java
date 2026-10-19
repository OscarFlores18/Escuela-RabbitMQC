package com.app.senders;

import lombok.Data;

/**
 * Evento de estado de curso publicado por el modulo Cursos y
 * consumido por el modulo Alumno.
 */
@Data
public class CursoEstadoEvent {

    private long idCurso;
    private int inscriptos;
    private Integer cupoMaximo;
    private boolean cupoLleno;

}
