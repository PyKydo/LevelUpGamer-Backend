package com.levelupgamer.gamificacion.cupones;

import com.levelupgamer.gamificacion.cupones.dto.CuponDTO;
import com.levelupgamer.gamificacion.cupones.dto.RedeemCouponRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class CuponController {

    private final CuponService cuponService;

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','CLIENTE')")
    @PostMapping("/redeem-coupon")
    public ResponseEntity<CuponDTO> canjearCupon(@Valid @RequestBody RedeemCouponRequest request) {
        return ResponseEntity.ok(cuponService.canjearPorCupon(request));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','CLIENTE')")
    @GetMapping("/coupons/{userId}")
    public ResponseEntity<List<CuponDTO>> listarCupones(@PathVariable Long userId) {
        return ResponseEntity.ok(cuponService.listarCuponesActivosPorUsuario(userId));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','CLIENTE')")
    @GetMapping("/coupon-options")
    public ResponseEntity<Map<Integer, Integer>> obtenerConversiones() {
        return ResponseEntity.ok(cuponService.obtenerTablaConversion());
    }
}
