package com.levelupgamer.orders.dto;

import jakarta.validation.constraints.*;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoCrearDTO {
    @NotNull
    private Long usuarioId;
    @NotNull
    private List<PedidoItemCrearDTO> items;
    // Getters y setters
    // ...
}
