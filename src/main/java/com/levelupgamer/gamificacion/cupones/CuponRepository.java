package com.levelupgamer.gamificacion.cupones;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CuponRepository extends JpaRepository<Cupon, Long> {
    Optional<Cupon> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Cupon> findByUsuarioIdAndEstado(Long usuarioId, EstadoCupon estado);
    Optional<Cupon> findByIdAndUsuarioIdAndEstado(Long id, Long usuarioId, EstadoCupon estado);
    Optional<Cupon> findByCodigoAndUsuarioIdAndEstado(String codigo, Long usuarioId, EstadoCupon estado);
}
