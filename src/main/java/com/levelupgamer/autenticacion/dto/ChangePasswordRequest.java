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
    @Size(min = 4, max = 10, message = "La contraseña actual debe tener entre 4 y 10 caracteres")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 4, max = 10, message = "La nueva contraseña debe tener entre 4 y 10 caracteres")
    private String newPassword;
}
