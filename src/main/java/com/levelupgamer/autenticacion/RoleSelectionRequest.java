package com.levelupgamer.autenticacion;

import com.levelupgamer.usuarios.RolUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleSelectionRequest {
    @NotBlank
    private String preAuthToken;

    @NotNull
    private RolUsuario selectedRole;
}
