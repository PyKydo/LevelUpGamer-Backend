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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoService {
    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);
    private static final int POINTS_PER_TEN_UNITS = 1;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PuntosService puntosService;

    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository, ProductoRepository productoRepository, PuntosService puntosService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.puntosService = puntosService;
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
            
            BigDecimal precioUnitario = producto.getPrecio();
            boolean isDuocUser = usuario.getCorreo().endsWith("duoc.cl") || usuario.getCorreo().endsWith("profesor.duoc.cl");
            if (isDuocUser) {
                precioUnitario = precioUnitario.multiply(new BigDecimal("0.8")).setScale(2, RoundingMode.HALF_UP);
            }
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(itemDTO.getCantidad()));
            PedidoItem item = new PedidoItem();
            item.setProducto(producto);
            item.setCantidad(itemDTO.getCantidad());
            item.setPrecioUnitario(precioUnitario);
            item.setSubtotal(subtotal);
            items.add(item);
            total = total.add(subtotal);
            
            producto.setStock(producto.getStock() - itemDTO.getCantidad());
            productoRepository.save(producto);

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

        // Asignar puntos al usuario usando el PuntosService
        int puntosGanados = total.divide(BigDecimal.TEN, 0, RoundingMode.DOWN).intValue() * POINTS_PER_TEN_UNITS;
        if (puntosGanados > 0) {
            puntosService.sumarPuntos(new PuntosDTO(usuario.getId(), puntosGanados));
        }

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
