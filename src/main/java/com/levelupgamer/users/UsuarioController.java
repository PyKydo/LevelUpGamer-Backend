package com.levelupgamer.users;

import com.levelupgamer.users.dto.UsuarioRegistroDTO;
import com.levelupgamer.users.dto.UsuarioRespuestaDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Validator;

@RestController
@RequestMapping("/api/users")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final Validator validator;

    public UsuarioController(UsuarioService usuarioService, Validator validator) {
        this.usuarioService = usuarioService;
        this.validator = validator;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioRespuestaDTO> registrar(@RequestBody UsuarioRegistroDTO dto) {
        validator.validate(dto);
        UsuarioRespuestaDTO usuario = usuarioService.registrarUsuario(dto);
        return ResponseEntity.ok(usuario);
    }

    // Endpoint de ejemplo para obtener usuario por id (protegido en seguridad)
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioRespuestaDTO> getUsuario(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(UsuarioMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioRespuestaDTO> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioRegistroDTO dto) {
        validator.validate(dto);
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, dto));
    }

    // Endpoint para obtener roles (solo admin)
    @GetMapping("/roles")
    public ResponseEntity<?> getRoles() {
        return ResponseEntity.ok(RolUsuario.values());
    }
}
