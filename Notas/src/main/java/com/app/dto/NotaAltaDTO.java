package com.app.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotaAltaDTO(
    @NotNull Long alumnoId,
    @NotNull Long cursoId,
    @NotBlank String materia,
    @NotNull @DecimalMin("0.0") @DecimalMax("10.0") Double nota,
    LocalDate fecha
) {}
