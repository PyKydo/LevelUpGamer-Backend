package com.levelupgamer.autenticacion;

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
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        validator.validate(loginRequest);
        return ResponseEntity.ok(autenticacionService.login(loginRequest));
    }
}

