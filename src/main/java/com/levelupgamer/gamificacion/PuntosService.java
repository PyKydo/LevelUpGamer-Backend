package com.levelupgamer.gamificacion;

import com.levelupgamer.gamificacion.dto.PuntosDTO;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PuntosService {
    
    private final PuntosRepository puntosRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public PuntosService(PuntosRepository puntosRepository, UsuarioRepository usuarioRepository) {
        this.puntosRepository = puntosRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public PuntosDTO obtenerPuntosPorUsuario(Long usuarioId) {
        return puntosRepository.findByUsuarioId(usuarioId)
                .map(p -> new PuntosDTO(p.getUsuarioId(), p.getPuntosAcumulados()))
                .orElse(new PuntosDTO(usuarioId, 0));
    }

    @Transactional
    public PuntosDTO sumarPuntos(PuntosDTO dto) {
        Puntos puntos = puntosRepository.findByUsuarioId(dto.getUsuarioId())
                .orElseGet(() -> {
                    // Crear entidad Puntos asociada a Usuario managed (MapsId requiere la referencia)
                    Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado para asignar puntos"));
                    Puntos nuevo = new Puntos();
                    nuevo.setUsuario(usuario);
                    // no setUsuarioId: dejar que @MapsId asigne el id cuando se persista
                    nuevo.setPuntosAcumulados(0);
                    return nuevo;
                });

        puntos.setPuntosAcumulados(puntos.getPuntosAcumulados() + dto.getPuntosAcumulados());
        Puntos savedPuntos = puntosRepository.save(puntos);
        
        return new PuntosDTO(savedPuntos.getUsuarioId(), savedPuntos.getPuntosAcumulados());
    }

    @Transactional
    public PuntosDTO canjearPuntos(PuntosDTO dto) {
        Puntos puntos = puntosRepository.findByUsuarioId(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario sin puntos para canjear"));
        
        if (puntos.getPuntosAcumulados() < dto.getPuntosAcumulados()) {
            throw new IllegalArgumentException("No tiene suficientes puntos para canjear");
        }
        
        puntos.setPuntosAcumulados(puntos.getPuntosAcumulados() - dto.getPuntosAcumulados());
        puntosRepository.save(puntos);
        return new PuntosDTO(puntos.getUsuarioId(), puntos.getPuntosAcumulados());
    }
}
