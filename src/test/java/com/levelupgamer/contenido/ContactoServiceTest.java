package com.levelupgamer.contenido;

import com.levelupgamer.common.service.EmailService;
import com.levelupgamer.contenido.dto.ContactoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ContactoServiceTest {

    @Mock
    private ContactoRepository contactoRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ContactoService contactoService;

    private ContactoDTO contactoDTO;
    private Contacto contacto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        contactoDTO = ContactoDTO.builder()
                .nombre("Test User")
                .correo("test@example.com")
                .comentario("This is a test comment.")
                .build();

        contacto = Contacto.builder()
                .id(1L)
                .nombre("Test User")
                .correo("test@example.com")
                .comentario("This is a test comment.")
                .fecha(LocalDateTime.now())
                .build();
    }

    @Test
    void guardarMensaje_guardaMensajeYEnviaCorreo() {
        // Given
        when(contactoRepository.save(any(Contacto.class))).thenReturn(contacto);
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // When
        ContactoDTO result = contactoService.guardarMensaje(contactoDTO);

        // Then
        assertNotNull(result);
        assertEquals(contacto.getId(), result.getId());
        assertEquals(contactoDTO.getNombre(), result.getNombre());

        verify(contactoRepository, times(1)).save(any(Contacto.class));
        verify(emailService, times(1)).sendEmail(
                eq(contactoDTO.getCorreo()),
                anyString(),
                anyString()
        );
    }
}