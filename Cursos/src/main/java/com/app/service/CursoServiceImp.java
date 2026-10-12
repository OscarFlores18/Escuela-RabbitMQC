package com.app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.client.DocenteClient;
import com.app.dto.CursoConDocenteDTO;
import com.app.dto.CursoModificarDTO;
import com.app.dto.DocenteDTO;
import com.app.exception.CupoExcedidoException;
import com.app.exception.RecursoNoEncontradoException;
import com.app.model.Alumno;
import com.app.model.Curso;
import com.app.repository.AlumnoRepository;
import com.app.repository.CursoRepository;

@Service
public class CursoServiceImp implements CursoService {

    private final CursoRepository cursoRepository;
    private final AlumnoRepository alumnoRepository;
    
    public CursoServiceImp(CursoRepository cursoRepository, AlumnoRepository alumnoRepository) {
        this.cursoRepository = cursoRepository;
        this.alumnoRepository = alumnoRepository;
    }

    @Override
    public Curso altaCursos(Curso curso) {
    	
        return cursoRepository.save(curso);
    }

    
    
    
    @Override
    public List<Curso> listarCursos() {
        return cursoRepository.findAll();
    }

    @Override
    public Optional<Curso> obtenerPorId(Long id) {
        return cursoRepository.findById(id);
    }

    
   

	@Override
	@Transactional
	public void registrarALumno(long idCurso, long idAlumno) {
		Curso curso = this.cursoRepository.findById(idCurso)
				.orElseThrow(() -> new RecursoNoEncontradoException(
						"Curso no encontrado con id: " + idCurso));

		// Control de cupo: no se pueden inscribir mas alumnos que el cupo maximo
		if (!curso.hayCupoDisponible()) {
			throw new CupoExcedidoException(
					"El curso alcanzo su cupo maximo de " + curso.getCupo_maximo() + " alumnos");
		}

		Alumno alumno = new Alumno();
		alumno.setId(idAlumno);
		curso.addAlumno(alumno);
		this.cursoRepository.save(curso);
	}

	@Override
	@Transactional
	public void sacarALumno(long idCurso, long idAlumno) {
		Curso curso = this.cursoRepository.findById(idCurso)
				.orElseThrow(() -> new RecursoNoEncontradoException(
						"Curso no encontrado con id: " + idCurso));
		Alumno alumno = this.alumnoRepository.findById(idAlumno)
				.orElseThrow(() -> new RecursoNoEncontradoException(
						"Alumno no encontrado con id: " + idAlumno));
		curso.removeAlumno(alumno);
		this.cursoRepository.save(curso);
	}
	
	@Override
	public Optional<Curso> modificarCurso(Long id, CursoModificarDTO dto) {
	    return cursoRepository.findById(id).map(curso -> {
	        curso.setCiclo_lectivo(dto.ciclo_lectivo());
	        curso.setDivision(dto.division());
	        curso.setGrado(dto.grado());
	        curso.setTurno(dto.turno());
	        curso.setMateria(dto.materia());
	        curso.setHorario(dto.horario());
	        curso.setCupo_maximo(dto.cupo_maximo());
	        curso.setDocenteId(dto.docenteId());

	        return cursoRepository.save(curso);
	    });
	}

	@Override
	@Transactional
	public boolean eliminarCurso(Long id) {
	    Optional<Curso> resultado = cursoRepository.findById(id);

	    if (resultado.isEmpty()) {
	        return false;
	    }

	    Curso curso = resultado.get();

	    // No eliminar el curso si todavía tiene alumnos asociados.
	    if (curso.getAlumnos() != null && !curso.getAlumnos().isEmpty()) {
	        return false;
	    }

	    cursoRepository.delete(curso);
	    return true;
	}
	
	
	
	
	


}