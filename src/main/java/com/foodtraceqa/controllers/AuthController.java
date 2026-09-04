package com.foodtraceqa.controllers;

import com.foodtraceqa.dto.AuthDTO;
import com.foodtraceqa.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDTO.AuthResponse register(@Valid @RequestBody AuthDTO.RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthDTO.AuthResponse login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public AuthDTO.AuthResponse registerByAdmin(@Valid @RequestBody AuthDTO.RegisterRequest request) {
        return authService.registerByAdmin(request);
    }
}
