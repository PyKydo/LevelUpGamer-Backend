package com.levelupgamer.gamificacion.cupones.dto;

import com.levelupgamer.gamificacion.cupones.EstadoCupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuponDTO {
    private Long id;
    private String codigo;
    private Integer porcentajeDescuento;
    private EstadoCupon estado;
}
