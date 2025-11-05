package com.levelupgamer.orders.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoItemCrearDTO {
    @NotNull
    private Long productoId;
    @NotNull
    @Min(1)
    private Integer cantidad;
    // Getters y setters
    // ...
}
