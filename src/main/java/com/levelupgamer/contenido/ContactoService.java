package com.levelupgamer.contenido;

import com.levelupgamer.contenido.dto.ContactoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class ContactoService {

    private static final Logger logger = LoggerFactory.getLogger(ContactoService.class);
    private final ContactoRepository contactoRepository;

    public ContactoService(ContactoRepository contactoRepository) {
        this.contactoRepository = contactoRepository;
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

        
        logger.info("Mensaje de contacto guardado de [{}]. El envío de correo de confirmación está desactivado.", dto.getCorreo());

        return ContactoDTO.builder()
                .id(contacto.getId())
                .nombre(contacto.getNombre())
                .correo(contacto.getCorreo())
                .comentario(contacto.getComentario())
                .fecha(contacto.getFecha())
                .build();
    }
}
