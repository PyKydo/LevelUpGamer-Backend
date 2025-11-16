package com.levelupgamer.pedidos.dto;

import java.util.List;

public class CarritoDto {
    private Long id;
    private List<CarritoItemDto> items;
    private double total;

    public CarritoDto() {
    }

    public CarritoDto(Long id, List<CarritoItemDto> items, double total) {
        this.id = id;
        this.items = items;
        this.total = total;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CarritoItemDto> getItems() {
        return items;
    }

    public void setItems(List<CarritoItemDto> items) {
        this.items = items;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
