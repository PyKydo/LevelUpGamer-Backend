package com.levelupgamer.gamificacion;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.levelupgamer.usuarios.Usuario;
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
public class Puntos {
    @Id
    private Long usuarioId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @NotNull
    @Min(0)
    private Integer puntosAcumulados;

    @Version
    private Integer version;

    // Relación con historial de canjes (por implementar)
    // @OneToMany(mappedBy = "puntos")
    // private List<MovimientoPuntos> historialCanjes;

    // Auditoría
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Getters y setters omitidos por brevedad
}
