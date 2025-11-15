package com.levelupgamer.autenticacion;

import com.levelupgamer.usuarios.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private Set<RolUsuario> roles;
    private Long usuarioId;
}
