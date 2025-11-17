package com.levelupgamer.autenticacion;

import com.levelupgamer.autenticacion.dto.LoginResponseDTO;
import com.levelupgamer.autenticacion.dto.RefreshTokenRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Validator;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {
    private final AutenticacionService autenticacionService;
    private final Validator validator;

    public AutenticacionController(AutenticacionService autenticacionService, Validator validator) {
        this.autenticacionService = autenticacionService;
        this.validator = validator;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequest loginRequest) {
        validator.validate(loginRequest);
        return ResponseEntity.ok(autenticacionService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequest) {
        return ResponseEntity.ok(autenticacionService.refreshToken(refreshTokenRequest));
    }
}
