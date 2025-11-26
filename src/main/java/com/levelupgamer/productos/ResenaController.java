package com.levelupgamer.productos;

import com.levelupgamer.productos.dto.ResenaDTO;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class ResenaController {
    @Autowired
    private ResenaService resenaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMINISTRADOR')")
    @PostMapping("/reviews")
    public ResponseEntity<ResenaDTO> crearResena(@Valid @RequestBody ResenaDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Resena resena = new Resena();
        resena.setTexto(dto.getTexto());
        resena.setCalificacion(dto.getCalificacion());

        Resena guardada = resenaService.crearResena(dto.getProductoId(), usuario.getId(), resena);

        return ResponseEntity.ok(ResenaDTO.builder()
                .id(guardada.getId())
                .texto(guardada.getTexto())
                .calificacion(guardada.getCalificacion())
                .nombreUsuario(usuario.getNombre() + " " + usuario.getApellidos())
                .productoId(guardada.getProducto().getId())
                .createdAt(guardada.getCreatedAt())
                .build());
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<ResenaDTO>> listarResenas(@PathVariable Long productId) {
        List<Resena> resenas = resenaService.listarResenasPorProducto(productId);
        List<ResenaDTO> dtos = resenas.stream().map(r -> ResenaDTO.builder()
                .id(r.getId())
                .texto(r.getTexto())
                .calificacion(r.getCalificacion())
                .nombreUsuario(r.getUsuario().getNombre() + " " + r.getUsuario().getApellidos())
                .productoId(r.getProducto().getId())
                .createdAt(r.getCreatedAt())
                .build()).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMINISTRADOR')")
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> eliminarResena(@PathVariable Long id) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMINISTRADOR".equals(authority.getAuthority()));
        
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        resenaService.eliminarResena(id, usuario.getId(), esAdmin);
        return ResponseEntity.noContent().build();
    }
}
