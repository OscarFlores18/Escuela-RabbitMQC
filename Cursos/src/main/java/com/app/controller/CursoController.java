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
    @PreAuthorize("hasAnyRole('ROOT','PRECEPTOR','ADMIN')")
    public ResponseEntity<CursoSalidaDTO> altaCursos(@RequestBody CursoAltaDTO dto) {
        Curso nuevo = new Curso();
        nuevo.setCiclo_lectivo(dto.ciclo_lectivo());
        nuevo.setDivision(dto.division());
        nuevo.setGrado(dto.grado());
        nuevo.setTurno(dto.turno());
        nuevo.setHorario(dto.horario());
        nuevo.setCupo_maximo(dto.cupo_maximo());
        nuevo.setDocenteId(dto.docenteId());
        Curso creado = cursoService.altaCursos(nuevo);
        return ResponseEntity.status(HttpStatus.CREATED).body(CursoSalidaDTO.from(creado));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROOT','ADMIN','DIRECTOR','DOCENTE','PRECEPTOR','SECRETARIA','ADMINISTRATIVO')")
    public ResponseEntity<List<CursoSalidaDTO>> listarCursos() {
        List<CursoSalidaDTO> cursos = cursoService.listarCursos()
                .stream()
                .map(CursoSalidaDTO::from)
                .toList();

        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROOT','ADMIN','DIRECTOR','DOCENTE','PRECEPTOR','SECRETARIA','ADMINISTRATIVO')")
    public ResponseEntity<CursoSalidaDTO> obtenerPorId(@PathVariable Long id) {
        return cursoService.obtenerPorId(id)
                .map(CursoSalidaDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("denyAll()")
    public ResponseEntity<CursoSalidaDTO> modificarCurso(
            @PathVariable Long id,
            @RequestBody CursoModificarDTO dto) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("denyAll()")
    public ResponseEntity<String> eliminarCurso(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Inscribir/Quitar alumno: denegado segun requerimiento
    @PostMapping("/{cursoId}/alumnos/{alumnoId}")
    @PreAuthorize("denyAll()")
    public ResponseEntity<Void> inscribirAlumno(
            @PathVariable Long cursoId,
            @PathVariable Long alumnoId) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @DeleteMapping("/{cursoId}/alumnos/{alumnoId}")
    @PreAuthorize("denyAll()")
    public ResponseEntity<Void> quitarAlumno(
            @PathVariable Long cursoId,
            @PathVariable Long alumnoId) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

}
