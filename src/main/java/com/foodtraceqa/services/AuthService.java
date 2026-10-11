package com.foodtraceqa.services;

import com.foodtraceqa.dto.AuthDTO;
import com.foodtraceqa.entities.Usuario;
import com.foodtraceqa.enums.Rol;
import com.foodtraceqa.repositories.UsuarioRepository;
import com.foodtraceqa.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("El username ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(request.nombres());
        usuario.setApellidos(request.apellidos());
        usuario.setUsername(request.username());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(Rol.SUPERVISOR);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        return new AuthDTO.AuthResponse(jwtService.generateToken(usuario), usuario.getUsername(), usuario.getRol());
    }

    @Transactional
    public AuthDTO.AuthResponse registerByAdmin(AuthDTO.RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("El username ya está registrado");
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombres(request.nombres());
        usuario.setApellidos(request.apellidos());
        usuario.setUsername(request.username());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol() == null ? Rol.SUPERVISOR : request.rol());
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        return new AuthDTO.AuthResponse(jwtService.generateToken(usuario), usuario.getUsername(), usuario.getRol());
    }

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
                
        return new AuthDTO.AuthResponse(jwtService.generateToken(usuario), usuario.getUsername(), usuario.getRol());
    }
}
