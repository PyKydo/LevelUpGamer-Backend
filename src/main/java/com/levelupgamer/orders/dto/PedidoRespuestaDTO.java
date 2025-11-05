package com.levelupgamer.orders.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoRespuestaDTO {
    private Long id;
    private Long usuarioId;
    private List<PedidoItemRespuestaDTO> items;
    private BigDecimal total;
    private LocalDateTime fecha;
    private String estado;
    // Getters y setters
    // ...
}
