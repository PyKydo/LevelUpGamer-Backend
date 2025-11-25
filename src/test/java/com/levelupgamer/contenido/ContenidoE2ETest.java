package com.levelupgamer.contenido;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupgamer.contenido.dto.ContactoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ContenidoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BlogRepository blogRepository;

    @BeforeEach
    void setUp() {
        blogRepository.deleteAll();
    }

    @Test
    void deberiaListarEntradasDeBlog() throws Exception {
        
        Blog blog = Blog.builder()
                .titulo("Blog de Prueba E2E")
                .autor("Tester")
                .fechaPublicacion(LocalDate.now())
                .descripcionCorta("Este es un resumen de prueba.")
                .contenidoUrl("/test/blog.md")
                .imagenUrl("/test/image.jpg")
                .build();
        blogRepository.save(blog);

        
        mockMvc.perform(get("/api/blog-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo", is("Blog de Prueba E2E")));
    }

    @Test
    void deberiaEnviarMensajeDeContacto() throws Exception {
        
        ContactoDTO contactoDTO = ContactoDTO.builder()
                .nombre("Usuario de Contacto")
                .correo("contacto@example.com")
                .comentario("Este es un mensaje de prueba E2E.")
                .build();

        
        mockMvc.perform(post("/api/contact-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contactoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Usuario de Contacto")));
    }
}
