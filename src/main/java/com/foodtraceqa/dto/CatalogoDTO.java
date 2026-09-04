package com.foodtraceqa.dto;

import com.foodtraceqa.enums.Medida;
import jakarta.validation.constraints.*;

public final class CatalogoDTO {
    private CatalogoDTO() {
    }

    public record ProveedorRequest(@NotNull Long ruc, @NotBlank String nombre,
                                   @Min(0) @Max(5) int calificacion) {
    }

    public record MateriaRequest(@NotBlank String nombre, String descripcion,
                                 @NotNull Medida unidadMedida) {
    }

    public record ProductoRequest(@NotBlank String nombre, String descripcion) {
    }
}
