package com.levelupgamer.usuarios;

import com.levelupgamer.exception.UserAlreadyExistsException;
import com.levelupgamer.gamificacion.Puntos;
import com.levelupgamer.gamificacion.PuntosRepository;
import com.levelupgamer.gamificacion.PuntosService;
import com.levelupgamer.gamificacion.dto.PuntosDTO;
import com.levelupgamer.usuarios.dto.UsuarioRegistroDTO;
import com.levelupgamer.usuarios.dto.UsuarioRespuestaDTO;
import com.levelupgamer.usuarios.dto.UsuarioUpdateDTO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {
    private static final int REFERRAL_POINTS = 100;
    private final UsuarioRepository usuarioRepository;
    private final PuntosRepository puntosRepository;
    private final PuntosService puntosService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PuntosRepository puntosRepository,
            PuntosService puntosService, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.puntosRepository = puntosRepository;
        this.puntosService = puntosService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioRespuestaDTO registrarUsuario(UsuarioRegistroDTO dto) {
        Objects.requireNonNull(dto, "Datos de registro no pueden ser nulos");

        
        if (dto.getCorreo() == null || dto.getCorreo().trim().isEmpty()) {
            throw new IllegalArgumentException("Correo es obligatorio");
        }
        String correo = dto.getCorreo().trim().toLowerCase();
        if (correo.length() > 254) {
            throw new IllegalArgumentException("Correo demasiado largo");
        }

        
        if (dto.getRun() == null || dto.getRun().trim().isEmpty()) {
            throw new IllegalArgumentException("RUN es obligatorio");
        }
        String normalizedRun = dto.getRun().toUpperCase().replace(".", "").replace("-", "").trim();
        if (normalizedRun.length() < 7 || normalizedRun.length() > 9) {
            throw new IllegalArgumentException("RUN inválido: debe tener entre 7 y 9 caracteres sin puntos ni guion");
        }

        
        if (dto.getContrasena() == null) {
            throw new IllegalArgumentException("Contraseña es obligatoria");
        }
        String contrasena = dto.getContrasena().trim();
        if (contrasena.length() < 4 || contrasena.length() > 10) {
            throw new IllegalArgumentException("Contraseña inválida: debe tener entre 4 y 10 caracteres");
        }

        
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new UserAlreadyExistsException("Correo ya registrado");
        }
        if (usuarioRepository.existsByRun(normalizedRun)) {
            throw new UserAlreadyExistsException("RUN ya registrado");
        }

        Usuario usuario = UsuarioMapper.toEntity(dto);
        usuario.setRun(normalizedRun);
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(contrasena));
        
        HashSet<RolUsuario> roles = new HashSet<>();
        roles.add(RolUsuario.CLIENTE);
        usuario.setRoles(roles);
        usuario.setActivo(true);
        if (correo.endsWith("@duoc.cl") || correo.endsWith("@profesor.duoc.cl") || correo.endsWith("@duocuc.cl")) {
            usuario.setIsDuocUser(true);
        }

        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        
        Puntos puntos = Puntos.builder()
                .usuario(nuevoUsuario)
                
                .puntosAcumulados(0)
                .build();
        puntosRepository.save(puntos);

        
        if (dto.getCodigoReferido() != null && !dto.getCodigoReferido().trim().isEmpty()) {
            usuarioRepository.findByCodigoReferido(dto.getCodigoReferido().trim())
                    .ifPresent(referrer -> puntosService.sumarPuntos(new PuntosDTO(referrer.getId(), REFERRAL_POINTS)));
        }

        return UsuarioMapper.toDTO(nuevoUsuario, 0);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional
    public UsuarioRespuestaDTO actualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (dto.getNombre() != null)
            usuario.setNombre(dto.getNombre());
        if (dto.getApellidos() != null)
            usuario.setApellidos(dto.getApellidos());
        if (dto.getRegion() != null)
            usuario.setRegion(dto.getRegion());
        if (dto.getComuna() != null)
            usuario.setComuna(dto.getComuna());
        if (dto.getDireccion() != null)
            usuario.setDireccion(dto.getDireccion());

        
        
        if (usuario.getRoles() != null && !(usuario.getRoles() instanceof java.util.HashSet)) {
            usuario.setRoles(new HashSet<>(usuario.getRoles()));
        }

        usuarioRepository.save(usuario);
        PuntosDTO puntosDTO = puntosService.obtenerPuntosPorUsuario(id);
        return UsuarioMapper.toDTO(usuario, puntosDTO.getPuntosAcumulados());
    }

    @Transactional(readOnly = true)
    public java.util.List<UsuarioRespuestaDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(u -> UsuarioMapper.toDTO(u, 0)) 
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioRespuestaDTO crearUsuarioAdmin(UsuarioRegistroDTO dto) {
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        if (usuarioRepository.existsByRun(dto.getRun())) {
            throw new IllegalArgumentException("El RUN ya está registrado");
        }

        Usuario usuario = UsuarioMapper.toEntity(dto);
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        usuario.setRoles(java.util.Set.of(RolUsuario.ADMINISTRADOR, RolUsuario.CLIENTE)); 
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);
        return UsuarioMapper.toDTO(guardado, 0);
    }

    @Transactional
    public boolean eliminarUsuario(Long id) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
    }
}
