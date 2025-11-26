package com.levelupgamer.boletas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoletaUsuarioDTO {
    private Long id;
    private String nombre;
    private String apellidos;
    private String correo;
}
