package com.levelupgamer.auth;

import com.levelupgamer.users.Usuario;
import com.levelupgamer.users.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Usuario usuario = usuarioRepository.findByCorreo(loginRequest.getCorreo())
            .orElseThrow(() -> new org.springframework.security.core.AuthenticationException("Usuario o contraseña inválidos") {});
        if (!passwordEncoder.matches(loginRequest.getContrasena(), usuario.getContrasena())) {
            throw new org.springframework.security.core.AuthenticationException("Usuario o contraseña inválidos") {};
        }
        String token = jwtProvider.generateToken(usuario);
        return LoginResponse.builder()
            .token(token)
            .rol(usuario.getRol())
            .usuarioId(usuario.getId())
            .build();
    }
}

