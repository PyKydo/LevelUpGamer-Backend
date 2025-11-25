package com.levelupgamer.boletas.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoletaDetalleCrearDTO {
    @NotNull
    private Long productoId;
    @NotNull
    @Min(1)
    private Integer cantidad;
}