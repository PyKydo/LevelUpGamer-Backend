package com.levelupgamer.gamificacion.cupones;

import com.levelupgamer.gamificacion.cupones.dto.CuponDTO;
import com.levelupgamer.gamificacion.cupones.dto.RedeemCouponRequest;
import com.levelupgamer.gamificacion.dto.PuntosDTO;
import com.levelupgamer.gamificacion.mapper.CuponMapper;
import com.levelupgamer.gamificacion.PuntosService;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class CuponService {
    private static final SecureRandom RANDOM = new SecureRandom();
        private static final Map<Integer, Integer> CONVERSION = new TreeMap<>(Map.of(
            500, 5,
            1000, 10,
            1500, 15,
            2000, 20,
            2500, 25,
            3000, 30
        ));

    private final CuponRepository cuponRepository;
    private final UsuarioRepository usuarioRepository;
    private final PuntosService puntosService;

    @Transactional
    public CuponDTO canjearPorCupon(RedeemCouponRequest request) {
        Integer puntos = Objects.requireNonNull(request.puntosAGastar(), "puntos a gastar es requerido");
        if (!CONVERSION.containsKey(puntos)) {
            throw new IllegalArgumentException("Monto de puntos inválido para canje de cupón");
        }

        Long usuarioId = Objects.requireNonNull(request.usuarioId(), "usuarioId es requerido");
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        puntosService.canjearPuntos(new PuntosDTO(usuario.getId(), puntos));

        Cupon cupon = new Cupon();
        cupon.setCodigo(generarCodigoUnico());
        cupon.setUsuario(usuario);
        cupon.setEstado(EstadoCupon.ACTIVO);
        cupon.setPorcentajeDescuento(CONVERSION.get(puntos));

        return CuponMapper.toDTO(cuponRepository.save(cupon));
    }

    @Transactional(readOnly = true)
    public List<CuponDTO> listarCuponesActivosPorUsuario(Long usuarioId) {
        Long safeUsuarioId = Objects.requireNonNull(usuarioId, "usuarioId no puede ser nulo");
        return cuponRepository.findByUsuarioIdAndEstado(safeUsuarioId, EstadoCupon.ACTIVO)
                .stream()
                .map(CuponMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<Integer, Integer> obtenerTablaConversion() {
        return Collections.unmodifiableMap(CONVERSION);
    }

    @Transactional(readOnly = true)
    public Optional<Cupon> buscarCuponValido(Long usuarioId, Long cuponId, String codigo) {
        Long safeUsuarioId = Objects.requireNonNull(usuarioId, "usuarioId no puede ser nulo");
        if (cuponId != null) {
            return cuponRepository.findByIdAndUsuarioIdAndEstado(cuponId, safeUsuarioId, EstadoCupon.ACTIVO);
        }
        if (codigo != null && !codigo.isBlank()) {
            return cuponRepository.findByCodigoAndUsuarioIdAndEstado(codigo.trim(), safeUsuarioId, EstadoCupon.ACTIVO);
        }
        return Optional.empty();
    }

    @Transactional
    public void marcarComoUsado(Cupon cupon) {
        cupon.setEstado(EstadoCupon.USADO);
        cuponRepository.save(cupon);
    }

    @Transactional
    public void reactivarCupon(Cupon cupon) {
        if (cupon == null) {
            return;
        }
        cupon.setEstado(EstadoCupon.ACTIVO);
        cuponRepository.save(cupon);
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            codigo = String.format("CUP-%06d", RANDOM.nextInt(1_000_000));
        } while (cuponRepository.existsByCodigo(codigo));
        return codigo;
    }
}
