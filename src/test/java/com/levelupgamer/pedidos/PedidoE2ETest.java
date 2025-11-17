package com.levelupgamer.pedidos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupgamer.autenticacion.LoginRequest;
import com.levelupgamer.pedidos.dto.PedidoCrearDTO;
import com.levelupgamer.pedidos.dto.PedidoItemCrearDTO;
import com.levelupgamer.productos.CategoriaProducto;
import com.levelupgamer.productos.Producto;
import com.levelupgamer.productos.ProductoRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class PedidoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private String clienteToken;
    private Long clienteId;
    private Producto producto;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        // Crear el usuario cliente con datos únicos
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        Usuario cliente = Usuario.builder()
                .run("99" + uniqueId) // RUN único
                .nombre("Cliente")
                .apellidos("Test")
                .correo("cliente-" + uniqueId + "@gmail.com") // Correo único
                .contrasena(passwordEncoder.encode("cliente"))
                .fechaNacimiento(LocalDate.now().minusYears(20))
                .roles(Set.of(RolUsuario.CLIENTE))
                .activo(true)
                .build();
        usuarioRepository.saveAndFlush(cliente);
        clienteId = cliente.getId();

        // Iniciar sesión como cliente
        LoginRequest loginRequest = new LoginRequest(cliente.getCorreo(), "cliente");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        clienteToken = root.get("accessToken").asText();

        // Crear un producto para la prueba
        producto = Producto.builder()
                .codigo("E2E-PEDIDO-001")
                .nombre("Producto para Pedido")
                .precio(new BigDecimal("100.00"))
                .stock(20)
                .categoria(CategoriaProducto.CONSOLAS)
                .activo(true)
                .build();
        productoRepository.saveAndFlush(producto);
    }

    @Test
    void deberiaCrearUnPedidoYReducirElStockDelProducto() throws Exception {
        // --- 1. Crear el Pedido ---
        PedidoItemCrearDTO itemDTO = new PedidoItemCrearDTO();
        itemDTO.setProductoId(producto.getId());
        itemDTO.setCantidad(2);

        PedidoCrearDTO pedidoDTO = new PedidoCrearDTO();
        pedidoDTO.setUsuarioId(clienteId);
        pedidoDTO.setItems(Collections.singletonList(itemDTO));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(200.00));

        // --- 2. Verificar que el stock del producto se ha reducido ---
        Producto productoActualizado = productoRepository.findById(producto.getId()).orElseThrow();
        assertEquals(18, productoActualizado.getStock());
    }
}
