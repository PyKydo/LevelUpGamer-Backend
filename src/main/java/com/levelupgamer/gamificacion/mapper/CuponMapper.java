package com.levelupgamer.gamificacion.mapper;

import com.levelupgamer.gamificacion.cupones.Cupon;
import com.levelupgamer.gamificacion.cupones.dto.CuponDTO;

public final class CuponMapper {
    private CuponMapper() {}

    public static CuponDTO toDTO(Cupon cupon) {
        if (cupon == null) {
            return null;
        }
        return CuponDTO.builder()
                .id(cupon.getId())
                .codigo(cupon.getCodigo())
                .porcentajeDescuento(cupon.getPorcentajeDescuento())
                .estado(cupon.getEstado())
                .build();
    }
}
