package com.levelupgamer.contenido;

import com.levelupgamer.contenido.dto.BlogDTO;
import com.levelupgamer.common.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/blog-posts")
public class BlogController {
    @Autowired
    private BlogService blogService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private S3Service s3Service;

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

        // Si la URL corresponde al bucket S3 configurado, obtener directamente desde S3
        try {
            String bucket = s3Service.getBucketName();
            String key = null;
            URI uri = URI.create(url);
            String host = uri.getHost();
            String path = uri.getPath();

            if (host != null && host.contains(bucket)) {
                // formato: <bucket>.s3.amazonaws.com/<key>
                if (path != null && path.startsWith("/")) {
                    key = path.substring(1);
                } else {
                    key = path;
                }
            } else if (host != null && host.contains("s3.amazonaws.com")) {
                // formato: s3.amazonaws.com/<bucket>/<key>
                // path = /<bucket>/<key>
                if (path != null && path.startsWith("/")) {
                    String p = path.substring(1);
                    if (p.startsWith(bucket + "/")) {
                        key = p.substring(bucket.length() + 1);
                    }
                }
            }

            if (key != null) {
                String content = s3Service.getFileContent(key);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"));
                return new ResponseEntity<>(content, headers, HttpStatus.OK);
            }
        } catch (Exception ignored) {
            // Si falla leer desde S3, intentaremos vía HTTP externo
        }

        // Si no es S3 o falla, caer a RestTemplate para URL pública
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"));
                return new ResponseEntity<>(resp.getBody(), headers, HttpStatus.OK);
            } else {
                return ResponseEntity.status(resp.getStatusCode()).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}
