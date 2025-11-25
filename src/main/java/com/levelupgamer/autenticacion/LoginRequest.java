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
    @Size(min = 8, max = 32, message = "La contraseña debe tener entre 8 y 32 caracteres")
    private String contrasena;

    private com.levelupgamer.usuarios.RolUsuario rol;
}
