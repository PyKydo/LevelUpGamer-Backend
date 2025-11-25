package com.levelupgamer.boletas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {
    List<Boleta> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndDetallesProductoId(Long usuarioId, Long productoId);
}
