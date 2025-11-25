package com.levelupgamer.boletas;

import com.levelupgamer.boletas.dto.BoletaCrearRequest;
import com.levelupgamer.boletas.dto.BoletaRespuestaDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
public class BoletaController {
    @Autowired
    private BoletaService boletaService;

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CLIENTE')")
    @PostMapping
    public ResponseEntity<BoletaRespuestaDTO> crearBoleta(@Valid @RequestBody BoletaCrearRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boletaService.crearBoleta(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BoletaRespuestaDTO>> listarBoletasPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(boletaService.listarBoletasPorUsuario(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoletaRespuestaDTO> getBoleta(@PathVariable Long id) {
        return boletaService.buscarPorId(id)
                .map(BoletaMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
