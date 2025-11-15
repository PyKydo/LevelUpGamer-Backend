package com.levelupgamer.usuarios;

import com.levelupgamer.usuarios.dto.UsuarioRegistroDTO;
import com.levelupgamer.usuarios.dto.UsuarioRespuestaDTO;
import com.levelupgamer.usuarios.dto.UsuarioUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioRespuestaDTO> registrar(@Valid @RequestBody UsuarioRegistroDTO dto) {
        UsuarioRespuestaDTO usuario = usuarioService.registrarUsuario(dto);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioRespuestaDTO> getUsuario(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(UsuarioMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioRespuestaDTO> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, dto));
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getRoles() {
        return ResponseEntity.ok(RolUsuario.values());
    }
}
