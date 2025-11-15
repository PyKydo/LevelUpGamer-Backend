package com.levelupgamer.autenticacion;

import com.levelupgamer.usuarios.RolUsuario;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Set;

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
        usuario.setRoles(Set.of(RolUsuario.CLIENTE)); // Usar setRoles
    }

    @Test
    void login_conCredencialesValidas_retornaLoginResponse() {
        // Given
        when(usuarioRepository.findByCorreo("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        when(jwtProvider.generateToken(usuario)).thenReturn("test-token");

        // When
        LoginResponse result = autenticacionService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("test-token", result.getToken());
        assertTrue(result.getRoles().contains(RolUsuario.CLIENTE)); // Usar getRoles
        assertEquals(1L, result.getUsuarioId());
    }

    @Test
    void login_conUsuarioNoEncontrado_lanzaAuthenticationException() {
        // Given
        when(usuarioRepository.findByCorreo("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AuthenticationException.class, () -> autenticacionService.login(loginRequest));
    }

    @Test
    void login_conContrasenaIncorrecta_lanzaAuthenticationException() {
        // Given
        when(usuarioRepository.findByCorreo("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(false);

        // When & Then
        assertThrows(AuthenticationException.class, () -> autenticacionService.login(loginRequest));
    }
}
