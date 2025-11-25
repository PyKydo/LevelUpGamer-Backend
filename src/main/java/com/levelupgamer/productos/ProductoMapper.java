package com.levelupgamer.productos;

import com.levelupgamer.productos.categorias.CategoriaMapper;
import com.levelupgamer.productos.dto.ProductoDTO;
import com.levelupgamer.productos.dto.VendedorResumenDTO;
import com.levelupgamer.usuarios.RolUsuario;
import com.levelupgamer.usuarios.Usuario;
import java.util.Set;

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
        dto.setVendedor(buildVendedorDTO(p.getVendedor()));
        return dto;
    }

    private static VendedorResumenDTO buildVendedorDTO(Usuario vendedor) {
        if (vendedor == null) {
            return null;
        }

        Set<RolUsuario> roles = vendedor.getRoles();
        boolean corporativo = roles != null && roles.contains(RolUsuario.ADMINISTRADOR)
                && !roles.contains(RolUsuario.VENDEDOR);

        String nombreMostrado = corporativo ? "LevelUp" : vendedor.getNombre();

        return VendedorResumenDTO.builder()
                .id(vendedor.getId())
                .nombre(nombreMostrado)
                .correo(vendedor.getCorreo())
                .corporativo(corporativo)
                .build();
    }
}
