package com.levelupgamer.usuarios;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.levelupgamer.boletas.Boleta;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    @NotNull
    @Size(min = 7, max = 10)
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
    private String contrasena;

    @Column(nullable = false)
    @NotNull
    private LocalDate fechaNacimiento;

    @ElementCollection(targetClass = RolUsuario.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Set<RolUsuario> roles;

    @Column(length = 100)
    @Size(max = 100)
    private String region;

    @Column(length = 100)
    @Size(max = 100)
    private String comuna;

    @Column(length = 300)
    @Size(max = 300)
    private String direccion;

    private String avatarUrl;

    private String codigoReferido;

    @Builder.Default
    private Boolean isDuocUser = false;

    @OneToMany(mappedBy = "usuario")
    private Set<Boleta> boletas;

    
    @Builder.Default
    private Boolean activo = true;

    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
