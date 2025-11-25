package com.levelupgamer.contenido;

import com.levelupgamer.contenido.dto.ContactoDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contact-messages")
public class ContactoController {
    @Autowired
    private ContactoService contactoService;

    @PostMapping
    public ResponseEntity<ContactoDTO> enviarMensaje(@Valid @RequestBody ContactoDTO dto) {
        return ResponseEntity.ok(contactoService.guardarMensaje(dto));
    }
}
