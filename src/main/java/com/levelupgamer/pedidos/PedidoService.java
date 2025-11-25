package com.levelupgamer.pedidos;

import com.levelupgamer.gamificacion.PuntosService;
import com.levelupgamer.gamificacion.cupones.Cupon;
import com.levelupgamer.gamificacion.cupones.CuponService;
import com.levelupgamer.gamificacion.dto.PuntosDTO;
import com.levelupgamer.pedidos.dto.PedidoCrearDTO;
import com.levelupgamer.pedidos.dto.PedidoItemCrearDTO;
import com.levelupgamer.pedidos.dto.PedidoRespuestaDTO;
import com.levelupgamer.productos.Producto;
import com.levelupgamer.productos.ProductoRepository;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);
    private static final String DUOC_DOMAIN = "duoc.cl";
    private static final String PROFESOR_DUOC_DOMAIN = "profesor.duoc.cl";
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PuntosService puntosService;
    private final CuponService cuponService;

    /**
     * Crea un nuevo pedido para un usuario.
     *
     * @param dto DTO con los datos del pedido.
     * @return DTO con la respuesta del pedido creado.
     */
    @Transactional
    public PedidoRespuestaDTO crearPedido(PedidoCrearDTO dto) {
        Objects.requireNonNull(dto, "El pedido no puede ser nulo");
        Usuario usuario = obtenerUsuario(dto.getUsuarioId());
        List<PedidoItemCrearDTO> itemsSolicitados = Optional.ofNullable(dto.getItems())
            .filter(l -> !l.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException("El pedido debe incluir al menos un producto"));
        List<PedidoItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int puntosGanados = 0;

        for (PedidoItemCrearDTO itemDTO : itemsSolicitados) {
            Producto producto = obtenerProducto(itemDTO.getProductoId());
            validarStock(producto, itemDTO.getCantidad());

            BigDecimal precioUnitario = calcularPrecioUnitario(producto, usuario);
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(itemDTO.getCantidad()));

            PedidoItem item = crearItemPedido(producto, itemDTO, precioUnitario, subtotal);
            items.add(item);
            total = total.add(subtotal);
            int puntosProducto = producto.getPuntosLevelUp() != null ? producto.getPuntosLevelUp() : 0;
            puntosGanados += puntosProducto * itemDTO.getCantidad();

            actualizarStock(producto, itemDTO.getCantidad());
        }

        Cupon cuponAplicado = procesarCupon(dto, usuario);
        DescuentoContexto descuentos = calcularDescuentos(total, usuario, cuponAplicado);

        Pedido pedido = guardarPedido(usuario, items, descuentos, cuponAplicado);
        procesarPuntos(usuario, puntosGanados);
        if (cuponAplicado != null) {
            cuponService.marcarComoUsado(cuponAplicado);
        }

        return PedidoMapper.toDTO(pedido);
    }

    /**
     * Lista los pedidos de un usuario específico.
     *
     * @param usuarioId ID del usuario.
     * @return Lista de pedidos del usuario.
     */
    @Transactional(readOnly = true)
    public List<PedidoRespuestaDTO> listarPedidosPorUsuario(Long usuarioId) {
        Objects.requireNonNull(usuarioId, "El id de usuario no puede ser nulo");
        return pedidoRepository.findByUsuarioId(usuarioId).stream()
                .map(PedidoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca un pedido por su ID.
     *
     * @param id ID del pedido.
     * @return Optional con el pedido si existe.
     */
    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(Long id) {
        Objects.requireNonNull(id, "El id de pedido no puede ser nulo");
        return pedidoRepository.findById(id);
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

    private PedidoItem crearItemPedido(Producto producto, PedidoItemCrearDTO itemDTO, BigDecimal precioUnitario,
            BigDecimal subtotal) {
        PedidoItem item = new PedidoItem();
        item.setProducto(producto);
        item.setCantidad(itemDTO.getCantidad());
        item.setPrecioUnitario(precioUnitario);
        item.setSubtotal(subtotal);
        return item;
    }

    private void actualizarStock(Producto producto, int cantidad) {
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);

        if (producto.getStockCritico() != null && producto.getStock() <= producto.getStockCritico()) {
            logger.warn("Alerta de stock crítico para el producto {}. Stock actual: {}, Stock crítico: {}",
                    producto.getNombre(), producto.getStock(), producto.getStockCritico());
        }
    }

    private Pedido guardarPedido(Usuario usuario, List<PedidoItem> items, DescuentoContexto descuentos, Cupon cupon) {
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setItems(items);
        pedido.setTotalAntesDescuentos(descuentos.totalOriginal());
        pedido.setTotal(descuentos.totalFinal());
        pedido.setCupon(cupon);
        pedido.setDescuentoCuponAplicado(descuentos.descuentoCupon());
        pedido.setDescuentoDuocAplicado(descuentos.descuentoDuoc());
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        items.forEach(item -> item.setPedido(pedido));
        return pedidoRepository.save(pedido);
    }

    private void procesarPuntos(Usuario usuario, int puntosGanados) {
        if (puntosGanados > 0) {
            puntosService.sumarPuntos(new PuntosDTO(usuario.getId(), puntosGanados));
        }
    }

    private Cupon procesarCupon(PedidoCrearDTO dto, Usuario usuario) {
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
}
