package com.app.Auth.dto;

import com.app.Auth.entity.Rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioAltaDTO(
    @NotBlank String username,
    @NotBlank String password,
    @NotNull Rol rol,
    Long personalId
) {}
