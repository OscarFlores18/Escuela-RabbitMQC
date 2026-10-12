package com.app.Auth.dto;

import com.app.Auth.entity.Rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// password opcional: si viene vacio, se conserva la contrasena actual.
public record UsuarioModificarDTO(
    @NotBlank String username,
    String password,
    @NotNull Rol rol
) {}
