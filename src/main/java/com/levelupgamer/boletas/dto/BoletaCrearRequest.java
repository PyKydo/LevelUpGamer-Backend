package com.levelupgamer.boletas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoletaCrearRequest {
    @NotNull
    @JsonProperty("cliente")
    private Long clienteId;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal total;

    @NotEmpty
    @Valid
    @JsonProperty("detalles")
    private List<BoletaDetalleRequest> detalles;
}
