package com.app.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.app.dto.AlumnoAltaDTO;
import com.app.dto.AlumnoSalidaDTO;
import com.app.dto.AlumnoConCursoDTO;
import com.app.dto.AlumnoModificarDTO;
import com.app.model.Alumno;
import com.app.service.AlumnoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alumnos")
@SecurityRequirement(name = "bearerAuth")

public class AlumnoController {

    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    // Alta de alumno: solo preceptor
    @PostMapping
    @PreAuthorize("hasAnyRole('PRECEPTOR','ADMIN','ROOT')")
    public ResponseEntity<AlumnoSalidaDTO> altaAlumno(@RequestBody AlumnoAltaDTO dto) {
        Alumno nuevo = new Alumno();
        nuevo.setNombre(dto.nombre());
        nuevo.setApellido(dto.apellido());
        nuevo.setDni(dto.dni());
        nuevo.setEmail(dto.email());
        Alumno creado = alumnoService.altaAlumno(nuevo, dto.cursoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(AlumnoSalidaDTO.from(creado));
    }

    // Lectura: root, director, docente, preceptor
    @GetMapping
    @PreAuthorize("hasAnyRole('ROOT','ADMIN','DOCENTE','PRECEPTOR','SECRETARIA','ADMINISTRATIVO')")
    public ResponseEntity<List<AlumnoSalidaDTO>> listarAlumnos() {
        List<AlumnoSalidaDTO> alumnos = alumnoService.listarAlumnos()
                .stream()
                .map(AlumnoSalidaDTO::from)
                .toList();

        return ResponseEntity.ok(alumnos);
    }

    @GetMapping("/{id}/con-curso")
    @PreAuthorize("hasAnyRole('ROOT','ADMIN','DOCENTE','PRECEPTOR','SECRETARIA','ADMINISTRATIVO')")
    public ResponseEntity<AlumnoConCursoDTO> obtenerConCurso(@PathVariable Long id) {
        return ResponseEntity.ok(alumnoService.obtenerConCurso(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRECEPTOR','ADMIN','ROOT')")
    public ResponseEntity<AlumnoSalidaDTO> modificarAlumno(
            @PathVariable Long id,
            @RequestBody AlumnoModificarDTO dto) {

        return alumnoService.modificarAlumno(id, dto)
            .map(AlumnoSalidaDTO::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRECEPTOR','ADMIN','ROOT')")
    public ResponseEntity<Void> eliminarAlumno(
            @PathVariable Long id) {

        if (!alumnoService.eliminarAlumno(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

}
