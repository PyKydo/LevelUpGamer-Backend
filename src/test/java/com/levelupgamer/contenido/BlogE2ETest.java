package com.levelupgamer.contenido;

import com.levelupgamer.common.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BlogE2ETest {

    private MockMvc mockMvc;

    @Mock
    private BlogService blogService;

    @Mock
    private S3Service s3Service;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BlogController blogController;

    private Blog blog;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(blogController).build();

        blog = new Blog();
        blog.setId(1L);
        blog.setTitulo("Test Blog");
        blog.setDescripcionCorta("Short description");
        blog.setFechaPublicacion(LocalDate.now());
    }

    @Test
    void getBlogContent_fromS3_returnsMarkdown() throws Exception {
        String bucket = "my-bucket";
        String key = "blogs/1/blog.md";
        String s3Url = "https://" + bucket + ".s3.amazonaws.com/" + key;
        String markdown = "# Hola desde S3";

        blog.setContenidoUrl(s3Url);

        when(blogService.buscarPorId(1L)).thenReturn(Optional.of(blog));
        when(s3Service.getBucketName()).thenReturn(bucket);
        when(s3Service.getFileContent(key)).thenReturn(markdown);

        mockMvc.perform(get("/api/blog-posts/1/content"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/markdown; charset=UTF-8"))
                .andExpect(content().string(markdown));
    }

    @Test
    void getBlogContent_fromPublicUrl_returnsMarkdown() throws Exception {
        String publicUrl = "https://example.com/blogs/1/blog.md";
        String markdown = "# Hola desde HTTP";

        blog.setContenidoUrl(publicUrl);

        when(blogService.buscarPorId(1L)).thenReturn(Optional.of(blog));
        when(restTemplate.getForEntity(publicUrl, String.class))
                .thenReturn(new ResponseEntity<>(markdown, HttpStatus.OK));

        mockMvc.perform(get("/api/blog-posts/1/content"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/markdown; charset=UTF-8"))
                .andExpect(content().string(markdown));
    }

    @Test
    void getBlogContent_blogNotFound_returns404() throws Exception {
        when(blogService.buscarPorId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/blog-posts/1/content"))
                .andExpect(status().isNotFound());
    }
}

