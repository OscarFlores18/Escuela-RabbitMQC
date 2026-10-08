package com.app.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.app.dto.AlumnoAltaDTO;
import com.app.dto.AlumnoConCursoDTO;
import com.app.dto.AlumnoModificarDTO;
import com.app.model.Alumno;
import com.app.service.AlumnoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<Alumno> altaAlumno(@RequestBody AlumnoAltaDTO dto) {
        Alumno nuevo = new Alumno();
        nuevo.setNombre(dto.nombre());
        nuevo.setApellido(dto.apellido());
        nuevo.setDni(dto.dni());
        nuevo.setEmail(dto.email());
        Alumno creado = alumnoService.altaAlumno(nuevo, dto.cursoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<Alumno>> listarAlumnos() {
        return ResponseEntity.ok(alumnoService.listarAlumnos());
    }

    @GetMapping("/{id}/con-curso")
    public ResponseEntity<AlumnoConCursoDTO> obtenerConCurso(@PathVariable Long id) {
        return ResponseEntity.ok(alumnoService.obtenerConCurso(id));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Alumno> modificarAlumno(
            @PathVariable Long id,
            @RequestBody AlumnoModificarDTO dto) {

        return alumnoService.modificarAlumno(id, dto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAlumno(
            @PathVariable Long id) {

        if (!alumnoService.eliminarAlumno(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
    
}