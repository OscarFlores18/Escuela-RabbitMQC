package com.app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.app.dto.NotaModificarDTO;
import com.app.model.Nota;
import com.app.repository.NotaRepository;

@Service
public class NotaServiceImpl implements NotaService {

    private final NotaRepository notaRepository;

    public NotaServiceImpl(NotaRepository notaRepository) {
        this.notaRepository = notaRepository;
    }

    @Override
    public Nota altaNota(Nota nota) {
        nota.setId(null); // el alta nunca acepta un id externo — lo genera la base
        return notaRepository.save(nota);
    }

    @Override
    public List<Nota> listarNotas() {
        return notaRepository.findAll();
    }

    @Override
    public Optional<Nota> obtenerPorId(Long id) {
        return notaRepository.findById(id);
    }

    @Override
    public List<Nota> listarPorAlumno(Long alumnoId) {
        return notaRepository.findByAlumnoId(alumnoId);
    }

    @Override
    public List<Nota> listarPorCurso(Long cursoId) {
        return notaRepository.findByCursoId(cursoId);
    }

    @Override
    public Optional<Nota> modificarNota(Long id, NotaModificarDTO dto) {
        return notaRepository.findById(id).map(nota -> {
            nota.setAlumnoId(dto.alumnoId());
            nota.setCursoId(dto.cursoId());
            nota.setMateria(dto.materia());
            nota.setNota(dto.nota());
            nota.setFecha(dto.fecha());
            return notaRepository.save(nota);
        });
    }

    @Override
    public boolean eliminarNota(Long id) {
        if (!notaRepository.existsById(id)) {
            return false;
        }

        notaRepository.deleteById(id);
        return true;
    }
}
