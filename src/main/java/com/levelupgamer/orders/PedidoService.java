package com.levelupgamer.orders;

import com.levelupgamer.orders.dto.PedidoCrearDTO;
import com.levelupgamer.orders.dto.PedidoItemCrearDTO;
import com.levelupgamer.orders.dto.PedidoRespuestaDTO;
import com.levelupgamer.products.Producto;
import com.levelupgamer.products.ProductoRepository;
import com.levelupgamer.users.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.levelupgamer.users.UsuarioRepository;

@Service
public class PedidoService {
    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);
    private static final int POINTS_PER_TEN_UNITS = 1; // 1 punto por cada 10 unidades monetarias
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository, ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public PedidoRespuestaDTO crearPedido(PedidoCrearDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        List<PedidoItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (PedidoItemCrearDTO itemDTO : dto.getItems()) {
            Producto producto = productoRepository.findById(itemDTO.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            if (producto.getStock() < itemDTO.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para producto: " + producto.getNombre());
            }
            // Descuento 20% para correos Duoc
            BigDecimal precioUnitario = producto.getPrecio();
            if (usuario.getIsDuocUser()) {
                precioUnitario = precioUnitario.multiply(new BigDecimal("0.8")).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(itemDTO.getCantidad()));
            PedidoItem item = new PedidoItem();
            item.setProducto(producto);
            item.setCantidad(itemDTO.getCantidad());
            item.setPrecioUnitario(precioUnitario);
            item.setSubtotal(subtotal);
            items.add(item);
            total = total.add(subtotal);
            // Actualizar stock
            producto.setStock(producto.getStock() - itemDTO.getCantidad());
            productoRepository.save(producto);

            // Verificar stock crítico
            if (producto.getStockCritico() != null && producto.getStock() <= producto.getStockCritico()) {
                logger.warn("Alerta de stock crítico para el producto {}. Stock actual: {}, Stock crítico: {}",
                            producto.getNombre(), producto.getStock(), producto.getStockCritico());
            }
        }
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setItems(items);
        pedido.setTotal(total);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        for (PedidoItem item : items) {
            item.setPedido(pedido);
        }
        pedidoRepository.save(pedido);

        // Asignar puntos al usuario por la compra
        int puntosGanados = total.divide(BigDecimal.TEN, BigDecimal.ROUND_DOWN).intValue() * POINTS_PER_TEN_UNITS;
        usuario.setPuntosLevelUp(usuario.getPuntosLevelUp() + puntosGanados);
        usuarioRepository.save(usuario);

        return PedidoMapper.toDTO(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoRespuestaDTO> listarPedidosPorUsuario(Long usuarioId) {
        return pedidoRepository.findAll().stream()
                .filter(p -> p.getUsuario() != null && p.getUsuario().getId().equals(usuarioId))
                .map(PedidoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }
}
