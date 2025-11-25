package com.levelupgamer.productos.categorias;

import com.levelupgamer.productos.dto.CategoriaDTO;

public final class CategoriaMapper {

    private CategoriaMapper() {
    }

    public static CategoriaDTO toDTO(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return CategoriaDTO.builder()
                .id(categoria.getId())
                .codigo(categoria.getCodigo())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .activo(categoria.getActivo())
                .build();
    }
}
