package com.levelupgamer.content.dto;

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
    private String imagenUrl;
    private String descripcionCorta;
    private String descripcionLarga;
    private LocalDate fechaPublicacion;
}

