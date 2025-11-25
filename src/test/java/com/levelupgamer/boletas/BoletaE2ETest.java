package com.levelupgamer.pedidos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupgamer.autenticacion.LoginRequest;
import com.levelupgamer.pedidos.dto.BoletaCrearRequest;
import com.levelupgamer.pedidos.dto.BoletaDetalleRequest;
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
                
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                Usuario cliente = Usuario.builder()
                                .run("99" + uniqueId) 
                                .nombre("Cliente")
                                .apellidos("Test")
                                .correo("cliente-" + uniqueId + "@gmail.com") 
                                .contrasena(passwordEncoder.encode("cliente"))
                                .fechaNacimiento(LocalDate.now().minusYears(20))
                                .roles(Set.of(RolUsuario.CLIENTE))
                                .activo(true)
                                .build();
                usuarioRepository.saveAndFlush(cliente);
                clienteId = cliente.getId();

                
                LoginRequest loginRequest = LoginRequest.builder()
                                .correo(cliente.getCorreo())
                                .contrasena("cliente")
                                .build();
                MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andReturn();
                JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
                clienteToken = root.get("accessToken").asText();

                
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
                
                BoletaDetalleRequest detalle = BoletaDetalleRequest.builder()
                                .productoId(producto.getId())
                                .cantidad(2)
                                .build();

                BoletaCrearRequest boletaRequest = BoletaCrearRequest.builder()
                                .clienteId(clienteId)
                                .total(new BigDecimal("200.00"))
                                .detalles(Collections.singletonList(detalle))
                                .build();

                mockMvc.perform(post("/api/v1/boletas")
                                .header("Authorization", "Bearer " + clienteToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(boletaRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.total").value(200.00));

                
                Producto productoActualizado = productoRepository.findById(producto.getId()).orElseThrow();
                assertEquals(18, productoActualizado.getStock());
        }
}
