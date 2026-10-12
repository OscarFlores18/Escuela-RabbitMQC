package com.app.dto;

import com.app.Enum.Cargo;
import com.app.model.Personal;

// DTO de salida: expone solo los datos del personal, sin entidades JPA internas.
public record PersonalSalidaDTO(
    Long id,
    String nombre,
    String apellido,
    String email,
    Cargo cargo
) {

    public static PersonalSalidaDTO from(Personal personal) {
        return new PersonalSalidaDTO(
                personal.getId(),
                personal.getNombre(),
                personal.getApellido(),
                personal.getEmail(),
                personal.getCargo()
        );
    }
}
