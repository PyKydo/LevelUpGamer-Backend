package com.levelupgamer.productos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendedorResumenDTO {
    private Long id;
    private String nombre;
    private String correo;
    private boolean corporativo;
}
