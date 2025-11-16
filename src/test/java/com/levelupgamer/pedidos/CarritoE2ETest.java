package com.levelupgamer.pedidos;

import com.levelupgamer.productos.Producto;
import com.levelupgamer.productos.ProductoRepository;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CarritoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    private Usuario usuario;
    private Producto producto;

    @BeforeEach
    void setUp() {
        // Limpiar repositorios para asegurar un estado limpio en cada test
        carritoRepository.deleteAll();
        usuarioRepository.deleteAll();
        productoRepository.deleteAll();

        // Crear entidades de prueba
        usuario = new Usuario();
        usuario.setNombre("Test User");
        usuario.setCorreo("test@user.com");
        usuario.setContrasena("password");
        usuario = usuarioRepository.save(usuario);

        producto = new Producto();
        producto.setNombre("Test Product");
        producto.setPrecio(new java.math.BigDecimal("99.99"));
        producto.setStock(100);
        producto.setCodigo("P123");
        producto = productoRepository.save(producto);
    }

    @Test
    void getCartByUserId_debeCrearYRetornarCarritoVacio() throws Exception {
        mockMvc.perform(get("/api/carrito/{userId}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total", is(0.0)));
    }

    @Test
    void addProductToCart_debeAgregarProductoYRetornarCarritoActualizado() throws Exception {
        mockMvc.perform(post("/api/carrito/{userId}/add", usuario.getId())
                        .param("productId", producto.getId().toString())
                        .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId", is(producto.getId().intValue())))
                .andExpect(jsonPath("$.items[0].quantity", is(2)))
                .andExpect(jsonPath("$.total", is(199.98))); // 2 * 99.99
    }

    @Test
    void removeProductFromCart_debeQuitarProductoYRetornarCarritoActualizado() throws Exception {
        // Primero, agregar un producto al carrito
        mockMvc.perform(post("/api/carrito/{userId}/add", usuario.getId())
                        .param("productId", producto.getId().toString())
                        .param("quantity", "1"));

        // Luego, eliminarlo
        mockMvc.perform(delete("/api/carrito/{userId}/remove", usuario.getId())
                        .param("productId", producto.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total", is(0.0)));
    }
}
