package com.levelupgamer.content;

import com.levelupgamer.content.dto.ContactoDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact-messages")
public class ContactoController {
    @Autowired
    private ContactoService contactoService;

    @PostMapping
    public ResponseEntity<ContactoDTO> enviarMensaje(@Valid @RequestBody ContactoDTO dto) {
        return ResponseEntity.ok(contactoService.guardarMensaje(dto));
    }
}
