package com.levelupgamer.pedidos;

import com.levelupgamer.pedidos.dto.CarritoDto;
import com.levelupgamer.pedidos.dto.CarritoItemDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Component
public class CarritoMapper {

    public CarritoDto toDto(Carrito carrito) {
        if (carrito == null) {
            return null;
        }

        BigDecimal total = carrito.getItems().stream()
                .map(item -> item.getProducto().getPrecio().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CarritoDto(
                carrito.getId(),
                carrito.getItems().stream().map(this::toDto).collect(Collectors.toList()),
                total.doubleValue()
        );
    }

    public CarritoItemDto toDto(CarritoItem carritoItem) {
        if (carritoItem == null) {
            return null;
        }

        return new CarritoItemDto(
                carritoItem.getProducto().getId(),
                carritoItem.getProducto().getNombre(),
                carritoItem.getQuantity(),
                carritoItem.getProducto().getPrecio().doubleValue()
        );
    }
}
