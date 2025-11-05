package com.levelupgamer.gamification.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntosDTO {
    private Long usuarioId;
    private Integer puntosAcumulados;
    // private List<MovimientoPuntosDTO> historialCanjes; // Para futuro
}

