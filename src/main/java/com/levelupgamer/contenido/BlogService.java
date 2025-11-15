package com.levelupgamer.contenido;

import com.levelupgamer.contenido.dto.BlogDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlogService {
    @Autowired
    private BlogRepository blogRepository;

    @Transactional(readOnly = true)
    public List<BlogDTO> listarBlogs() {
        return blogRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<Blog> buscarPorId(Long id) {
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
