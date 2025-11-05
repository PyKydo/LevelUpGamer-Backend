package com.levelupgamer.users.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Pattern;
import com.levelupgamer.validation.AllowedEmailDomain;
import com.levelupgamer.validation.Adult;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRegistroDTO {
    @NotNull
    @Size(min = 7, max = 9)
    @Pattern(regexp = "^[0-9Kk]+$", message = "RUN inválido (sólo números y dígito verificador K)")
    private String run;
    @NotNull
    @Size(max = 50)
    private String nombre;
    @NotNull
    @Size(max = 100)
    private String apellidos;
    @NotNull
    @Email
    @Size(max = 100)
    @AllowedEmailDomain
    private String correo;
    @NotNull
    @Size(min = 4, max = 10)
    private String contrasena;
    @NotNull
    @Adult
    private LocalDate fechaNacimiento;
    @NotNull
    private String region;
    @NotNull
    private String comuna;
    @NotNull
    private String direccion;
    private String codigoReferido;
    // Getters y setters
    // ...
}
