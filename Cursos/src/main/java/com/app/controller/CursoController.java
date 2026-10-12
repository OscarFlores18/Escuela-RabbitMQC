package com.app.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.app.dto.CursoAltaDTO;
import com.app.dto.CursoModificarDTO;
import com.app.dto.CursoSalidaDTO;
import com.app.model.Curso;
import com.app.service.CursoService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/cursos")
@SecurityRequirement(name = "bearerAuth")

public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ROOT','DIRECTOR','SECRETARIA')")
    public ResponseEntity<CursoSalidaDTO> altaCursos(@RequestBody CursoAltaDTO dto) {
        Curso nuevo = new Curso();
        nuevo.setCiclo_lectivo(dto.ciclo_lectivo());
        nuevo.setDivision(dto.division());
        nuevo.setGrado(dto.grado());
        nuevo.setTurno(dto.turno());
        nuevo.setMateria(dto.materia());
        nuevo.setHorario(dto.horario());
        nuevo.setCupo_maximo(dto.cupo_maximo());
        nuevo.setDocenteId(dto.docenteId());
        Curso creado = cursoService.altaCursos(nuevo);
        return ResponseEntity.status(HttpStatus.CREATED).body(CursoSalidaDTO.from(creado));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ROOT','DIRECTOR','SECRETARIA','PRECEPTOR','DOCENTE')")
    public ResponseEntity<List<CursoSalidaDTO>> listarCursos() {
        List<CursoSalidaDTO> cursos = cursoService.listarCursos()
                .stream()
                .map(CursoSalidaDTO::from)
                .toList();

        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ROOT','DIRECTOR','SECRETARIA','PRECEPTOR','DOCENTE')")
    public ResponseEntity<CursoSalidaDTO> obtenerPorId(@PathVariable Long id) {
        return cursoService.obtenerPorId(id)
                .map(CursoSalidaDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ROOT','DIRECTOR','SECRETARIA')")
    public ResponseEntity<CursoSalidaDTO> modificarCurso(
            @PathVariable Long id,
            @RequestBody CursoModificarDTO dto) {

        return cursoService.modificarCurso(id, dto)
                .map(CursoSalidaDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ROOT','DIRECTOR')")
    public ResponseEntity<String> eliminarCurso(@PathVariable Long id) {
        boolean eliminado = cursoService.eliminarCurso(id);

        if (!eliminado) {
            // Si no existe o tiene alumnos, no se elimina.
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se pudo eliminar: el curso no existe o tiene alumnos asociados.");
        }

        return ResponseEntity.noContent().build();
    }

    // Inscribir un alumno en el curso (controla el cupo maximo)
    @PostMapping("/{cursoId}/alumnos/{alumnoId}")
    @PreAuthorize("hasAnyRole('ADMIN','ROOT','DIRECTOR','SECRETARIA')")
    public ResponseEntity<Void> inscribirAlumno(
            @PathVariable Long cursoId,
            @PathVariable Long alumnoId) {

        cursoService.registrarALumno(cursoId, alumnoId);
        return ResponseEntity.noContent().build();
    }

    // Quitar un alumno del curso
    @DeleteMapping("/{cursoId}/alumnos/{alumnoId}")
    @PreAuthorize("hasAnyRole('ADMIN','ROOT','DIRECTOR')")
    public ResponseEntity<Void> quitarAlumno(
            @PathVariable Long cursoId,
            @PathVariable Long alumnoId) {

        cursoService.sacarALumno(cursoId, alumnoId);
        return ResponseEntity.noContent().build();
    }

}
