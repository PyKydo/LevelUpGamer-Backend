package com.levelupgamer.boletas.dto;

import com.levelupgamer.boletas.EstadoBoleta;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoletaActualizarEstadoRequest {
    @NotNull
    private EstadoBoleta estado;
}
