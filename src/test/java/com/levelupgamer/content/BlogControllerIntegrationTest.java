package com.levelupgamer.content;

import com.levelupgamer.content.Blog;
import com.levelupgamer.content.BlogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BlogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlogRepository blogRepository;

    @BeforeEach
    void setup() {
        blogRepository.deleteAll();
    }

    @Test
    void listarBlogs_debeRetornarBlogsExistentes() throws Exception {
        Blog blog = Blog.builder()
                .titulo("Primer Post")
                .imagenUrl("https://example.com/img1.png")
                .descripcionCorta("Desc corta")
                .descripcionLarga("Desc larga del blog")
                .fechaPublicacion(LocalDate.now())
                .build();
        blogRepository.save(blog);

        mockMvc.perform(get("/api/blog-posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo", is("Primer Post")))
                .andExpect(jsonPath("$[0].imagenUrl", is("https://example.com/img1.png")))
                .andExpect(jsonPath("$[0].descripcionCorta", is("Desc corta")))
                .andExpect(jsonPath("$[0].descripcionLarga", is("Desc larga del blog")));
    }
}

