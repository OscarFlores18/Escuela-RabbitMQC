package com.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.dto.NotaAltaDTO;
import com.app.dto.NotaModificarDTO;
import com.app.dto.NotaSalidaDTO;
import com.app.model.Nota;
import com.app.service.NotaService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/notas")
@SecurityRequirement(name = "bearerAuth")
public class NotaController {

    private final NotaService notaService;

    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    // Carga de notas: solo docente
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCENTE','ADMIN','ROOT','SECRETARIA','PRECEPTOR','ADMINISTRATIVO','DIRECTOR')")
    public ResponseEntity<NotaSalidaDTO> altaNota(@Valid @RequestBody NotaAltaDTO dto) {
        Nota nueva = new Nota();
        nueva.setAlumnoId(dto.alumnoId());
        nueva.setCursoId(dto.cursoId());
        nueva.setMateria(dto.materia());
        nueva.setNota(dto.nota());
        nueva.setFecha(dto.fecha());

        Nota creada = notaService.altaNota(nueva);
        return ResponseEntity.status(HttpStatus.CREATED).body(NotaSalidaDTO.from(creada));
    }

    // Consulta: docente y director
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCENTE','DIRECTOR','ROOT','PRECEPTOR','ADMIN','SECRETARIA','ADMINISTRATIVO')")
    public ResponseEntity<List<NotaSalidaDTO>> listarNotas() {
        List<NotaSalidaDTO> notas = notaService.listarNotas()
                .stream()
                .map(NotaSalidaDTO::from)
                .toList();

        return ResponseEntity.ok(notas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCENTE','DIRECTOR','ROOT','PRECEPTOR','ADMIN','SECRETARIA','ADMINISTRATIVO')")
    public ResponseEntity<NotaSalidaDTO> obtenerPorId(@PathVariable Long id) {
        return notaService.obtenerPorId(id)
                .map(NotaSalidaDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/alumno/{alumnoId}")
    @PreAuthorize("hasAnyRole('DOCENTE','DIRECTOR','ROOT','PRECEPTOR','ADMIN','SECRETARIA','ADMINISTRATIVO')")
    public ResponseEntity<List<NotaSalidaDTO>> listarPorAlumno(@PathVariable Long alumnoId) {
        List<NotaSalidaDTO> notas = notaService.listarPorAlumno(alumnoId)
                .stream()
                .map(NotaSalidaDTO::from)
                .toList();

        return ResponseEntity.ok(notas);
    }

    @GetMapping("/curso/{cursoId}")
    @PreAuthorize("hasAnyRole('DOCENTE','DIRECTOR','ROOT','PRECEPTOR','ADMIN','SECRETARIA','ADMINISTRATIVO')")
    public ResponseEntity<List<NotaSalidaDTO>> listarPorCurso(@PathVariable Long cursoId) {
        List<NotaSalidaDTO> notas = notaService.listarPorCurso(cursoId)
                .stream()
                .map(NotaSalidaDTO::from)
                .toList();

        return ResponseEntity.ok(notas);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCENTE','ADMIN','ROOT','SECRETARIA','PRECEPTOR','ADMINISTRATIVO','DIRECTOR')")
    public ResponseEntity<NotaSalidaDTO> modificarNota(
            @PathVariable Long id,
            @Valid @RequestBody NotaModificarDTO dto) {

        return notaService.modificarNota(id, dto)
                .map(NotaSalidaDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCENTE','ADMIN','ROOT','SECRETARIA','PRECEPTOR','ADMINISTRATIVO','DIRECTOR')")
    public ResponseEntity<Void> eliminarNota(@PathVariable Long id) {
        if (!notaService.eliminarNota(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
