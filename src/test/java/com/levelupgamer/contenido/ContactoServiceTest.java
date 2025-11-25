package com.levelupgamer.contenido;

import com.levelupgamer.contenido.dto.ContactoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactoServiceTest {

    @Mock
    private ContactoRepository contactoRepository;

    @InjectMocks
    private ContactoService contactoService;

    private ContactoDTO contactoDTO;
    private Contacto contacto;

    @BeforeEach
    void setUp() {
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
    void guardarMensaje_debeGuardarMensaje() {
        
        when(contactoRepository.save(any(Contacto.class))).thenReturn(contacto);

        
        ContactoDTO result = contactoService.guardarMensaje(contactoDTO);

        
        assertNotNull(result);
        assertEquals(contacto.getId(), result.getId());
        assertEquals(contactoDTO.getNombre(), result.getNombre());

        verify(contactoRepository, times(1)).save(any(Contacto.class));
    }
}
