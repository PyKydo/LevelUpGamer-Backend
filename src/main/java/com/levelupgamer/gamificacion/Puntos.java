package com.levelupgamer.gamificacion;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.levelupgamer.usuarios.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "historialCanjes")
@EqualsAndHashCode(exclude = "historialCanjes")
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

    @OneToMany(mappedBy = "puntos", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MovimientoPuntos> historialCanjes = new ArrayList<>();

    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void agregarMovimiento(MovimientoPuntos movimiento) {
        movimiento.setPuntos(this);
        this.historialCanjes.add(movimiento);
    }
}
