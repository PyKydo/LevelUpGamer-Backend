package com.levelupgamer.boletas.dto;

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
public class BoletaRespuestaDTO {
    private Long id;
    private Long usuarioId;
    private BoletaUsuarioDTO usuario;
    private List<BoletaDetalleRespuestaDTO> detalles;
    private BigDecimal total;
    private BigDecimal totalAntesDescuentos;
    private Integer descuentoCupon;
    private Integer descuentoDuoc;
    private Long cuponId;
    private String cuponCodigo;
    private LocalDateTime fecha;
    private String estado;
}
