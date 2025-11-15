package com.levelupgamer.pedidos;

import com.levelupgamer.pedidos.dto.PedidoCrearDTO;
import com.levelupgamer.pedidos.dto.PedidoRespuestaDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class PedidoController {
    @Autowired
    private PedidoService pedidoService;

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR','CLIENTE')")
    @PostMapping
    public ResponseEntity<PedidoRespuestaDTO> crearPedido(@Valid @RequestBody PedidoCrearDTO dto) {
        return ResponseEntity.ok(pedidoService.crearPedido(dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PedidoRespuestaDTO>> listarPedidosPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(pedidoService.listarPedidosPorUsuario(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoRespuestaDTO> getPedido(@PathVariable Long id) {
        return pedidoService.buscarPorId(id)
                .map(PedidoMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
