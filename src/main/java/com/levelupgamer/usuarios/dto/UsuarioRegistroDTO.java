package com.levelupgamer.usuarios.dto;

import com.levelupgamer.validation.AllowedEmailDomain;
import com.levelupgamer.validation.Rut;
import com.levelupgamer.validation.Adult;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRegistroDTO {

    @NotBlank(message = "El RUN no puede estar vacío")
    @Rut
    private String run;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
    private String nombre;

    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    @Size(max = 100, message = "Los apellidos no pueden tener más de 100 caracteres")
    private String apellidos;

    @NotBlank(message = "El correo no puede estar vacío")
    @Email(message = "El formato del correo no es válido")
    @Size(max = 100, message = "El correo no puede tener más de 100 caracteres")
    @AllowedEmailDomain(domains = {"gmail.com", "hotmail.com", "outlook.com", "yahoo.com", "duoc.cl", "profesor.duoc.cl"})
    private String correo;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, max = 32, message = "La contraseña debe tener entre 8 y 32 caracteres")
    private String contrasena;

    @Adult
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La región no puede estar vacía")
    @Size(max = 100)
    private String region;

    @NotBlank(message = "La comuna no puede estar vacía")
    @Size(max = 100)
    private String comuna;

    @NotBlank(message = "La dirección no puede estar vacía")
    @Size(max = 300)
    private String direccion;

    private String codigoReferido;
}
