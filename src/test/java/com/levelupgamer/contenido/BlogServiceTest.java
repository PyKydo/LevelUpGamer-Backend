package com.levelupgamer.contenido;

import com.levelupgamer.contenido.dto.BlogDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @InjectMocks
    private BlogService blogService;

    private Blog blog;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        blog = new Blog();
        blog.setId(1L);
        blog.setTitulo("Test Blog");
        blog.setImagenUrl("http://example.com/image.jpg");
        blog.setDescripcionCorta("Short description");
        blog.setContenidoUrl("/path/to/content.md"); // Corregido
        blog.setFechaPublicacion(LocalDate.now());
    }

    @Test
    void listarBlogs_retornaListaDeBlogDTO() {
        // Given
        when(blogRepository.findAll()).thenReturn(Collections.singletonList(blog));

        // When
        List<BlogDTO> result = blogService.listarBlogs();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(blog.getTitulo(), result.get(0).getTitulo());
    }

    @Test
    void buscarPorId_blogExistente_retornaOptionalConBlog() {
        // Given
        when(blogRepository.findById(1L)).thenReturn(Optional.of(blog));

        // When
        Optional<Blog> result = blogService.buscarPorId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(blog, result.get());
    }

    @Test
    void buscarPorId_blogNoExistente_retornaOptionalVacio() {
        // Given
        when(blogRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<Blog> result = blogService.buscarPorId(1L);

        // Then
        assertFalse(result.isPresent());
    }
}
