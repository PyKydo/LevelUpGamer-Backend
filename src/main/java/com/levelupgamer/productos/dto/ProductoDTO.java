package com.levelupgamer.productos.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private Integer stockCritico;
    private CategoriaDTO categoria;
    private Integer puntosLevelUp;
    private List<String> imagenes;
    private Boolean activo;
    private VendedorResumenDTO vendedor;
}
