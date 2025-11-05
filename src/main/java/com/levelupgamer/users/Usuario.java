package com.levelupgamer.users;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.levelupgamer.orders.Pedido;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 9)
    @NotNull
    @Size(min = 7, max = 9)
    private String run;

    @Column(nullable = false, length = 50)
    @NotNull
    @Size(max = 50)
    private String nombre;

    @Column(nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 100)
    @NotNull
    @Email
    @Size(max = 100)
    private String correo;

    @Column(nullable = false, length = 100)
    @NotNull
    @Size(min = 4, max = 10)
    private String contrasena;

    @Column(nullable = false)
    @NotNull
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    @Column(length = 100)
    @Size(max = 100)
    private String region;

    @Column(length = 100)
    @Size(max = 100)
    private String comuna;

    @Column(length = 300)
    @Size(max = 300)
    private String direccion;

    private Integer puntosLevelUp;
    private String codigoReferido;
    private Boolean isDuocUser = false;

    @OneToMany(mappedBy = "usuario")
    private Set<Pedido> pedidos;

    // Soft delete
    private Boolean activo = true;

    // Auditoría
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Getters y setters omitidos por brevedad
}
