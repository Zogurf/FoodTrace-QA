package com.foodtraceqa.dto;

import com.foodtraceqa.enums.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDTO {
    private AuthDTO() {
    }

    public record RegisterRequest(
            @NotBlank String nombres,
            @NotBlank String apellidos,
            @NotBlank @Size(min = 4, max = 80) String username,
            @NotBlank @Size(min = 8, max = 120) String password,
            Rol rol) {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record AuthResponse(String token, String username, Rol rol) {
    }
}
