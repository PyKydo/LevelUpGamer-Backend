package com.levelupgamer.boletas;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Optional<Carrito> findByUsuarioId(Long userId);
}
