package com.levelupgamer.usuarios;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByRun(String run);
    boolean existsByCorreo(String correo);
    boolean existsByRun(String run);
    Optional<Usuario> findByCodigoReferido(String codigoReferido);
    Optional<Usuario> findFirstByRolesContaining(RolUsuario rol);
}

