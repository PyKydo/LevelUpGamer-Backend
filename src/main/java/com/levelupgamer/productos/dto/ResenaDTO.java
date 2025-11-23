package com.levelupgamer.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResenaDTO {
    private Long id;
    private String texto;
    private Integer calificacion;
    private String nombreUsuario;
    private Long productoId;
    private LocalDateTime createdAt;
}
