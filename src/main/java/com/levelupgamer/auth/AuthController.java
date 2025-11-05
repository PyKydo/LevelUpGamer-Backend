package com.levelupgamer.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;

import jakarta.validation.Validator;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final Validator validator;

    public AuthController(AuthService authService, Validator validator) {
        this.authService = authService;
        this.validator = validator;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        validator.validate(loginRequest);
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}

