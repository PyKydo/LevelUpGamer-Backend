package com.levelupgamer.gamificacion.cupones.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RedeemCouponRequest(
        @NotNull Long usuarioId,
        @NotNull @Positive Integer puntosAGastar
) {}
