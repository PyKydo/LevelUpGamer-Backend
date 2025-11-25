package com.levelupgamer.pedidos.dto;

import jakarta.validation.constraints.*;
import java.util.List;
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
public class PedidoCrearDTO {
    @NotNull
    private Long usuarioId;
    @NotNull
    private List<PedidoItemCrearDTO> items;
    private Long cuponId;
    private String codigoCupon;
}