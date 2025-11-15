package com.levelupgamer.contenido.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactoDTO {
    private Long id;
    private String nombre;
    private String correo;
    private String comentario;
    private LocalDateTime fecha;
}

