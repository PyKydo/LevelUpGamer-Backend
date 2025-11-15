package com.levelupgamer.pedidos;

import com.levelupgamer.gamificacion.PuntosService;
import com.levelupgamer.gamificacion.dto.PuntosDTO;
import com.levelupgamer.pedidos.dto.PedidoCrearDTO;
import com.levelupgamer.pedidos.dto.PedidoItemCrearDTO;
import com.levelupgamer.pedidos.dto.PedidoRespuestaDTO;
import com.levelupgamer.productos.Producto;
import com.levelupgamer.productos.ProductoRepository;
import com.levelupgamer.usuarios.RolUsuario;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PuntosService puntosService; // Mockear el servicio de puntos

    @InjectMocks
    private PedidoService pedidoService;

    private Usuario usuario;
    private Producto producto;
    private PedidoCrearDTO pedidoCrearDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCorreo("test@example.com");
        usuario.setIsDuocUser(false);
        usuario.setRoles(Set.of(RolUsuario.CLIENTE));

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto Test");
        producto.setPrecio(new BigDecimal("100.00"));
        producto.setStock(10);
        producto.setStockCritico(5);

        PedidoItemCrearDTO itemDTO = new PedidoItemCrearDTO();
        itemDTO.setProductoId(1L);
        itemDTO.setCantidad(2);

        pedidoCrearDTO = new PedidoCrearDTO();
        pedidoCrearDTO.setUsuarioId(1L);
        pedidoCrearDTO.setItems(Collections.singletonList(itemDTO));
    }

    @Test
    void crearPedido_conDatosValidos_creaPedidoYActualizaStock() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        PedidoRespuestaDTO result = pedidoService.crearPedido(pedidoCrearDTO);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("200.00"), result.getTotal());
        assertEquals(8, producto.getStock());
        
        // Verificar que se llamó a sumarPuntos con la cantidad correcta
        verify(puntosService, times(1)).sumarPuntos(new PuntosDTO(1L, 20));
        verify(productoRepository, times(1)).save(producto);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void crearPedido_conUsuarioDuoc_aplicaDescuento() {
        // Given
        usuario.setIsDuocUser(true);
        usuario.setCorreo("test@duoc.cl");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PedidoRespuestaDTO result = pedidoService.crearPedido(pedidoCrearDTO);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("160.00"), result.getTotal());
        
        // Verificar que se llamó a sumarPuntos con la cantidad correcta (160 / 10)
        verify(puntosService, times(1)).sumarPuntos(new PuntosDTO(1L, 16));
    }

    @Test
    void crearPedido_conStockInsuficiente_lanzaExcepcion() {
        // Given
        producto.setStock(1);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> pedidoService.crearPedido(pedidoCrearDTO));
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(puntosService, never()).sumarPuntos(any()); // No se deben sumar puntos si falla
    }
}
