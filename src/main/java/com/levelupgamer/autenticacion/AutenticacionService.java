package com.levelupgamer.autenticacion;

import com.levelupgamer.autenticacion.dto.LoginResponseDTO;
import com.levelupgamer.autenticacion.dto.RefreshTokenRequestDTO;
import com.levelupgamer.usuarios.RolUsuario;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

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

    public LoginResponseDTO login(LoginRequest loginRequest) {
        Usuario usuario = usuarioRepository.findByCorreo(loginRequest.getCorreo())
            .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña inválidos"));
        if (!passwordEncoder.matches(loginRequest.getContrasena(), usuario.getContrasena())) {
            throw new BadCredentialsException("Usuario o contraseña inválidos");
        }
        String accessToken = jwtProvider.generateAccessToken(usuario);
        String refreshToken = jwtProvider.generateRefreshToken(usuario);

        return LoginResponseDTO.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .roles(usuario.getRoles().stream().map(RolUsuario::name).collect(Collectors.toList()))
            .usuarioId(usuario.getId())
            .build();
    }

    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        if (jwtProvider.validateToken(refreshToken)) {
            Claims claims = jwtProvider.getClaims(refreshToken);
            String correo = claims.getSubject();
            Usuario usuario = usuarioRepository.findByCorreo(correo)
                    .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado desde el refresh token"));

            String newAccessToken = jwtProvider.generateAccessToken(usuario);

            return LoginResponseDTO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) // Se puede devolver el mismo refresh token
                    .roles(usuario.getRoles().stream().map(RolUsuario::name).collect(Collectors.toList()))
                    .usuarioId(usuario.getId())
                    .build();
        } else {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }
    }
}
