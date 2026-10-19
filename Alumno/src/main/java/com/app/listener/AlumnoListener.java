package com.app.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.app.config.RabbitMQConfig;
import com.app.senders.CursoEstadoEvent;

/**
 * Consumidor de eventos provenientes del modulo Cursos.
 */
@Component
public class AlumnoListener {

    @RabbitListener(queues = RabbitMQConfig.Q_CURSO_ESTADO)
    public void recibirEstadoCurso(CursoEstadoEvent evento) {
        System.out.println("Estado de curso recibido: idCurso=" + evento.getIdCurso()
                + ", inscriptos=" + evento.getInscriptos()
                + ", cupoMaximo=" + evento.getCupoMaximo()
                + ", cupoLleno=" + evento.isCupoLleno());
    }

}
