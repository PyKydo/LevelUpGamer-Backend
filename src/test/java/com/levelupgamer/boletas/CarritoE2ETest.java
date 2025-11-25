package com.levelupgamer.boletas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupgamer.autenticacion.LoginRequest;
import com.levelupgamer.productos.Producto;
import com.levelupgamer.productos.ProductoRepository;
import com.levelupgamer.productos.categorias.Categoria;
import com.levelupgamer.productos.categorias.CategoriaRepository;
import com.levelupgamer.usuarios.RolUsuario;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@SuppressWarnings("null")
class CarritoE2ETest {

        private final MockMvc mockMvc;
        private final UsuarioRepository usuarioRepository;
        private final ProductoRepository productoRepository;
        private final CarritoRepository carritoRepository;
        private final CategoriaRepository categoriaRepository;
        private final BCryptPasswordEncoder passwordEncoder;
        private final ObjectMapper objectMapper;

        private Usuario usuario;
        private Producto producto;
        private String clienteToken;

        @Autowired
        public CarritoE2ETest(MockMvc mockMvc, UsuarioRepository usuarioRepository,
                        ProductoRepository productoRepository,
                        CarritoRepository carritoRepository, CategoriaRepository categoriaRepository,
                        BCryptPasswordEncoder passwordEncoder,
                        ObjectMapper objectMapper) {
                this.mockMvc = mockMvc;
                this.usuarioRepository = usuarioRepository;
                this.productoRepository = productoRepository;
                this.carritoRepository = carritoRepository;
                this.categoriaRepository = categoriaRepository;
                this.passwordEncoder = passwordEncoder;
                this.objectMapper = objectMapper;
        }

        @BeforeEach
        @Transactional
        void setUp() throws Exception {
                
                carritoRepository.deleteAll();
                usuarioRepository.deleteAll();
                productoRepository.deleteAll();

                
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                usuario = Usuario.builder()
                                .run("77" + uniqueId)
                                .nombre("Cliente Carrito")
                                .apellidos("Test")
                                .correo("carrito-" + uniqueId + "@gmail.com")
                                .contrasena(passwordEncoder.encode("cliente"))
                                .fechaNacimiento(LocalDate.now().minusYears(25))
                                .roles(Set.of(RolUsuario.CLIENTE))
                                .activo(true)
                                .build();
                usuarioRepository.saveAndFlush(usuario);

                
                LoginRequest loginRequest = LoginRequest.builder()
                                .correo(usuario.getCorreo())
                                .contrasena("cliente")
                                .build();
                MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andReturn();
                JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
                clienteToken = root.get("accessToken").asText();

                
                Categoria categoria = categoriaRepository.save(Categoria.builder()
                                .codigo("CAT-" + uniqueId)
                                .nombre("Categoria Carrito")
                                .descripcion("Categoria para pruebas de carrito")
                                .activo(true)
                                .build());

                producto = new Producto();
                producto.setNombre("Test Product");
                producto.setPrecio(new java.math.BigDecimal("99.99"));
                producto.setStock(100);
                producto.setCodigo("P" + uniqueId);
                producto.setCategoria(categoria);
                producto = productoRepository.saveAndFlush(producto);
        }

        @Test
        void getCartByUserId_debeCrearYRetornarCarritoVacio() throws Exception {
                mockMvc.perform(get("/api/v1/cart/{userId}", usuario.getId())
                                .header("Authorization", "Bearer " + clienteToken))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.items", hasSize(0)))
                                .andExpect(jsonPath("$.total", is(0.0)));
        }

        @Test
        void addProductToCart_debeAgregarProductoYRetornarCarritoActualizado() throws Exception {
                mockMvc.perform(post("/api/v1/cart/{userId}/add", usuario.getId())
                                .header("Authorization", "Bearer " + clienteToken)
                                .param("productId", producto.getId().toString())
                                .param("quantity", "2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.items", hasSize(1)))
                                .andExpect(jsonPath("$.items[0].productId", is(producto.getId().intValue())))
                                .andExpect(jsonPath("$.items[0].quantity", is(2)))
                                .andExpect(jsonPath("$.total", is(199.98))); 
        }

        @Test
        void removeProductFromCart_debeQuitarProductoYRetornarCarritoActualizado() throws Exception {
                
                mockMvc.perform(post("/api/v1/cart/{userId}/add", usuario.getId())
                                .header("Authorization", "Bearer " + clienteToken)
                                .param("productId", producto.getId().toString())
                                .param("quantity", "1"));

                
                mockMvc.perform(delete("/api/v1/cart/{userId}/remove", usuario.getId())
                                .header("Authorization", "Bearer " + clienteToken)
                                .param("productId", producto.getId().toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.items", hasSize(0)))
                                .andExpect(jsonPath("$.total", is(0.0)));
        }

        @Test
        void clearCart_debeVaciarElCarrito() throws Exception {
                
                mockMvc.perform(post("/api/v1/cart/{userId}/add", usuario.getId())
                                .header("Authorization", "Bearer " + clienteToken)
                                .param("productId", producto.getId().toString())
                                .param("quantity", "3"));

                
                mockMvc.perform(delete("/api/v1/cart/{userId}", usuario.getId())
                                .header("Authorization", "Bearer " + clienteToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.items", hasSize(0)))
                                .andExpect(jsonPath("$.total", is(0.0)));
        }
}
