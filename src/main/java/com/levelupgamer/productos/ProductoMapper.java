package com.levelupgamer.productos;

import com.levelupgamer.productos.categorias.CategoriaMapper;
import com.levelupgamer.productos.dto.ProductoDTO;

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
        dto.setCategoria(CategoriaMapper.toDTO(p.getCategoria()));
        dto.setPuntosLevelUp(p.getPuntosLevelUp());
        dto.setImagenes(p.getImagenes());
        dto.setActivo(p.getActivo());
        return dto;
    }
}
