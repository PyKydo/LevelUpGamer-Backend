package com.levelupgamer.productos;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
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
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotNull
    private String codigo;

    @Column(nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String nombre;

    @Column(length = 500)
    @Size(max = 500)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precio;

    @Column(nullable = false)
    @NotNull
    @Min(0)
    private Integer stock;

    @Min(0)
    private Integer stockCritico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CategoriaProducto categoria;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "producto_imagenes", joinColumns = @JoinColumn(name = "producto_id"))
    @Column(name = "imagen_url")
    private List<String> imagenes;

    // Soft delete
    @Builder.Default
    private Boolean activo = true;

    // Auditoría
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}