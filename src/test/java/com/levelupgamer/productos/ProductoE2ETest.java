package com.levelupgamer.productos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupgamer.autenticacion.LoginRequest;
import com.levelupgamer.common.storage.FileStorageService;
import com.levelupgamer.productos.categorias.Categoria;
import com.levelupgamer.productos.categorias.CategoriaRepository;
import com.levelupgamer.productos.dto.ProductoRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@SuppressWarnings({"null", "removal"})
class ProductoE2ETest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UsuarioRepository usuarioRepository;

        @Autowired
        private BCryptPasswordEncoder passwordEncoder;

        @Autowired
        private CategoriaRepository categoriaRepository;

        @MockBean
        private FileStorageService fileStorageService;

        private String adminToken;
        private Categoria categoriaDefault;

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

                categoriaDefault = categoriaRepository.save(Categoria.builder()
                                .codigo("CAT-" + uniqueId)
                                .nombre("Categoria Test")
                                .descripcion("Categoria creada para pruebas e2e")
                                .activo(true)
                                .build());

                
                LoginRequest loginRequest = LoginRequest.builder()
                                .correo(admin.getCorreo())
                                .contrasena("admin123")
                                .build();

                MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
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
                
                ProductoRequest newProduct = ProductoRequest.builder()
                                .codigo("E2E-001")
                                .nombre("Producto de Prueba E2E")
                                .descripcion("Descripción del producto de prueba")
                                .precio(new BigDecimal("99.99"))
                                .stock(100)
                                .stockCritico(10)
                                .categoriaId(categoriaDefault.getId())
                                .puntosLevelUp(200)
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

                when(fileStorageService.uploadFile(any(), any(), anyLong(), any(), any())).thenReturn("/uploads/e2e.jpg");

                MvcResult creation = mockMvc.perform(multipart("/api/v1/products")
                                .file(productoPart)
                                .file(imagenPart)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.codigo").value("E2E-001"))
                                .andReturn();

                JsonNode creationJson = objectMapper.readTree(creation.getResponse().getContentAsString());
                long productId = creationJson.get("id").asLong();

                
                mockMvc.perform(get("/api/v1/products")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[?(@.codigo=='E2E-001')]").exists());

                
                mockMvc.perform(get("/api/v1/products"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[?(@.codigo=='E2E-001')]").exists());

                mockMvc.perform(get("/api/v1/products/" + productId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(productId));
        }

        @Test
        void vendedorNoPuedeEliminarProductoDeOtroVendedor() throws Exception {
                String uniqueIdProducto = UUID.randomUUID().toString().substring(0, 8);

                ProductoRequest newProduct = ProductoRequest.builder()
                                .codigo("E2E-OWN-" + uniqueIdProducto)
                                .nombre("Producto Corporativo")
                                .descripcion("Producto sembrado para validación de ownership")
                                .precio(new BigDecimal("49.99"))
                                .stock(5)
                                .stockCritico(1)
                                .categoriaId(categoriaDefault.getId())
                                .puntosLevelUp(200)
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

                when(fileStorageService.uploadFile(any(), any(), anyLong(), any(), any())).thenReturn("/uploads/e2e.jpg");

                MvcResult creationResult = mockMvc.perform(multipart("/api/v1/products")
                                .file(productoPart)
                                .file(imagenPart)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode creationJson = objectMapper.readTree(creationResult.getResponse().getContentAsString());
                long productId = creationJson.get("id").asLong();

                String vendorPassword = "vend1234";
                Usuario vendor = Usuario.builder()
                                .run("77777777-7")
                                .nombre("Carlos")
                                .apellidos("Vendedor")
                                .correo("vendor-" + uniqueIdProducto + "@example.com")
                                .contrasena(passwordEncoder.encode(vendorPassword))
                                .fechaNacimiento(LocalDate.now().minusYears(25))
                                .roles(Set.of(RolUsuario.VENDEDOR))
                                .activo(true)
                                .build();
                usuarioRepository.save(vendor);

                LoginRequest vendorLogin = LoginRequest.builder()
                                .correo(vendor.getCorreo())
                                .contrasena(vendorPassword)
                                .rol(RolUsuario.VENDEDOR)
                                .build();

                String vendorToken = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(vendorLogin)))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                JsonNode vendorLoginJson = objectMapper.readTree(vendorToken);
                String vendorAccessToken = vendorLoginJson.get("accessToken").asText();

                mockMvc.perform(delete("/api/v1/products/" + productId)
                                .header("Authorization", "Bearer " + vendorAccessToken))
                                .andExpect(status().isForbidden());
        }
}
