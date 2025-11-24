package com.levelupgamer.pedidos;

import com.levelupgamer.pedidos.dto.CarritoDto;
import com.levelupgamer.productos.Producto;
import com.levelupgamer.productos.ProductoRepository;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private CarritoMapper carritoMapper;

    @InjectMocks
    private CarritoService carritoService;

    private Usuario usuario;
    private Producto producto;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Test Product");
        producto.setPrecio(new java.math.BigDecimal("10.00"));

        carrito = new Carrito(usuario);
        carrito.setId(1L);
    }

    @Test
    void getCartByUserId_cuandoCarritoExiste_debeRetornarlo() {
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(carritoMapper.toDto(any(Carrito.class))).thenReturn(new CarritoDto());

        CarritoDto result = carritoService.getCartByUserId(1L);

        assertThat(result).isNotNull();
        verify(carritoRepository).findByUsuarioId(1L);
        verify(carritoMapper).toDto(carrito);
    }

    @Test
    void getCartByUserId_cuandoCarritoNoExiste_debeCrearlo() {
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        when(carritoMapper.toDto(any(Carrito.class))).thenReturn(new CarritoDto());

        CarritoDto result = carritoService.getCartByUserId(1L);

        assertThat(result).isNotNull();
        verify(carritoRepository).save(any(Carrito.class));
        verify(carritoMapper).toDto(carrito);
    }

    @Test
    void addProductToCart_cuandoProductoEsNuevo_debeAgregarlo() {
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        when(carritoMapper.toDto(any(Carrito.class))).thenReturn(new CarritoDto());

        carritoService.addProductToCart(1L, 1L, 2);

        assertThat(carrito.getItems()).hasSize(1);
        assertThat(carrito.getItems().get(0).getQuantity()).isEqualTo(2);
        verify(carritoRepository).save(carrito);
    }

    @Test
    void addProductToCart_cuandoProductoYaExiste_debeIncrementarCantidad() {
        CarritoItem itemExistente = new CarritoItem(carrito, producto, 1);
        carrito.addItem(itemExistente);

        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        when(carritoMapper.toDto(any(Carrito.class))).thenReturn(new CarritoDto());

        carritoService.addProductToCart(1L, 1L, 3);

        assertThat(carrito.getItems()).hasSize(1);
        assertThat(carrito.getItems().get(0).getQuantity()).isEqualTo(4); // 1 + 3
        verify(carritoRepository).save(carrito);
    }

    @Test
    void removeProductFromCart_debeQuitarElProducto() {
        CarritoItem itemExistente = new CarritoItem(carrito, producto, 1);
        carrito.addItem(itemExistente);

        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        when(carritoMapper.toDto(any(Carrito.class))).thenReturn(new CarritoDto());

        carritoService.removeProductFromCart(1L, 1L);

        assertThat(carrito.getItems()).isEmpty();
        verify(carritoRepository).save(carrito);
    }

    @Test
    void clearCart_debeVaciarElCarrito() {
        CarritoItem item1 = new CarritoItem(carrito, producto, 1);
        carrito.addItem(item1);

        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        when(carritoMapper.toDto(any(Carrito.class))).thenReturn(new CarritoDto());

        carritoService.clearCart(1L);

        assertThat(carrito.getItems()).isEmpty();
        verify(carritoRepository).save(carrito);
    }
}
