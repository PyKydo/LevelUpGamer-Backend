package com.levelupgamer.autenticacion;

import com.levelupgamer.autenticacion.dto.ChangePasswordRequest;
import com.levelupgamer.autenticacion.dto.LoginResponseDTO;
import com.levelupgamer.autenticacion.dto.RefreshTokenRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {
    private final AutenticacionService autenticacionService;

    public AutenticacionController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(autenticacionService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequest) {
        return ResponseEntity.ok(autenticacionService.refreshToken(refreshTokenRequest));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        autenticacionService.changePassword(changePasswordRequest);
        return ResponseEntity.ok().build();
    }
}
