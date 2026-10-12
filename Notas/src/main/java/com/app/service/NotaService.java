package com.app.service;

import java.util.List;
import java.util.Optional;

import com.app.dto.NotaModificarDTO;
import com.app.model.Nota;

public interface NotaService {

    Nota altaNota(Nota nota);

    List<Nota> listarNotas();

    Optional<Nota> obtenerPorId(Long id);

    List<Nota> listarPorAlumno(Long alumnoId);

    List<Nota> listarPorCurso(Long cursoId);

    Optional<Nota> modificarNota(Long id, NotaModificarDTO dto);

    boolean eliminarNota(Long id);
}
