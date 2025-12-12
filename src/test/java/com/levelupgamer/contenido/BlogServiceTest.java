package com.levelupgamer.contenido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.levelupgamer.common.storage.FileStorageService;
import com.levelupgamer.contenido.dto.BlogDTO;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private FileStorageService storageService;

    @Mock
    private BlogAssetStorageService blogAssetStorageService;

    @InjectMocks
    private BlogService blogService;

    @Test
    void listarBlogs_retornaListaDeBlogDTO() {
        Blog blog = Blog.builder()
                .id(1L)
                .titulo("Test Blog")
                .descripcionCorta("Short description")
                .fechaPublicacion(LocalDate.now())
                .build();

        when(blogRepository.findAll()).thenReturn(Collections.singletonList(blog));

        List<BlogDTO> result = blogService.listarBlogs();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(blog.getTitulo(), result.get(0).getTitulo());
    }

    @Test
    void buscarPorId_blogExistente_retornaOptionalConBlog() {
        Blog blog = Blog.builder().id(1L).build();
        when(blogRepository.findById(1L)).thenReturn(Optional.of(blog));

        Optional<Blog> result = blogService.buscarPorId(1L);

        assertTrue(result.isPresent());
        assertEquals(blog, result.get());
    }

    @Test
    void buscarPorId_blogNoExistente_retornaOptionalVacio() {
        when(blogRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Blog> result = blogService.buscarPorId(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void crearBlogDebeSubirMarkdownEImagenCuandoSeEntreganArchivos() throws Exception {
        Blog blog = Blog.builder()
                .titulo("Titulo")
                .autor("Autor")
                .fechaPublicacion(LocalDate.now())
                .descripcionCorta("Desc")
                .build();

        MultipartFile imagen = mock(MultipartFile.class);
        MultipartFile markdown = mock(MultipartFile.class);

        when(imagen.isEmpty()).thenReturn(false);
        when(markdown.isEmpty()).thenReturn(false);
        when(blogRepository.save(any(Blog.class))).thenAnswer(invocation -> {
            Blog entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(5L);
            }
            return entity;
        });
        when(blogAssetStorageService.storeMarkdown(eq(5L), eq(markdown))).thenReturn("markdown-url");
        when(blogAssetStorageService.storeImage(eq(5L), eq(imagen))).thenReturn("image-url");

        BlogDTO dto = blogService.crearBlog(blog, imagen, markdown);

        assertEquals("markdown-url", dto.getContenidoUrl());
        assertEquals("image-url", dto.getImagenUrl());
        verify(blogAssetStorageService).storeMarkdown(5L, markdown);
        verify(blogAssetStorageService).storeImage(5L, imagen);
    }

    @Test
    void crearBlogSinArchivosConservaLasRutasExistentes() throws Exception {
        Blog blog = Blog.builder()
                .titulo("Post existente")
                .autor("Autor")
                .fechaPublicacion(LocalDate.now())
                .descripcionCorta("Desc")
                .contenidoUrl("/contenido/actual.md")
                .imagenUrl("/imagenes/actual.jpg")
                .build();

        when(blogRepository.save(any(Blog.class))).thenAnswer(invocation -> {
            Blog entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(7L);
            }
            return entity;
        });

        BlogDTO dto = blogService.crearBlog(blog, null, null);

        assertEquals("/contenido/actual.md", dto.getContenidoUrl());
        assertEquals("/imagenes/actual.jpg", dto.getImagenUrl());
        verify(blogAssetStorageService, never()).storeMarkdown(eq(7L), any());
        verify(blogAssetStorageService, never()).storeImage(eq(7L), any());
    }

    @Test
    void listarAssetsUsaLaCarpetaPorBlog() throws IOException {
        Blog blog = Blog.builder().id(9L).build();
        when(blogRepository.findById(9L)).thenReturn(Optional.of(blog));
        when(storageService.listPublicUrls("blogs/9")).thenReturn(List.of("asset-a", "asset-b"));

        Optional<List<String>> assets = blogService.listarAssets(9L);

        assertTrue(assets.isPresent());
        assertEquals(2, assets.get().size());
        verify(storageService).listPublicUrls("blogs/9");
    }

    @Test
    void listarAssetsRetornaVacioCuandoNoExisteElBlog() {
        when(blogRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<List<String>> assets = blogService.listarAssets(99L);

        assertFalse(assets.isPresent());
    }
}
