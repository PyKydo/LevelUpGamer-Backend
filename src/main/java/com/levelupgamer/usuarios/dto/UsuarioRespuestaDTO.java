package com.levelupgamer.usuarios.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRespuestaDTO {
    private Long id;
    private String run;
    private String nombre;
    private String apellidos;
    private String correo;
    private String region;
    private String comuna;
    private String direccion;
    private String codigoReferido;
    private String rol;
    private LocalDate fechaNacimiento;
    private Integer puntosLevelUp;
}
