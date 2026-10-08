package com.app.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.app.listener.objects.AlumnoEvent;
import com.app.service.CursoService;

@Component
public class CursoListener {

	private final CursoService cursoService;

	public CursoListener(CursoService cursoService) {
		this.cursoService = cursoService;
	}

	@RabbitListener(queues = "alumno.alta.queue")
	public void recibirAltaAlumno(AlumnoEvent evento) {
		cursoService.registrarALumno(evento.getIdCurso(), evento.getIdAlumno());
		System.out.println("Evento de alta de alumno recibido: idAlumno=" + evento.getIdAlumno()
				+ ", idCurso=" + evento.getIdCurso());
	}

}