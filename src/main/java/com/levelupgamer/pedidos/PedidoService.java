package com.levelupgamer.pedidos;

import com.levelupgamer.gamificacion.PuntosService;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);
    private static final int POINTS_PER_TEN_UNITS = 1;
    private static final String DUOC_DOMAIN = "duoc.cl";
    private static final String PROFESOR_DUOC_DOMAIN = "profesor.duoc.cl";
    private static final BigDecimal DESCUENTO_DUOC = new BigDecimal("0.8");
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PuntosService puntosService;

    /**
     * Crea un nuevo pedido para un usuario.
     *
     * @param dto DTO con los datos del pedido.
     * @return DTO con la respuesta del pedido creado.
     */
    @Transactional
    public PedidoRespuestaDTO crearPedido(PedidoCrearDTO dto) {
        Usuario usuario = obtenerUsuario(dto.getUsuarioId());
        List<PedidoItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (PedidoItemCrearDTO itemDTO : dto.getItems()) {
            Producto producto = obtenerProducto(itemDTO.getProductoId());
            validarStock(producto, itemDTO.getCantidad());

            BigDecimal precioUnitario = calcularPrecioUnitario(producto, usuario);
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(itemDTO.getCantidad()));

            PedidoItem item = crearItemPedido(producto, itemDTO, precioUnitario, subtotal);
            items.add(item);
            total = total.add(subtotal);

            actualizarStock(producto, itemDTO.getCantidad());
        }

        Pedido pedido = guardarPedido(usuario, items, total);
        procesarPuntos(usuario, total);

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
        return pedidoRepository.findById(id);
    }

    // --- Métodos Auxiliares ---

    private Usuario obtenerUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private Producto obtenerProducto(Long productoId) {
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    private void validarStock(Producto producto, int cantidad) {
        if (producto.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para producto: " + producto.getNombre());
        }
    }

    private BigDecimal calcularPrecioUnitario(Producto producto, Usuario usuario) {
        BigDecimal precioUnitario = producto.getPrecio();
        boolean isDuocUser = usuario.getCorreo().endsWith(DUOC_DOMAIN)
                || usuario.getCorreo().endsWith(PROFESOR_DUOC_DOMAIN);
        if (isDuocUser) {
            precioUnitario = precioUnitario.multiply(DESCUENTO_DUOC).setScale(2, RoundingMode.HALF_UP);
        }
        return precioUnitario;
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

    private Pedido guardarPedido(Usuario usuario, List<PedidoItem> items, BigDecimal total) {
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setItems(items);
        pedido.setTotal(total);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        items.forEach(item -> item.setPedido(pedido));
        return pedidoRepository.save(pedido);
    }

    private void procesarPuntos(Usuario usuario, BigDecimal total) {
        int puntosGanados = total.divide(BigDecimal.TEN, 0, RoundingMode.DOWN).intValue() * POINTS_PER_TEN_UNITS;
        if (puntosGanados > 0) {
            puntosService.sumarPuntos(new PuntosDTO(usuario.getId(), puntosGanados));
        }
    }
}
