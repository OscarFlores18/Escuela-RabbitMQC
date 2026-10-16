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
import com.app.dto.PersonalModificarDTO;
import com.app.dto.PersonalSalidaDTO;
import com.app.dto.PersonalAltaDTO;
import com.app.model.Personal;
import com.app.service.PersonalService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/personal")
@SecurityRequirement(name = "bearerAuth")

public class PersonalController {

	private final PersonalService personalService;

	public PersonalController(PersonalService personalService) {
		this.personalService = personalService;
	}

	// Gestion de personal: solo administracion/root
	@PostMapping
	@PreAuthorize("hasAnyRole('ROOT','ADMIN','PRECEPTOR')")
	public ResponseEntity<PersonalSalidaDTO> altaPersonal(@RequestBody PersonalAltaDTO dto) {
	    Personal nuevo = new Personal();
	    nuevo.setNombre(dto.nombre());
	    nuevo.setApellido(dto.apellido());
	    nuevo.setEmail(dto.email());
	    nuevo.setCargo(dto.cargo());

	    Personal creado = personalService.altaPersonal(nuevo);
	    return ResponseEntity.status(HttpStatus.CREATED).body(PersonalSalidaDTO.from(creado));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ROOT','ADMIN','DIRECTOR','PRECEPTOR')")
	public ResponseEntity<List<PersonalSalidaDTO>> listarPersonal() {
		List<PersonalSalidaDTO> personal = personalService.listarPersonal()
				.stream()
				.map(PersonalSalidaDTO::from)
				.toList();

		return ResponseEntity.ok(personal);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ROOT','ADMIN','DIRECTOR','PRECEPTOR')")
	    public ResponseEntity<PersonalSalidaDTO> obtenerPorId(@PathVariable Long id) {
	        return personalService.obtenerPorId(id)
	                .map(PersonalSalidaDTO::from)
	                .map(ResponseEntity::ok)
	                .orElse(ResponseEntity.notFound().build());
	    }

	// Listado basico accesible a cualquier usuario interno autenticado:
	// se usa para mostrar/asignar docentes en los cursos (relacion curso -> docente).
	@GetMapping("/basico")
	public ResponseEntity<List<PersonalSalidaDTO>> listarPersonalBasico() {
		List<PersonalSalidaDTO> personal = personalService.listarPersonal()
				.stream()
				.map(PersonalSalidaDTO::from)
				.toList();

		return ResponseEntity.ok(personal);
	}


	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ROOT','ADMIN','PRECEPTOR')")
	public ResponseEntity<PersonalSalidaDTO> modificarPersonal(
	        @PathVariable Long id,
	        @RequestBody PersonalModificarDTO dto) {

	    return personalService.modificarPersonal(id, dto)
	            .map(PersonalSalidaDTO::from)
	            .map(ResponseEntity::ok)
	            .orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ROOT','ADMIN','PRECEPTOR')")
	public ResponseEntity<Void> eliminarPersonal(@PathVariable Long id) {
	    if (!personalService.eliminarPersonal(id)) {
	        return ResponseEntity.notFound().build();
	    }

	    return ResponseEntity.noContent().build();
	}



}
