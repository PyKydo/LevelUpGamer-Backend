package com.levelupgamer.contenido;

import com.levelupgamer.contenido.dto.BlogDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogService {
    private static final String BLOG_IMAGE_FOLDER = "blogs";

    private final BlogRepository blogRepository;
    private final com.levelupgamer.common.storage.FileStorageService storageService;
    private final BlogAssetStorageService blogAssetStorageService;

    @Transactional(readOnly = true)
    public List<BlogDTO> listarBlogs() {
        return blogRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BlogDTO crearBlog(
            Blog blog,
            org.springframework.web.multipart.MultipartFile imagen,
            org.springframework.web.multipart.MultipartFile contenido) throws java.io.IOException {

        blog.setFechaPublicacion(java.time.LocalDate.now());
        Blog guardado = blogRepository.save(blog);

        if (contenido != null && !contenido.isEmpty()) {
            String contenidoUrl = blogAssetStorageService.storeMarkdown(guardado.getId(), contenido);
            guardado.setContenidoUrl(contenidoUrl);
        }

        if (imagen != null && !imagen.isEmpty()) {
            String imageUrl = blogAssetStorageService.storeImage(guardado.getId(), imagen);
            guardado.setImagenUrl(imageUrl);
        }

        Blog actualizado = blogRepository.save(guardado);
        return toDTO(actualizado);
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

    @Transactional(readOnly = true)
    public Optional<List<String>> listarAssets(Long id) {
        Objects.requireNonNull(id, "id es obligatorio");
        return blogRepository.findById(id)
                .map(blog -> fetchAssetsForBlog(blog.getId()));
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

    private List<String> fetchAssetsForBlog(Long blogId) {
        try {
            return storageService.listPublicUrls(BLOG_IMAGE_FOLDER + "/" + blogId);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron listar los assets del blog " + blogId, e);
        }
    }
}
