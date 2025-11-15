package com.levelupgamer.contenido.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogDTO {
    private Long id;
    private String titulo;
    private String autor;
    private LocalDate fechaPublicacion;
    private String descripcionCorta;
    private String contenidoUrl;
    private String imagenUrl;
    private String altImagen;
}
