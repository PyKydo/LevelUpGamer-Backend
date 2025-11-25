package com.levelupgamer.productos;

import com.levelupgamer.common.storage.FileStorageService;
import com.levelupgamer.productos.categorias.Categoria;
import com.levelupgamer.productos.categorias.CategoriaRepository;
import com.levelupgamer.productos.dto.ProductoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;
    private ProductoDTO productoDTO;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        categoria = new Categoria();
        categoria.setId(10L);
        categoria.setCodigo("CAT-TEST");
        categoria.setNombre("Categoria Test");
        categoria.setActivo(true);

        producto = new Producto();
        producto.setId(1L);
        producto.setCodigo("P001");
        producto.setNombre("Producto Test");
        producto.setActivo(true);
        producto.setPrecio(new BigDecimal("10.00"));
        producto.setImagenes(Collections.singletonList("http://example.com/test.jpg"));
        producto.setCategoria(categoria);

        productoDTO = new ProductoDTO();
        productoDTO.setId(1L);
        productoDTO.setCodigo("P001");
        productoDTO.setNombre("Producto Test");
        productoDTO.setImagenes(Collections.singletonList("http://example.com/test.jpg"));
        when(categoriaRepository.findById(categoria.getId())).thenReturn(Optional.of(categoria));
    }

    @Test
    void listarProductos_retornaListaDeProductosDTO() {
        when(productoRepository.findAll()).thenReturn(Collections.singletonList(producto));
        List<ProductoDTO> result = productoService.listarProductos();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void crearProducto_conCodigoNuevo_guardaYRetornaProductoDTO() throws IOException {
        
        MockMultipartFile mockImage = new MockMultipartFile("imagen", "test.jpg", "image/jpeg", "test-image".getBytes());
        String imageUrl = "http://s3.test.url/test.jpg";

        when(productoRepository.existsByCodigo("P001")).thenReturn(false);
        when(fileStorageService.uploadFile(any(), any(), anyLong())).thenReturn(imageUrl);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ProductoDTO result = productoService.crearProducto(producto, mockImage);

        
        assertNotNull(result);
        assertEquals(imageUrl, result.getImagenes().get(0));
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void crearProducto_conCodigoExistente_lanzaExcepcion() throws IOException {
        
        MockMultipartFile mockImage = new MockMultipartFile("imagen", "test.jpg", "image/jpeg", "test-image".getBytes());
        when(productoRepository.existsByCodigo("P001")).thenReturn(true);

        
        assertThrows(IllegalArgumentException.class, () -> productoService.crearProducto(producto, mockImage));
        verify(fileStorageService, never()).uploadFile(any(), any(), anyLong());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void crearProducto_sinImagen_noSubeArchivo() throws IOException {
        when(productoRepository.existsByCodigo("P001")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductoDTO result = productoService.crearProducto(producto, null);

        assertNotNull(result);
        verify(fileStorageService, never()).uploadFile(any(), any(), anyLong());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void buscarPorId_productoActivoExistente_retornaOptionalConProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        Optional<Producto> result = productoService.buscarPorId(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void actualizarProducto_productoExistente_actualizaYRetornaOptionalConProducto() {
        
        Producto productoActualizado = new Producto();
        productoActualizado.setNombre("Producto Actualizado");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        
        Optional<Producto> result = productoService.actualizarProducto(1L, productoActualizado);

        
        assertTrue(result.isPresent());
        assertEquals("Producto Actualizado", result.get().getNombre());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void eliminarProducto_productoExistente_desactivaYRetornaTrue() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        boolean result = productoService.eliminarProducto(1L);
        assertTrue(result);
        assertFalse(producto.getActivo());
        verify(productoRepository, times(1)).save(producto);
    }
}
