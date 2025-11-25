package com.levelupgamer.autenticacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "La contraseña actual es obligatoria")
    @Size(min = 8, max = 32, message = "La contraseña actual debe tener entre 8 y 32 caracteres")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 32, message = "La nueva contraseña debe tener entre 8 y 32 caracteres")
    private String newPassword;
}
