package com.app.service;

import java.util.List;
import java.util.Optional;
import com.app.dto.PersonalModificarDTO;
import org.springframework.stereotype.Service;
import com.app.model.Personal;
import com.app.repository.PersonalRepository;

@Service
public class PersonalServiceImpl implements PersonalService {

    private final PersonalRepository personalRepository;

    public PersonalServiceImpl(PersonalRepository personalRepository) {
        this.personalRepository = personalRepository;
    }

    @Override
    public Personal altaPersonal(Personal personal) {
        personal.setId(null); // el alta nunca acepta un id externo — lo genera la base
        personal.setActivo(true);
        return personalRepository.save(personal);
    }

    @Override
    public List<Personal> listarPersonal() {
        return personalRepository.findByActivoTrue();
    }

	@Override
	public Optional<Personal> obtenerPorId(Long id) {
        return personalRepository.findByIdAndActivoTrue(id);

	}

	@Override
	public Optional<Personal> modificarPersonal(Long id, PersonalModificarDTO dto) {
	    return personalRepository.findByIdAndActivoTrue(id).map(personal -> {
	        personal.setNombre(dto.nombre());
	        personal.setApellido(dto.apellido());
	        personal.setEmail(dto.email());
	        personal.setCargo(dto.cargo());

	        return personalRepository.save(personal);
	    });
	}

	@Override
	public boolean eliminarPersonal(Long id) {
	    // Baja logica: marca inactivo en lugar de borrar la fila
	    return personalRepository.findById(id)
	        .filter(Personal::isActivo)
	        .map(personal -> {
	            personal.setActivo(false);
	            personalRepository.save(personal);
	            return true;
	        })
	        .orElse(false);
	}

}
