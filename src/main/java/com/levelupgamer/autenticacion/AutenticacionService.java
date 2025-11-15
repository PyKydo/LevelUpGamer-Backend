package com.levelupgamer.autenticacion;

import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacionService {
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AutenticacionService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
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
            .roles(usuario.getRoles())
            .usuarioId(usuario.getId())
            .build();
    }
}
