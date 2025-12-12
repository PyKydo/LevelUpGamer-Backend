package com.levelupgamer.contenido;

import com.levelupgamer.contenido.dto.BlogDTO;
import com.levelupgamer.common.storage.FileStorageService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/blog-posts")
public class BlogController {
    @Autowired
    private BlogService blogService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FileStorageService fileStorageService;

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

    @GetMapping("/{id}/assets")
    public ResponseEntity<List<String>> listarAssets(@PathVariable Long id) {
        return blogService.listarAssets(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<String> getBlogContent(@PathVariable Long id) {
        var opt = blogService.buscarPorId(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Blog blog = opt.get();
        String url = blog.getContenidoUrl();
        if (url == null || url.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            var managedContent = fileStorageService.readContentIfManaged(url);
            if (managedContent.isPresent()) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"));
                return new ResponseEntity<>(managedContent.get(), headers, HttpStatus.OK);
            }
        } catch (Exception ignored) {
            
        }

        
        try {
            ResponseEntity<byte[]> resp = restTemplate.getForEntity(url, byte[].class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                String markdown = new String(resp.getBody(), StandardCharsets.UTF_8);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"));
                return new ResponseEntity<>(markdown, headers, HttpStatus.OK);
            }
            return ResponseEntity.status(resp.getStatusCode()).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<BlogDTO> crearBlog(
            @RequestPart("blog") @jakarta.validation.Valid Blog blog,
            @RequestPart(value = "imagen", required = false) org.springframework.web.multipart.MultipartFile imagen,
            @RequestPart(value = "contenido", required = false) org.springframework.web.multipart.MultipartFile contenido)
            throws java.io.IOException {
        return ResponseEntity.ok(blogService.crearBlog(blog, imagen, contenido));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<BlogDTO> actualizarBlog(@PathVariable Long id, @RequestBody Blog blog) {
        return blogService.actualizarBlog(id, blog)
                .map(blogService::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarBlog(@PathVariable Long id) {
        if (blogService.eliminarBlog(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
