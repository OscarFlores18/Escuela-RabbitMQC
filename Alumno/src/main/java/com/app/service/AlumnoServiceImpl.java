package com.app.service;

import com.app.config.RabbitMQConfig;
import com.app.dto.AlumnoConCursoDTO;
import com.app.dto.AlumnoModificarDTO;
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
        Alumno nuevo = alumnoRepository.save(alumno);

        if (cursoId != null) {
            AlumnoEvent event = new AlumnoEvent();
            event.setIdAlumno(nuevo.getId());
            event.setIdCurso(cursoId);

            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                event
            );
        }

        return nuevo;
    }

    @Override
    public List<Alumno> listarAlumnos() {
        return alumnoRepository.findAll();
    }

    @Override
    public AlumnoConCursoDTO obtenerConCurso(Long alumnoId) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado con id: " + alumnoId));

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

        return alumnoRepository.findById(id)
            .map(alumno -> {
                alumno.setNombre(dto.nombre());
                alumno.setApellido(dto.apellido());
                alumno.setDni(dto.dni());
                alumno.setEmail(dto.email());

                return alumnoRepository.save(alumno);
            });
    }

    @Override
    public boolean eliminarAlumno(Long id) {

        if (!alumnoRepository.existsById(id)) {
            return false;
        }

        alumnoRepository.deleteById(id);
        return true;
    }
    
}