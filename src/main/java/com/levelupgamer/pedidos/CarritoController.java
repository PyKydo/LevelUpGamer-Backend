package com.levelupgamer.pedidos;

import com.levelupgamer.pedidos.dto.CarritoDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CarritoDto> getCartByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(carritoService.getCartByUserId(userId));
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<CarritoDto> addProductToCart(@PathVariable Long userId,
                                                       @RequestParam Long productId,
                                                       @RequestParam int quantity) {
        return ResponseEntity.ok(carritoService.addProductToCart(userId, productId, quantity));
    }

    @DeleteMapping("/{userId}/remove")
    public ResponseEntity<CarritoDto> removeProductFromCart(@PathVariable Long userId,
                                                            @RequestParam Long productId) {
        return ResponseEntity.ok(carritoService.removeProductFromCart(userId, productId));
    }
}
