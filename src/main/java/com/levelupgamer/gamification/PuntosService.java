package com.levelupgamer.gamification;

import com.levelupgamer.gamification.dto.PuntosDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PuntosService {
    @Autowired
    private PuntosRepository puntosRepository;

    @Transactional(readOnly = true)
    public PuntosDTO obtenerPuntosPorUsuario(Long usuarioId) {
        return puntosRepository.findByUsuarioId(usuarioId)
                .map(p -> new PuntosDTO(p.getUsuarioId(), p.getPuntosAcumulados()))
                .orElse(new PuntosDTO(usuarioId, 0));
    }

    @Transactional
    public PuntosDTO sumarPuntos(PuntosDTO dto) {
        Puntos puntos = puntosRepository.findByUsuarioId(dto.getUsuarioId())
                .orElse(Puntos.builder().usuarioId(dto.getUsuarioId()).puntosAcumulados(0).build());
        puntos.setPuntosAcumulados(puntos.getPuntosAcumulados() + dto.getPuntosAcumulados());
        puntosRepository.save(puntos);
        return new PuntosDTO(puntos.getUsuarioId(), puntos.getPuntosAcumulados());
    }

    @Transactional
    public PuntosDTO canjearPuntos(PuntosDTO dto) {
        Puntos puntos = puntosRepository.findByUsuarioId(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario sin puntos"));
        if (puntos.getPuntosAcumulados() < dto.getPuntosAcumulados()) {
            throw new IllegalArgumentException("No tiene suficientes puntos para canjear");
        }
        puntos.setPuntosAcumulados(puntos.getPuntosAcumulados() - dto.getPuntosAcumulados());
        puntosRepository.save(puntos);
        return new PuntosDTO(puntos.getUsuarioId(), puntos.getPuntosAcumulados());
    }
}
