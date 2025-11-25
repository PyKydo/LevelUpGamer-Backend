package com.levelupgamer.productos;

import com.levelupgamer.common.storage.FileStorageService;
import com.levelupgamer.productos.categorias.Categoria;
import com.levelupgamer.productos.categorias.CategoriaRepository;
import com.levelupgamer.productos.dto.ProductoDTO;
import com.levelupgamer.productos.dto.ProductoRequest;
import com.levelupgamer.usuarios.RolUsuario;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
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

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;
    private ProductoDTO productoDTO;
    private ProductoRequest productoRequest;
    private Categoria categoria;
    private Usuario adminUsuario;
    private Usuario vendedorUsuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        categoria = new Categoria();
        categoria.setId(10L);
        categoria.setCodigo("CAT-TEST");
        categoria.setNombre("Categoria Test");
        categoria.setActivo(true);

        adminUsuario = Usuario.builder()
            .id(100L)
            .correo("admin@test.com")
            .nombre("Admin")
            .roles(Collections.singleton(RolUsuario.ADMINISTRADOR))
            .build();

        vendedorUsuario = Usuario.builder()
            .id(200L)
            .correo("vendor@test.com")
            .nombre("Vendor")
            .roles(Collections.singleton(RolUsuario.VENDEDOR))
            .build();

        producto = new Producto();
        producto.setId(1L);
        producto.setCodigo("P001");
        producto.setNombre("Producto Test");
        producto.setActivo(true);
        producto.setPrecio(new BigDecimal("10.00"));
        producto.setImagenes(Collections.singletonList("http://example.com/test.jpg"));
        producto.setCategoria(categoria);
        producto.setVendedor(adminUsuario);

        productoDTO = new ProductoDTO();
        productoDTO.setId(1L);
        productoDTO.setCodigo("P001");
        productoDTO.setNombre("Producto Test");
        productoDTO.setImagenes(Collections.singletonList("http://example.com/test.jpg"));
        when(categoriaRepository.findById(categoria.getId())).thenReturn(Optional.of(categoria));

        productoRequest = ProductoRequest.builder()
            .codigo("P001")
            .nombre("Producto Test")
            .descripcion("Desc test")
            .precio(new BigDecimal("10.00"))
            .stock(5)
            .stockCritico(1)
            .categoriaId(categoria.getId())
            .puntosLevelUp(100)
            .imagenes(Collections.singletonList("http://example.com/test.jpg"))
            .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listarProductos_retornaListaDeProductosDTO() {
        authenticateAs(adminUsuario.getCorreo(), "ADMINISTRADOR");
        when(productoRepository.findAll()).thenReturn(Collections.singletonList(producto));
        List<ProductoDTO> result = productoService.listarProductos();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void listarProductos_paraVendedor_filtraPorPropietario() {
        Producto productoVendedor = new Producto();
        productoVendedor.setId(2L);
        productoVendedor.setCodigo("PV01");
        productoVendedor.setNombre("Propio");
        productoVendedor.setPrecio(new BigDecimal("20"));
        productoVendedor.setCategoria(categoria);
        productoVendedor.setVendedor(vendedorUsuario);

        authenticateAs(vendedorUsuario.getCorreo(), "VENDEDOR");
        when(usuarioRepository.findByCorreo(vendedorUsuario.getCorreo())).thenReturn(Optional.of(vendedorUsuario));
        when(productoRepository.findAllByVendedorId(vendedorUsuario.getId())).thenReturn(Collections.singletonList(productoVendedor));

        List<ProductoDTO> result = productoService.listarProductos();

        assertEquals(1, result.size());
        assertEquals("PV01", result.get(0).getCodigo());
    }

    @Test
    void crearProducto_conCodigoNuevo_guardaYRetornaProductoDTO() throws IOException {
        
        MockMultipartFile mockImage = new MockMultipartFile("imagen", "test.jpg", "image/jpeg", "test-image".getBytes());
        String imageUrl = "http://s3.test.url/test.jpg";

        authenticateAs(adminUsuario.getCorreo(), "ADMINISTRADOR");
        when(usuarioRepository.findByCorreo(adminUsuario.getCorreo())).thenReturn(Optional.of(adminUsuario));
        when(productoRepository.existsByCodigo("P001")).thenReturn(false);
        when(fileStorageService.uploadFile(any(), any(), anyLong())).thenReturn(imageUrl);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ProductoDTO result = productoService.crearProducto(productoRequest, mockImage);

        
        assertNotNull(result);
        assertEquals(imageUrl, result.getImagenes().get(0));
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void crearProducto_conCodigoExistente_lanzaExcepcion() throws IOException {
        
        MockMultipartFile mockImage = new MockMultipartFile("imagen", "test.jpg", "image/jpeg", "test-image".getBytes());
        authenticateAs(adminUsuario.getCorreo(), "ADMINISTRADOR");
        when(productoRepository.existsByCodigo("P001")).thenReturn(true);

        
        assertThrows(IllegalArgumentException.class, () -> productoService.crearProducto(productoRequest, mockImage));
        verify(fileStorageService, never()).uploadFile(any(), any(), anyLong());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void crearProducto_sinImagen_noSubeArchivo() throws IOException {
        authenticateAs(adminUsuario.getCorreo(), "ADMINISTRADOR");
        when(usuarioRepository.findByCorreo(adminUsuario.getCorreo())).thenReturn(Optional.of(adminUsuario));
        when(productoRepository.existsByCodigo("P001")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductoDTO result = productoService.crearProducto(productoRequest, null);

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
        
        ProductoRequest productoActualizado = productoRequest.toBuilder()
            .nombre("Producto Actualizado")
            .build();
        authenticateAs(adminUsuario.getCorreo(), "ADMINISTRADOR");
        when(usuarioRepository.findByCorreo(adminUsuario.getCorreo())).thenReturn(Optional.of(adminUsuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        
        Optional<Producto> result = productoService.actualizarProducto(1L, productoActualizado);

        
        assertTrue(result.isPresent());
        assertEquals("Producto Actualizado", result.get().getNombre());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void eliminarProducto_productoExistente_desactivaYRetornaTrue() {
        authenticateAs(adminUsuario.getCorreo(), "ADMINISTRADOR");
        when(usuarioRepository.findByCorreo(adminUsuario.getCorreo())).thenReturn(Optional.of(adminUsuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        boolean result = productoService.eliminarProducto(1L);
        assertTrue(result);
        assertFalse(producto.getActivo());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void eliminarProducto_vendedorNoPropietario_lanzaAccesoDenegado() {
        producto.setVendedor(adminUsuario);
        authenticateAs(vendedorUsuario.getCorreo(), "VENDEDOR");
        when(usuarioRepository.findByCorreo(vendedorUsuario.getCorreo())).thenReturn(Optional.of(vendedorUsuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThrows(AccessDeniedException.class, () -> productoService.eliminarProducto(1L));
        verify(productoRepository, never()).save(producto);
    }

    private void authenticateAs(String correo, String... roles) {
        String[] authorities = Arrays.stream(roles)
                .map(rol -> "ROLE_" + rol)
                .toArray(String[]::new);
        TestingAuthenticationToken authenticationToken = new TestingAuthenticationToken(correo, null, authorities);
        authenticationToken.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
