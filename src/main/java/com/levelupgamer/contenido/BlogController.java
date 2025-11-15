package com.levelupgamer.contenido;

import com.levelupgamer.contenido.dto.BlogDTO;
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
                .map(blogService::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
