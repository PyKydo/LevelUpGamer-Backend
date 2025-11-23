package com.levelupgamer.autenticacion;

import com.levelupgamer.autenticacion.dto.ChangePasswordRequest;
import com.levelupgamer.autenticacion.dto.LoginResponseDTO;
import com.levelupgamer.autenticacion.dto.RefreshTokenRequestDTO;
import com.levelupgamer.usuarios.RolUsuario;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AutenticacionService {
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AutenticacionService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder,
            JwtProvider jwtProvider) {
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

        // Si tiene más de un rol, devolver pre-auth token
        if (usuario.getRoles().size() > 1) {
            String preAuthToken = jwtProvider.generatePreAuthToken(usuario);
            return LoginResponseDTO.builder()
                    .preAuthToken(preAuthToken)
                    .roles(usuario.getRoles().stream().map(RolUsuario::name).collect(Collectors.toList()))
                    .usuarioId(usuario.getId())
                    .build();
        }

        // Si tiene un solo rol, flujo normal
        String accessToken = jwtProvider.generateAccessToken(usuario);
        String refreshToken = jwtProvider.generateRefreshToken(usuario);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .roles(usuario.getRoles().stream().map(RolUsuario::name).collect(Collectors.toList()))
                .usuarioId(usuario.getId())
                .build();
    }

    public LoginResponseDTO selectRole(RoleSelectionRequest request) {
        if (!jwtProvider.validateToken(request.getPreAuthToken())) {
            throw new BadCredentialsException("Token de pre-autenticación inválido o expirado");
        }

        Claims claims = jwtProvider.getClaims(request.getPreAuthToken());
        String type = (String) claims.get("type");
        if (!"PRE_AUTH".equals(type)) {
            throw new BadCredentialsException("Token inválido para selección de rol");
        }

        String correo = claims.getSubject();
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!usuario.getRoles().contains(request.getSelectedRole())) {
            throw new BadCredentialsException("El usuario no tiene el rol seleccionado");
        }

        String accessToken = jwtProvider.generateAccessToken(usuario, request.getSelectedRole());
        String refreshToken = jwtProvider.generateRefreshToken(usuario);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .roles(java.util.List.of(request.getSelectedRole().name()))
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

    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new BadCredentialsException("Usuario no autenticado");
        }

        String username = authentication.getName();

        Usuario usuario = usuarioRepository.findByCorreo(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), usuario.getContrasena())) {
            throw new BadCredentialsException("La contraseña actual es incorrecta");
        }

        usuario.setContrasena(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        usuarioRepository.save(usuario);
    }
}
