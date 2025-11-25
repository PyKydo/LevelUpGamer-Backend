package com.levelupgamer.productos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaRequest {
    @NotBlank
    @Size(max = 50)
    private String codigo;

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @Size(max = 255)
    private String descripcion;

    private Boolean activo;
}
