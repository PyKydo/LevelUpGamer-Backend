package com.levelupgamer.autenticacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    @NotNull
    @Email
    private String correo;
    @NotNull
    @Size(min = 4, max = 10)
    private String contrasena;

    private com.levelupgamer.usuarios.RolUsuario rol;
}
