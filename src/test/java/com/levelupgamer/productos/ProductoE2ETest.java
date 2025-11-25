package com.levelupgamer.productos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupgamer.autenticacion.LoginRequest;
import com.levelupgamer.common.storage.FileStorageService;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

        @MockBean
        private FileStorageService fileStorageService;

        private String adminToken;

        @BeforeEach
        void setUp() throws Exception {
                
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                Usuario admin = Usuario.builder()
                                .run("11111111-1") 
                                .nombre("Admin")
                                .apellidos("Test")
                                .correo("admin-" + uniqueId + "@example.com") 
                                .contrasena(passwordEncoder.encode("admin123"))
                                .fechaNacimiento(LocalDate.now().minusYears(30))
                                .roles(Set.of(RolUsuario.ADMINISTRADOR))
                                .activo(true)
                                .build();
                usuarioRepository.save(admin);

                
                LoginRequest loginRequest = LoginRequest.builder()
                                .correo(admin.getCorreo())
                                .contrasena("admin123")
                                .build();

                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                String responseBody = result.getResponse().getContentAsString();
                JsonNode root = objectMapper.readTree(responseBody);
                adminToken = root.get("accessToken").asText();
        }

        @Test
        void deberiaCrearYListarProductos() throws Exception {
                
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

                
                MockMultipartFile productoPart = new MockMultipartFile(
                                "producto",
                                "producto.json",
                                MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsBytes(newProduct));

                MockMultipartFile imagenPart = new MockMultipartFile(
                                "imagen",
                                "imagen.jpg",
                                MediaType.IMAGE_JPEG_VALUE,
                                "fake-image-content".getBytes());

                when(fileStorageService.uploadFile(any(), any(), anyLong())).thenReturn("/uploads/e2e.jpg");

                mockMvc.perform(multipart("/api/products")
                                .file(productoPart)
                                .file(imagenPart)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.codigo").value("E2E-001"));

                
                mockMvc.perform(get("/api/products")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[?(@.codigo=='E2E-001')]").exists());
        }
}
