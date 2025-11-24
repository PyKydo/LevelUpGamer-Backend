package com.levelupgamer.autenticacion;

import com.levelupgamer.autenticacion.dto.ChangePasswordRequest;
import com.levelupgamer.autenticacion.dto.LoginResponseDTO;
import com.levelupgamer.autenticacion.dto.RefreshTokenRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacionController {
    private final AutenticacionService autenticacionService;

    /**
     * Inicia sesión en el sistema.
     *
     * @param loginRequest Credenciales de acceso.
     * @return Respuesta con token y roles.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(autenticacionService.login(loginRequest));
    }

    /**
     * Refresca el token de acceso.
     *
     * @param refreshTokenRequest Token de refresco.
     * @return Nuevo token de acceso.
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequest) {
        return ResponseEntity.ok(autenticacionService.refreshToken(refreshTokenRequest));
    }

    /**
     * Cambia la contraseña del usuario.
     *
     * @param changePasswordRequest Solicitud de cambio de contraseña.
     * @return Respuesta vacía si es exitoso.
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        autenticacionService.changePassword(changePasswordRequest);
        return ResponseEntity.ok().build();
    }
}
