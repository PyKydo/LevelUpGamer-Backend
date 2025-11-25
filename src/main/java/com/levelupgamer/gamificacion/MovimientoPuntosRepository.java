package com.levelupgamer.gamificacion;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoPuntosRepository extends JpaRepository<MovimientoPuntos, Long> {
    List<MovimientoPuntos> findByPuntosUsuarioId(Long usuarioId);
}
