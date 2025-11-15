package com.levelupgamer.pedidos;

import com.levelupgamer.pedidos.dto.*;
import java.util.List;
import java.util.stream.Collectors;

public class PedidoMapper {
    public static PedidoRespuestaDTO toDTO(Pedido pedido) {
        PedidoRespuestaDTO dto = new PedidoRespuestaDTO();
        dto.setId(pedido.getId());
        dto.setUsuarioId(pedido.getUsuario() != null ? pedido.getUsuario().getId() : null);
        dto.setItems(toItemDTOList(pedido.getItems()));
        dto.setTotal(pedido.getTotal());
        dto.setFecha(pedido.getFecha());
        dto.setEstado(pedido.getEstado() != null ? pedido.getEstado().name() : null);
        return dto;
    }
    public static List<PedidoItemRespuestaDTO> toItemDTOList(List<PedidoItem> items) {
        if (items == null) return null;
        return items.stream().map(PedidoMapper::toItemDTO).collect(Collectors.toList());
    }
    public static PedidoItemRespuestaDTO toItemDTO(PedidoItem item) {
        PedidoItemRespuestaDTO dto = new PedidoItemRespuestaDTO();
        dto.setProductoId(item.getProducto() != null ? item.getProducto().getId() : null);
        dto.setNombreProducto(item.getProducto() != null ? item.getProducto().getNombre() : null);
        dto.setCantidad(item.getCantidad());
        dto.setPrecioUnitario(item.getPrecioUnitario());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}

