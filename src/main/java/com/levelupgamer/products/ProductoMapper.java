package com.levelupgamer.products;

import com.levelupgamer.products.dto.ProductoDTO;

public class ProductoMapper {
    public static ProductoDTO toDTO(Producto p) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(p.getId());
        dto.setCodigo(p.getCodigo());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setPrecio(p.getPrecio());
        dto.setStock(p.getStock());
        dto.setStockCritico(p.getStockCritico());
        dto.setCategoria(p.getCategoria() != null ? p.getCategoria().name() : null);
        dto.setImagenUrl(p.getImagenUrl());
        dto.setActivo(p.getActivo());
        return dto;
    }
}

