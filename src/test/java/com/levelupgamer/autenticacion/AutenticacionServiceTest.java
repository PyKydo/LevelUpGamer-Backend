package com.levelupgamer.autenticacion;

import com.levelupgamer.autenticacion.dto.LoginResponseDTO;
import com.levelupgamer.usuarios.RolUsuario;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AutenticacionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AutenticacionService autenticacionService;

    private LoginRequest loginRequest;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        loginRequest = new LoginRequest();
        loginRequest.setCorreo("test@example.com");
        loginRequest.setContrasena("password");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCorreo("test@example.com");
        usuario.setContrasena("hashedPassword");
        usuario.setRoles(Set.of(RolUsuario.CLIENTE));
    }

    @Test
    void login_conCredencialesValidas_retornaLoginResponseDTO() {
        // Given
        when(usuarioRepository.findByCorreo("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        when(jwtProvider.generateAccessToken(usuario)).thenReturn("test-access-token");
        when(jwtProvider.generateRefreshToken(usuario)).thenReturn("test-refresh-token");

        // When
        LoginResponseDTO result = autenticacionService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("test-access-token", result.getAccessToken());
        assertEquals("test-refresh-token", result.getRefreshToken());
        assertTrue(result.getRoles().contains("CLIENTE"));
        assertEquals(1L, result.getUsuarioId());
    }

    @Test
    void login_conUsuarioNoEncontrado_lanzaBadCredentialsException() {
        // Given
        when(usuarioRepository.findByCorreo("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BadCredentialsException.class, () -> autenticacionService.login(loginRequest));
    }

    @Test
    void login_conContrasenaIncorrecta_lanzaBadCredentialsException() {
        // Given
        when(usuarioRepository.findByCorreo("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(false);

        // When & Then
        assertThrows(BadCredentialsException.class, () -> autenticacionService.login(loginRequest));
    }

    @Test
    void login_conRolSeleccionado_retornaLoginResponseDTO() {
        // Given
        usuario.setRoles(Set.of(RolUsuario.CLIENTE, RolUsuario.ADMINISTRADOR));
        loginRequest.setRol(RolUsuario.ADMINISTRADOR);

        when(usuarioRepository.findByCorreo("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        when(jwtProvider.generateAccessToken(usuario, RolUsuario.ADMINISTRADOR)).thenReturn("test-access-token-admin");
        when(jwtProvider.generateRefreshToken(usuario)).thenReturn("test-refresh-token");

        // When
        LoginResponseDTO result = autenticacionService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("test-access-token-admin", result.getAccessToken());
        assertTrue(result.getRoles().contains("ADMINISTRADOR"));
    }

    @Test
    void login_conMultiplesRolesYSinSeleccion_lanzaBadCredentialsException() {
        // Given
        usuario.setRoles(Set.of(RolUsuario.CLIENTE, RolUsuario.ADMINISTRADOR));
        loginRequest.setRol(null);

        when(usuarioRepository.findByCorreo("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);

        // When & Then
        assertThrows(BadCredentialsException.class, () -> autenticacionService.login(loginRequest));
    }

    @Test
    void login_conRolNoAsignado_lanzaBadCredentialsException() {
        // Given
        usuario.setRoles(Set.of(RolUsuario.CLIENTE));
        loginRequest.setRol(RolUsuario.ADMINISTRADOR);

        when(usuarioRepository.findByCorreo("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);

        // When & Then
        assertThrows(BadCredentialsException.class, () -> autenticacionService.login(loginRequest));
    }
}
