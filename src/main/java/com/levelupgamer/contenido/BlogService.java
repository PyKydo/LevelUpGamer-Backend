package com.levelupgamer.contenido;

import com.levelupgamer.contenido.dto.BlogDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlogService {
    private static final String BLOG_IMAGE_FOLDER = "blogs";
    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private com.levelupgamer.common.storage.FileStorageService storageService;

    @Transactional(readOnly = true)
    public List<BlogDTO> listarBlogs() {
        return blogRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BlogDTO crearBlog(Blog blog, org.springframework.web.multipart.MultipartFile imagen)
            throws java.io.IOException {
        if (imagen != null && !imagen.isEmpty()) {
                String imageUrl = storageService.uploadFile(
                    imagen.getInputStream(),
                    imagen.getOriginalFilename(),
                    imagen.getSize(),
                    BLOG_IMAGE_FOLDER,
                    imagen.getContentType());
            blog.setImagenUrl(imageUrl);
        }
        blog.setFechaPublicacion(java.time.LocalDate.now());
        Blog guardado = blogRepository.save(blog);
        return toDTO(guardado);
    }

    @Transactional
    public Optional<Blog> actualizarBlog(Long id, Blog nuevo) {
        Objects.requireNonNull(id, "id es obligatorio");
        return blogRepository.findById(id).map(blog -> {
            blog.setTitulo(nuevo.getTitulo());
            blog.setAutor(nuevo.getAutor());
            blog.setDescripcionCorta(nuevo.getDescripcionCorta());
            blog.setContenidoUrl(nuevo.getContenidoUrl());
            blog.setAltImagen(nuevo.getAltImagen());
            
            blogRepository.save(blog);
            return blog;
        });
    }

    @Transactional
    public boolean eliminarBlog(Long id) {
        Objects.requireNonNull(id, "id es obligatorio");
        if (blogRepository.existsById(id)) {
            blogRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Blog> buscarPorId(Long id) {
        Objects.requireNonNull(id, "id es obligatorio");
        return blogRepository.findById(id);
    }

    public BlogDTO toDTO(Blog blog) {
        return BlogDTO.builder()
                .id(blog.getId())
                .titulo(blog.getTitulo())
                .autor(blog.getAutor())
                .fechaPublicacion(blog.getFechaPublicacion())
                .descripcionCorta(blog.getDescripcionCorta())
                .contenidoUrl(blog.getContenidoUrl())
                .imagenUrl(blog.getImagenUrl())
                .altImagen(blog.getAltImagen())
                .build();
    }
}
