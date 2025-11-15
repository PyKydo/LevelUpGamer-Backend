package com.levelupgamer.gamificacion;

import com.levelupgamer.gamificacion.dto.PuntosDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/points")
public class PuntosController {
    @Autowired
    private PuntosService puntosService;

    @GetMapping("/{userId}")
    public ResponseEntity<PuntosDTO> getPuntos(@PathVariable Long userId) {
        return ResponseEntity.ok(puntosService.obtenerPuntosPorUsuario(userId));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR','CLIENTE')")
    @PostMapping("/earn")
    public ResponseEntity<PuntosDTO> sumarPuntos(@RequestBody PuntosDTO dto) {
        return ResponseEntity.ok(puntosService.sumarPuntos(dto));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR','CLIENTE')")
    @PostMapping("/redeem")
    public ResponseEntity<PuntosDTO> canjearPuntos(@RequestBody PuntosDTO dto) {
        return ResponseEntity.ok(puntosService.canjearPuntos(dto));
    }
}
