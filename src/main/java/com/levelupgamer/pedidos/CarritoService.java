package com.levelupgamer.pedidos;

import com.levelupgamer.pedidos.dto.CarritoDto;
import com.levelupgamer.productos.Producto;
import com.levelupgamer.productos.ProductoRepository;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final CarritoMapper carritoMapper;

    public CarritoService(CarritoRepository carritoRepository, UsuarioRepository usuarioRepository,
                       ProductoRepository productoRepository, CarritoMapper carritoMapper) {
        this.carritoRepository = carritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.carritoMapper = carritoMapper;
    }

    @Transactional
    public CarritoDto getCartByUserId(Long userId) {
        Carrito carrito = carritoRepository.findByUsuarioId(userId)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    return carritoRepository.save(new Carrito(usuario));
                });
        return carritoMapper.toDto(carrito);
    }

    @Transactional
    public CarritoDto addProductToCart(Long userId, Long productId, int quantity) {
        Carrito carrito = carritoRepository.findByUsuarioId(userId)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    return carritoRepository.save(new Carrito(usuario));
                });

        Producto producto = productoRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        carrito.getItems().stream()
                .filter(item -> item.getProducto().getId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + quantity),
                        () -> carrito.addItem(new CarritoItem(carrito, producto, quantity))
                );

        return carritoMapper.toDto(carritoRepository.save(carrito));
    }

    @Transactional
    public CarritoDto removeProductFromCart(Long userId, Long productId) {
        Carrito carrito = carritoRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        carrito.getItems().stream()
                .filter(item -> item.getProducto().getId().equals(productId))
                .findFirst()
                .ifPresent(carrito::removeItem);

        return carritoMapper.toDto(carritoRepository.save(carrito));
    }
}
