package com.app.Auth.dto;

import com.app.Auth.entity.Rol;
import com.app.Auth.entity.Usuario;

// DTO de salida: nunca expone el hash de la contrasena.
public record UsuarioSalidaDTO(
    Long id,
    String username,
    Rol rol
) {

    public static UsuarioSalidaDTO from(Usuario usuario) {
        return new UsuarioSalidaDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRol()
        );
    }
}
