package com.levelupgamer.gamificacion;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PuntosRepository extends JpaRepository<Puntos, Long> {
    Optional<Puntos> findByUsuarioId(Long usuarioId);
}

