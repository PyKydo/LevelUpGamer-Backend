package com.levelupgamer.productos;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Producto> findTop5ByActivoTrueOrderByPuntosLevelUpDesc();
    List<Producto> findAllByVendedorId(Long vendedorId);
}

