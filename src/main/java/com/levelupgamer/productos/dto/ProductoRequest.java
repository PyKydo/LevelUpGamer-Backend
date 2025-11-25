package com.levelupgamer.productos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ProductoRequest {
    private String codigo;

    @NotNull
    @Size(max = 100)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precio;

    @NotNull
    @Min(0)
    private Integer stock;

    @Min(0)
    private Integer stockCritico;

    @NotNull
    private Long categoriaId;

    @NotNull
    @Min(0)
    @Max(1000)
    private Integer puntosLevelUp;

    private List<String> imagenes;
}
