package com.levelupgamer.productos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResenaDTO {
    private Long id;

    @NotBlank
    @Size(max = 1000)
    private String texto;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer calificacion;

    private String nombreUsuario;

    @NotNull
    private Long productoId;

    private LocalDateTime createdAt;
}
