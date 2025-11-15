package com.levelupgamer.productos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupgamer.autenticacion.LoginRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ProductoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Crear el usuario admin con datos únicos
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        Usuario admin = Usuario.builder()
                .run("88" + uniqueId) // RUN único
                .nombre("Admin")
                .apellidos("Test")
                .correo("admin-" + uniqueId + "@example.com") // Correo único
                .contrasena(passwordEncoder.encode("admin123"))
                .fechaNacimiento(LocalDate.now().minusYears(30))
                .roles(Set.of(RolUsuario.ADMINISTRADOR))
                .activo(true)
                .build();
        usuarioRepository.save(admin);

        // Iniciar sesión como admin para obtener un token válido
        LoginRequest loginRequest = new LoginRequest(admin.getCorreo(), "admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);
        adminToken = root.get("token").asText();
    }

    @Test
    void deberiaCrearYListarProductos() throws Exception {
        // --- 1. Probar la Creación de un Producto ---
        Producto newProduct = Producto.builder()
                .codigo("E2E-001")
                .nombre("Producto de Prueba E2E")
                .descripcion("Descripción del producto de prueba")
                .precio(new BigDecimal("99.99"))
                .stock(100)
                .stockCritico(10)
                .categoria(CategoriaProducto.CONSOLAS)
                .imagenes(Collections.singletonList("http://example.com/image.jpg"))
                .build();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("E2E-001"));

        // --- 2. Probar el Listado de Productos ---
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.codigo=='E2E-001')]").exists());
    }
}
