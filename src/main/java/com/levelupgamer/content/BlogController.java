package com.levelupgamer.content;

import com.levelupgamer.content.dto.BlogDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/blog-posts")
public class BlogController {
    @Autowired
    private BlogService blogService;

    @GetMapping
    public ResponseEntity<List<BlogDTO>> listarBlogs() {
        return ResponseEntity.ok(blogService.listarBlogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogDTO> getBlog(@PathVariable Long id) {
        return blogService.buscarPorId(id)
                .map(blog -> BlogDTO.builder()
                        .id(blog.getId())
                        .titulo(blog.getTitulo())
                        .imagenUrl(blog.getImagenUrl())
                        .descripcionCorta(blog.getDescripcionCorta())
                        .descripcionLarga(blog.getDescripcionLarga())
                        .fechaPublicacion(blog.getFechaPublicacion())
                        .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
