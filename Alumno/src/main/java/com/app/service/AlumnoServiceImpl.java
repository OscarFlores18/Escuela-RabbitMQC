package com.app.service;

import com.app.config.RabbitMQConfig;
import com.app.dto.AlumnoConCursoDTO;
import com.app.dto.AlumnoModificarDTO;
import com.app.exception.RecursoNoEncontradoException;
import com.app.model.Alumno;
import com.app.repository.AlumnoRepository;
import com.app.senders.AlumnoEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlumnoServiceImpl implements AlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final RabbitTemplate rabbitTemplate;

    public AlumnoServiceImpl(AlumnoRepository alumnoRepository, RabbitTemplate rabbitTemplate) {
        this.alumnoRepository = alumnoRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Alumno altaAlumno(Alumno alumno, Long cursoId) {
        alumno.setId(null); // el alta nunca acepta un id externo — lo genera la base
        alumno.setActivo(true);
        Alumno nuevo = alumnoRepository.save(alumno);

        if (cursoId != null) {
            AlumnoEvent event = new AlumnoEvent();
            event.setIdAlumno(nuevo.getId());
            event.setIdCurso(cursoId);

            // 1) Cola de alta: inscribe al alumno en el curso
            publicar(RabbitMQConfig.RK_ALTA, event);
        }

        return nuevo;
    }

    private void publicar(String routingKey, AlumnoEvent event) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE,
            routingKey,
            event
        );
    }

    @Override
    public List<Alumno> listarAlumnos() {
        return alumnoRepository.findByActivoTrue();
    }

    @Override
    public AlumnoConCursoDTO obtenerConCurso(Long alumnoId) {
        Alumno alumno = alumnoRepository.findByIdAndActivoTrue(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Alumno no encontrado con id: " + alumnoId));

        AlumnoConCursoDTO dto = new AlumnoConCursoDTO();
        dto.setId(alumno.getId());
        dto.setNombre(alumno.getNombre());
        dto.setApellido(alumno.getApellido());
        dto.setDni(alumno.getDni());
        dto.setEmail(alumno.getEmail());

        return dto;
    }


    @Override
    public Optional<Alumno> modificarAlumno(
            Long id,
            AlumnoModificarDTO dto) {

        return alumnoRepository.findByIdAndActivoTrue(id)
            .map(alumno -> {
                alumno.setNombre(dto.nombre());
                alumno.setApellido(dto.apellido());
                alumno.setDni(dto.dni());
                alumno.setEmail(dto.email());

                Alumno guardado = alumnoRepository.save(alumno);

                // 2) Cola de modificacion: sincroniza los datos del alumno en los
                // demas modulos. Si viene un curso, tambien asegura la inscripcion.
                AlumnoEvent event = new AlumnoEvent();
                event.setIdAlumno(guardado.getId());
                event.setIdCurso(dto.cursoId() != null ? dto.cursoId() : 0L);
                publicar(RabbitMQConfig.RK_MODIFICACION, event);

                return guardado;
            });
    }

    @Override
    public boolean eliminarAlumno(Long id) {
        // Baja logica: marca inactivo en lugar de borrar la fila
        return alumnoRepository.findById(id)
            .filter(Alumno::isActivo)
            .map(alumno -> {
                alumno.setActivo(false);
                alumnoRepository.save(alumno);

                // 3) Cola de baja: quita al alumno de su curso en el modulo Cursos
                AlumnoEvent event = new AlumnoEvent();
                event.setIdAlumno(alumno.getId());
                publicar(RabbitMQConfig.RK_BAJA, event);

                return true;
            })
            .orElse(false);
    }

}
