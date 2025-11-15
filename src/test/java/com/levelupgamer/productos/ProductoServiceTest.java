package com.levelupgamer.productos;

import com.levelupgamer.productos.dto.ProductoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;
    private ProductoDTO productoDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        producto = new Producto();
        producto.setId(1L);
        producto.setCodigo("P001");
        producto.setNombre("Producto Test");
        producto.setActivo(true);
        producto.setPrecio(new BigDecimal("10.00"));
        producto.setImagenes(Collections.singletonList("http://example.com/test.jpg"));

        productoDTO = new ProductoDTO();
        productoDTO.setId(1L);
        productoDTO.setCodigo("P001");
        productoDTO.setNombre("Producto Test");
        productoDTO.setImagenes(Collections.singletonList("http://example.com/test.jpg"));
    }

    @Test
    void listarProductos_retornaListaDeProductosDTO() {
        // Given
        when(productoRepository.findAll()).thenReturn(Collections.singletonList(producto));

        // When
        List<ProductoDTO> result = productoService.listarProductos();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(productoDTO.getId(), result.get(0).getId());
        assertEquals(productoDTO.getImagenes(), result.get(0).getImagenes());
    }

    @Test
    void crearProducto_conCodigoNuevo_guardaYRetornaProductoDTO() {
        // Given
        when(productoRepository.existsByCodigo("P001")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // When
        ProductoDTO result = productoService.crearProducto(producto);

        // Then
        assertNotNull(result);
        assertEquals(productoDTO.getId(), result.getId());
        assertTrue(producto.getActivo());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void crearProducto_conCodigoExistente_lanzaExcepcion() {
        // Given
        when(productoRepository.existsByCodigo("P001")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> productoService.crearProducto(producto));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void buscarPorId_productoActivoExistente_retornaOptionalConProducto() {
        // Given
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // When
        Optional<Producto> result = productoService.buscarPorId(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(producto, result.get());
    }

    @Test
    void buscarPorId_productoInactivo_retornaOptionalVacio() {
        // Given
        producto.setActivo(false);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // When
        Optional<Producto> result = productoService.buscarPorId(1L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void actualizarProducto_productoExistente_actualizaYRetornaOptionalConProducto() {
        // Given
        Producto productoActualizado = new Producto();
        productoActualizado.setNombre("Producto Actualizado");
        productoActualizado.setImagenes(Collections.singletonList("http://example.com/new.jpg"));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // When
        Optional<Producto> result = productoService.actualizarProducto(1L, productoActualizado);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Producto Actualizado", result.get().getNombre());
        assertEquals(productoActualizado.getImagenes(), result.get().getImagenes());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void eliminarProducto_productoExistente_desactivaYRetornaTrue() {
        // Given
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // When
        boolean result = productoService.eliminarProducto(1L);

        // Then
        assertTrue(result);
        assertFalse(producto.getActivo());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void eliminarProducto_productoNoExistente_retornaFalse() {
        // Given
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        boolean result = productoService.eliminarProducto(1L);

        // Then
        assertFalse(result);
        verify(productoRepository, never()).save(any(Producto.class));
    }
}
