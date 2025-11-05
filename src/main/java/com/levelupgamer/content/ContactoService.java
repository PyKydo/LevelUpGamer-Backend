package com.levelupgamer.content;

import com.levelupgamer.common.service.EmailService;
import com.levelupgamer.content.dto.ContactoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class ContactoService {
    private final ContactoRepository contactoRepository;
    private final EmailService emailService;

    public ContactoService(ContactoRepository contactoRepository, EmailService emailService) {
        this.contactoRepository = contactoRepository;
        this.emailService = emailService;
    }

    @Transactional
    public ContactoDTO guardarMensaje(ContactoDTO dto) {
        Contacto contacto = Contacto.builder()
                .nombre(dto.getNombre())
                .correo(dto.getCorreo())
                .comentario(dto.getComentario())
                .fecha(LocalDateTime.now())
                .build();
        contacto = contactoRepository.save(contacto);

        // Enviar correo de confirmación
        String subject = "Gracias por contactarnos, " + dto.getNombre();
        String body = "Hemos recibido tu mensaje y te responderemos a la brevedad.";
        emailService.sendEmail(dto.getCorreo(), subject, body);

        return ContactoDTO.builder()
                .id(contacto.getId())
                .nombre(contacto.getNombre())
                .correo(contacto.getCorreo())
                .comentario(contacto.getComentario())
                .fecha(contacto.getFecha())
                .build();
    }
}

