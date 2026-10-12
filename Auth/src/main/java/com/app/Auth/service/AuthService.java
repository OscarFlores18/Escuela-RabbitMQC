package com.app.Auth.service;

import com.app.Auth.dto.LoginRequest;
import com.app.Auth.dto.LoginResponse;
import com.app.Auth.entity.Usuario;
import com.app.Auth.exception.CredencialesInvalidasException;
import com.app.Auth.repository.UsuarioRepository;
import com.app.Auth.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        Usuario usuario = usuarioRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new CredencialesInvalidasException("Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                usuario.getPassword())) {

            throw new CredencialesInvalidasException(
                    "Usuario o contraseña incorrectos");
        }

        String token = jwtService.generarToken(usuario);

        return new LoginResponse(token);
    }
}
