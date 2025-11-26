package com.levelupgamer.boletas;

import com.levelupgamer.boletas.dto.BoletaActualizarEstadoRequest;
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

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @GetMapping
    public ResponseEntity<List<BoletaRespuestaDTO>> listarBoletas() {
        return ResponseEntity.ok(boletaService.listarTodas());
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR', 'CLIENTE')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BoletaRespuestaDTO>> listarBoletasPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(boletaService.listarBoletasPorUsuario(userId));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR', 'CLIENTE')")
    @GetMapping("/{id}")
    public ResponseEntity<BoletaRespuestaDTO> getBoleta(@PathVariable Long id) {
        return boletaService.buscarPorId(id)
                .map(BoletaMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<BoletaRespuestaDTO> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody BoletaActualizarEstadoRequest request) {
        return ResponseEntity.ok(boletaService.actualizarEstado(id, request.getEstado()));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarBoleta(@PathVariable Long id) {
        boletaService.eliminarBoleta(id);
        return ResponseEntity.noContent().build();
    }
}
