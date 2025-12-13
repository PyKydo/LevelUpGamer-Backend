package com.levelupgamer.usuarios.dto;

import com.levelupgamer.validation.AllowedEmailDomain;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDTO {

    @Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
    private String nombre;

    @Size(max = 100, message = "Los apellidos no pueden tener más de 100 caracteres")
    private String apellidos;

    @Email(message = "El formato del correo no es válido")
    @Size(max = 100, message = "El correo no puede tener más de 100 caracteres")
    @AllowedEmailDomain(domains = {"gmail.com", "duoc.cl", "profesor.duoc.cl", "duocuc.cl"})
    private String correo;

    @Size(max = 100)
    private String region;

    @Size(max = 100)
    private String comuna;

    @Size(max = 300)
    private String direccion;
}
