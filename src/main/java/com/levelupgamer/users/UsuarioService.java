package com.levelupgamer.users;

import com.levelupgamer.exception.UserAlreadyExistsException;
import com.levelupgamer.users.dto.UsuarioRegistroDTO;
import com.levelupgamer.users.dto.UsuarioRespuestaDTO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioService {
    private static final int REFERRAL_POINTS = 100;
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioRespuestaDTO registrarUsuario(UsuarioRegistroDTO dto) {
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new UserAlreadyExistsException("Correo ya registrado");
        }
        if (usuarioRepository.existsByRun(dto.getRun())) {
            throw new UserAlreadyExistsException("RUN ya registrado");
        }
        // Hashear contraseña
        Usuario usuario = UsuarioMapper.toEntity(dto);
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setPuntosLevelUp(0);
        usuario.setActivo(true);
        String correo = dto.getCorreo().toLowerCase();
        if (correo.endsWith("@duoc.cl") || correo.endsWith("@profesor.duoc.cl")) {
            usuario.setIsDuocUser(true);
        }

        // Asignar puntos al referido si existe
        if (dto.getCodigoReferido() != null && !dto.getCodigoReferido().isEmpty()) {
            usuarioRepository.findByCodigoReferido(dto.getCodigoReferido())
                .ifPresent(referrer -> {
                    referrer.setPuntosLevelUp(referrer.getPuntosLevelUp() + REFERRAL_POINTS);
                    usuarioRepository.save(referrer);
                });
        }

        usuarioRepository.save(usuario);
        return UsuarioMapper.toDTO(usuario);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public UsuarioRespuestaDTO actualizarUsuario(Long id, UsuarioRegistroDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setNombre(dto.getNombre());
        usuario.setApellidos(dto.getApellidos());
        usuario.setRegion(dto.getRegion());
        usuario.setComuna(dto.getComuna());
        usuario.setDireccion(dto.getDireccion());
        // No se permite cambiar run, correo ni fechaNacimiento ni rol por seguridad
        usuarioRepository.save(usuario);
        return UsuarioMapper.toDTO(usuario);
    }
}
