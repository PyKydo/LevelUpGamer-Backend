package com.levelupgamer.content;

import com.levelupgamer.content.dto.BlogDTO;
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
                .map(blog -> BlogDTO.builder()
                        .id(blog.getId())
                        .titulo(blog.getTitulo())
                        .imagenUrl(blog.getImagenUrl())
                        .descripcionCorta(blog.getDescripcionCorta())
                        .descripcionLarga(blog.getDescripcionLarga())
                        .fechaPublicacion(blog.getFechaPublicacion())
                        .build())
                .collect(Collectors.toList());
    }

    public Optional<Blog> buscarPorId(Long id) {
        return blogRepository.findById(id);
    }
}
