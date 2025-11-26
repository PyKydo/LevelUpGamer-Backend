package com.levelupgamer.gamificacion;

import com.levelupgamer.gamificacion.dto.PuntosDTO;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PuntosService {
    
    private final PuntosRepository puntosRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoPuntosRepository movimientoPuntosRepository;

    public PuntosService(PuntosRepository puntosRepository,
                         UsuarioRepository usuarioRepository,
                         MovimientoPuntosRepository movimientoPuntosRepository) {
        this.puntosRepository = puntosRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimientoPuntosRepository = movimientoPuntosRepository;
    }

    @Transactional(readOnly = true)
    public PuntosDTO obtenerPuntosPorUsuario(Long usuarioId) {
        Long safeUsuarioId = Objects.requireNonNull(usuarioId, "usuarioId no puede ser nulo");
        return puntosRepository.findByUsuarioId(safeUsuarioId)
                .map(p -> new PuntosDTO(p.getUsuarioId(), p.getPuntosAcumulados()))
                .orElse(new PuntosDTO(safeUsuarioId, 0));
    }

    @Transactional
    public PuntosDTO sumarPuntos(PuntosDTO dto) {
        Long usuarioId = Objects.requireNonNull(dto.getUsuarioId(), "usuarioId es requerido");
        Integer puntosASumar = Objects.requireNonNull(dto.getPuntosAcumulados(), "puntosAcumulados es requerido");

        Puntos puntos = puntosRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    
                    Usuario usuario = usuarioRepository.findById(usuarioId)
                            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado para asignar puntos"));
                    Puntos nuevo = new Puntos();
                    nuevo.setUsuario(usuario);
                    
                    nuevo.setPuntosAcumulados(0);
                    return nuevo;
                });

        puntos.setPuntosAcumulados(puntos.getPuntosAcumulados() + puntosASumar);
        Puntos savedPuntos = puntosRepository.save(puntos);
        registrarMovimiento(savedPuntos, puntosASumar, TipoMovimientoPuntos.GANANCIA,
                "Suma directa de puntos");

        return new PuntosDTO(savedPuntos.getUsuarioId(), savedPuntos.getPuntosAcumulados());
    }

    @Transactional
    public PuntosDTO canjearPuntos(PuntosDTO dto) {
        Long usuarioId = Objects.requireNonNull(dto.getUsuarioId(), "usuarioId es requerido");
        Integer puntosACanjear = Objects.requireNonNull(dto.getPuntosAcumulados(), "puntosAcumulados es requerido");

        Puntos puntos = puntosRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario sin puntos para canjear"));
        
        if (puntos.getPuntosAcumulados() < puntosACanjear) {
            throw new IllegalArgumentException("No tiene suficientes puntos para canjear");
        }
        
        puntos.setPuntosAcumulados(puntos.getPuntosAcumulados() - puntosACanjear);
        Puntos saved = puntosRepository.save(puntos);
        registrarMovimiento(saved, puntosACanjear, TipoMovimientoPuntos.CANJE,
                "Canje manual de puntos");

        return new PuntosDTO(saved.getUsuarioId(), saved.getPuntosAcumulados());
    }

    @Transactional
    public PuntosDTO restarPuntosPorAjuste(Long usuarioId, Integer puntosAjustados, String descripcion) {
        Long safeUsuarioId = Objects.requireNonNull(usuarioId, "usuarioId es requerido");
        Integer safePuntos = Objects.requireNonNull(puntosAjustados, "puntosAjustados es requerido");

        Puntos puntos = puntosRepository.findByUsuarioId(safeUsuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario sin puntos registrados"));

        if (puntos.getPuntosAcumulados() < safePuntos) {
            throw new IllegalArgumentException("No tiene suficientes puntos para revertir la operación solicitada");
        }

        puntos.setPuntosAcumulados(puntos.getPuntosAcumulados() - safePuntos);
        Puntos saved = puntosRepository.save(puntos);
        registrarMovimiento(saved, safePuntos, TipoMovimientoPuntos.CANJE,
                descripcion != null ? descripcion : "Ajuste de puntos");

        return new PuntosDTO(saved.getUsuarioId(), saved.getPuntosAcumulados());
    }

    @SuppressWarnings("null")
    private void registrarMovimiento(Puntos puntos, Integer cantidad, TipoMovimientoPuntos tipo, String descripcion) {
        MovimientoPuntos movimiento = MovimientoPuntos.builder()
                .puntos(puntos)
                .puntosAfectados(cantidad)
                .tipo(tipo)
                .descripcion(descripcion)
                .build();
        MovimientoPuntos persisted = movimientoPuntosRepository.save(movimiento);
        puntos.agregarMovimiento(persisted);
    }
}
