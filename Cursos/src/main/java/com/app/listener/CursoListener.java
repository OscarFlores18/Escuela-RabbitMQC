package com.app.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.app.config.RabbitMQConsumerConfig;
import com.app.listener.objects.AlumnoEvent;
import com.app.service.CursoService;

@Component
public class CursoListener {

    private final CursoService cursoService;

    public CursoListener(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    // 1) Cola de alta: inscribe al alumno en el curso indicado
    @RabbitListener(queues = RabbitMQConsumerConfig.Q_ALTA)
    public void recibirAltaAlumno(AlumnoEvent evento) {
        cursoService.registrarALumno(evento.getIdCurso(), evento.getIdAlumno());
        System.out.println("[ALTA] Evento recibido: idAlumno=" + evento.getIdAlumno()
                + ", idCurso=" + evento.getIdCurso());
    }

    // 2) Cola de modificacion: sincroniza/asegura la inscripcion del alumno
    @RabbitListener(queues = RabbitMQConsumerConfig.Q_MODIFICACION)
    public void recibirModificacionAlumno(AlumnoEvent evento) {
        cursoService.sincronizarAlumno(evento.getIdAlumno(), evento.getIdCurso());
        System.out.println("[MODIFICACION] Evento recibido: idAlumno=" + evento.getIdAlumno()
                + ", idCurso=" + evento.getIdCurso());
    }

    // 3) Cola de baja: quita al alumno de su curso
    @RabbitListener(queues = RabbitMQConsumerConfig.Q_BAJA)
    public void recibirBajaAlumno(AlumnoEvent evento) {
        cursoService.quitarAlumnoDeCurso(evento.getIdAlumno());
        System.out.println("[BAJA] Evento recibido: idAlumno=" + evento.getIdAlumno());
    }

    // 4) Cola de auditoria: registra todos los eventos de alumno (patron alumno.#)
    @RabbitListener(queues = RabbitMQConsumerConfig.Q_AUDITORIA)
    public void auditarEventoAlumno(AlumnoEvent evento) {
        System.out.println("[AUDITORIA] Evento de alumno: idAlumno=" + evento.getIdAlumno()
                + ", idCurso=" + evento.getIdCurso());
    }

}
