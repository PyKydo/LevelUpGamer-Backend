package com.levelupgamer.boletas;

import com.levelupgamer.boletas.dto.BoletaCrearDTO;
import com.levelupgamer.boletas.dto.BoletaCrearRequest;
import com.levelupgamer.boletas.dto.BoletaDetalleCrearDTO;
import com.levelupgamer.boletas.dto.BoletaRespuestaDTO;
import com.levelupgamer.gamificacion.PuntosService;
import com.levelupgamer.gamificacion.cupones.Cupon;
import com.levelupgamer.gamificacion.cupones.CuponService;
import com.levelupgamer.gamificacion.dto.PuntosDTO;
import com.levelupgamer.productos.Producto;
import com.levelupgamer.productos.ProductoRepository;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoletaService {
    private static final Logger logger = LoggerFactory.getLogger(BoletaService.class);
    private static final String DUOC_DOMAIN = "duoc.cl";
    private static final String PROFESOR_DUOC_DOMAIN = "profesor.duoc.cl";
    private final BoletaRepository boletaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PuntosService puntosService;
    private final CuponService cuponService;

        @Transactional
        public BoletaRespuestaDTO crearBoleta(BoletaCrearRequest request) {
        Objects.requireNonNull(request, "La boleta no puede ser nula");

        BoletaCrearDTO boletaDTO = BoletaCrearDTO.builder()
            .usuarioId(request.getClienteId())
            .detalles(Optional.ofNullable(request.getDetalles())
                .orElseThrow(() -> new IllegalArgumentException("La boleta debe incluir al menos un detalle"))
                .stream()
                .map(detalle -> BoletaDetalleCrearDTO.builder()
                    .productoId(detalle.getProductoId())
                    .cantidad(detalle.getCantidad())
                    .build())
                .collect(Collectors.toList()))
            .build();

        BoletaRespuestaDTO respuesta = crearBoletaInterna(boletaDTO);
        validarTotalDeclarado(request.getTotal(), respuesta.getTotal());
        return respuesta;
        }

    @Transactional
    public BoletaRespuestaDTO crearBoletaInterna(BoletaCrearDTO dto) {
        Objects.requireNonNull(dto, "La boleta no puede ser nula");
        Usuario usuario = obtenerUsuario(dto.getUsuarioId());
        List<BoletaDetalleCrearDTO> detallesSolicitados = Optional.ofNullable(dto.getDetalles())
            .filter(l -> !l.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException("La boleta debe incluir al menos un producto"));
        List<BoletaDetalle> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int puntosGanados = 0;

        for (BoletaDetalleCrearDTO detalleDTO : detallesSolicitados) {
            Producto producto = obtenerProducto(detalleDTO.getProductoId());
            validarStock(producto, detalleDTO.getCantidad());

            BigDecimal precioUnitario = calcularPrecioUnitario(producto, usuario);
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(detalleDTO.getCantidad()));

            BoletaDetalle detalle = crearDetalleBoleta(producto, detalleDTO, precioUnitario, subtotal);
            detalles.add(detalle);
            total = total.add(subtotal);
            int puntosProducto = producto.getPuntosLevelUp() != null ? producto.getPuntosLevelUp() : 0;
            puntosGanados += puntosProducto * detalleDTO.getCantidad();

            actualizarStock(producto, detalleDTO.getCantidad());
        }

        Cupon cuponAplicado = procesarCupon(dto, usuario);
        DescuentoContexto descuentos = calcularDescuentos(total, usuario, cuponAplicado);

        Boleta boleta = guardarBoleta(usuario, detalles, descuentos, cuponAplicado);
        procesarPuntos(usuario, puntosGanados);
        if (cuponAplicado != null) {
            cuponService.marcarComoUsado(cuponAplicado);
        }

        return BoletaMapper.toDTO(boleta);
    }

    @Transactional(readOnly = true)
    public List<BoletaRespuestaDTO> listarTodas() {
        return boletaRepository.findAll().stream()
                .map(BoletaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BoletaRespuestaDTO> listarBoletasPorUsuario(Long usuarioId) {
        Objects.requireNonNull(usuarioId, "El id de usuario no puede ser nulo");
        return boletaRepository.findByUsuarioId(usuarioId).stream()
                .map(BoletaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Boleta> buscarPorId(Long id) {
        Objects.requireNonNull(id, "El id de la boleta no puede ser nulo");
        return boletaRepository.findById(id);
    }

    @Transactional
    public BoletaRespuestaDTO actualizarEstado(Long boletaId, EstadoBoleta estadoSolicitado) {
        Objects.requireNonNull(boletaId, "El id de la boleta no puede ser nulo");
        Objects.requireNonNull(estadoSolicitado, "El estado solicitado no puede ser nulo");

        Boleta boleta = boletaRepository.findById(boletaId)
                .orElseThrow(() -> new IllegalArgumentException("Boleta no encontrada"));

        if (boleta.getEstado() == EstadoBoleta.CANCELADO && estadoSolicitado != EstadoBoleta.CANCELADO) {
            throw new IllegalStateException("No es posible reabrir una boleta cancelada");
        }

        if (estadoSolicitado == EstadoBoleta.CANCELADO && boleta.getEstado() != EstadoBoleta.CANCELADO) {
            revertirInventarioYBeneficios(boleta);
        }

        boleta.setEstado(estadoSolicitado);
        return BoletaMapper.toDTO(boletaRepository.save(boleta));
    }

    @Transactional
    public void eliminarBoleta(Long boletaId) {
        Objects.requireNonNull(boletaId, "El id de la boleta no puede ser nulo");
        Boleta boleta = boletaRepository.findById(boletaId)
                .orElseThrow(() -> new IllegalArgumentException("Boleta no encontrada"));

        if (boleta.getEstado() != EstadoBoleta.CANCELADO) {
            revertirInventarioYBeneficios(boleta);
        }

        boletaRepository.delete(boleta);
    }

    

    private Usuario obtenerUsuario(Long usuarioId) {
        Objects.requireNonNull(usuarioId, "El id de usuario no puede ser nulo");
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private Producto obtenerProducto(Long productoId) {
        Objects.requireNonNull(productoId, "El id de producto no puede ser nulo");
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    private void validarStock(Producto producto, int cantidad) {
        if (producto.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para producto: " + producto.getNombre());
        }
    }

    private BigDecimal calcularPrecioUnitario(Producto producto, Usuario usuario) {
        return producto.getPrecio();
    }

    private BoletaDetalle crearDetalleBoleta(Producto producto, BoletaDetalleCrearDTO detalleDTO, BigDecimal precioUnitario,
            BigDecimal subtotal) {
        BoletaDetalle detalle = new BoletaDetalle();
        detalle.setProducto(producto);
        detalle.setCantidad(detalleDTO.getCantidad());
        detalle.setPrecioUnitario(precioUnitario);
        detalle.setSubtotal(subtotal);
        return detalle;
    }

    private void actualizarStock(Producto producto, int cantidad) {
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);

        if (producto.getStockCritico() != null && producto.getStock() <= producto.getStockCritico()) {
            logger.warn("Alerta de stock crítico para el producto {}. Stock actual: {}, Stock crítico: {}",
                    producto.getNombre(), producto.getStock(), producto.getStockCritico());
        }
    }

    private Boleta guardarBoleta(Usuario usuario, List<BoletaDetalle> detalles, DescuentoContexto descuentos, Cupon cupon) {
        Boleta boleta = new Boleta();
        boleta.setUsuario(usuario);
        boleta.setDetalles(detalles);
        boleta.setTotalAntesDescuentos(descuentos.totalOriginal());
        boleta.setTotal(descuentos.totalFinal());
        boleta.setCupon(cupon);
        boleta.setDescuentoCuponAplicado(descuentos.descuentoCupon());
        boleta.setDescuentoDuocAplicado(descuentos.descuentoDuoc());
        boleta.setFecha(LocalDateTime.now());
        boleta.setEstado(EstadoBoleta.PENDIENTE);
        detalles.forEach(detalle -> detalle.setBoleta(boleta));
        return boletaRepository.save(boleta);
    }

    private void procesarPuntos(Usuario usuario, int puntosGanados) {
        if (puntosGanados > 0) {
            puntosService.sumarPuntos(new PuntosDTO(usuario.getId(), puntosGanados));
        }
    }

    private Cupon procesarCupon(BoletaCrearDTO dto, Usuario usuario) {
        if (dto.getCuponId() == null && dto.getCodigoCupon() == null) {
            return null;
        }
        return cuponService.buscarCuponValido(usuario.getId(), dto.getCuponId(), dto.getCodigoCupon())
                .orElseThrow(() -> new IllegalArgumentException("Cupón inválido o no disponible"));
    }

    private DescuentoContexto calcularDescuentos(BigDecimal total, Usuario usuario, Cupon cupon) {
        boolean isDuocUser = usuario.getCorreo().endsWith(DUOC_DOMAIN)
                || usuario.getCorreo().endsWith(PROFESOR_DUOC_DOMAIN);
        int descuentoDuoc = isDuocUser ? 20 : 0;
        int descuentoCupon = cupon != null ? cupon.getPorcentajeDescuento() : 0;

        int descuentoTotal = Math.min(descuentoDuoc + descuentoCupon, 90);
        BigDecimal factor = BigDecimal.valueOf(100 - descuentoTotal)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalConDescuento = total.multiply(factor).setScale(2, RoundingMode.HALF_UP);

        return new DescuentoContexto(total, totalConDescuento, descuentoDuoc, descuentoCupon);
    }

    private record DescuentoContexto(BigDecimal totalOriginal, BigDecimal totalFinal, Integer descuentoDuoc, Integer descuentoCupon) {}

    private void validarTotalDeclarado(BigDecimal totalDeclarado, BigDecimal totalCalculado) {
        if (totalDeclarado == null || totalCalculado == null) {
            return;
        }
        BigDecimal diferencia = totalDeclarado.subtract(totalCalculado).abs();
        if (diferencia.compareTo(new BigDecimal("0.01")) > 0) {
            throw new IllegalArgumentException("El total enviado no coincide con el calculado por el sistema");
        }
    }

    private void revertirInventarioYBeneficios(Boleta boleta) {
        List<BoletaDetalle> detalles = boleta.getDetalles();
        if (detalles != null) {
            for (BoletaDetalle detalle : detalles) {
                Producto producto = detalle.getProducto();
                if (producto == null) {
                    continue;
                }
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        int puntosRegistrados = calcularPuntosDesdeDetalles(boleta);
        if (puntosRegistrados > 0) {
            puntosService.restarPuntosPorAjuste(boleta.getUsuario().getId(), puntosRegistrados,
                    "Reverso boleta #" + boleta.getId());
        }

        if (boleta.getCupon() != null) {
            cuponService.reactivarCupon(boleta.getCupon());
        }
    }

    private int calcularPuntosDesdeDetalles(Boleta boleta) {
        List<BoletaDetalle> detalles = boleta.getDetalles();
        if (detalles == null) {
            return 0;
        }
        int total = 0;
        for (BoletaDetalle detalle : detalles) {
            Producto producto = detalle.getProducto();
            if (producto == null) {
                continue;
            }
            int puntosProducto = producto.getPuntosLevelUp() != null ? producto.getPuntosLevelUp() : 0;
            total += puntosProducto * detalle.getCantidad();
        }
        return total;
    }
}
