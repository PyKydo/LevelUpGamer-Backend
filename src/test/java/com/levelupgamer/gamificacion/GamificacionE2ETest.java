package com.levelupgamer.gamificacion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupgamer.autenticacion.LoginRequest;
import com.levelupgamer.gamificacion.dto.PuntosDTO;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class GamificacionE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private String clienteToken;
    private Long clienteId;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        // Crear el usuario cliente para la prueba
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        Usuario cliente = Usuario.builder()
                .run("44" + uniqueId)
                .nombre("Gamificado")
                .apellidos("Test")
                .correo("gami-" + uniqueId + "@gmail.com")
                .contrasena(passwordEncoder.encode("gami123"))
                .fechaNacimiento(LocalDate.now().minusYears(22))
                .roles(Set.of(RolUsuario.CLIENTE))
                .activo(true)
                .build();
        usuarioRepository.saveAndFlush(cliente);
        clienteId = cliente.getId();

        // Iniciar sesión como cliente
        LoginRequest loginRequest = new LoginRequest(cliente.getCorreo(), "gami123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        clienteToken = root.get("token").asText();
    }

    @Test
    void deberiaObtenerYModificarPuntosDeUsuario() throws Exception {
        // 1. Obtener puntos iniciales (deberían ser 0)
        mockMvc.perform(get("/api/points/" + clienteId)
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntosAcumulados").value(0));

        // 2. Sumar puntos
        PuntosDTO earnPointsDTO = new PuntosDTO(clienteId, 100);
        mockMvc.perform(post("/api/points/earn")
                        .header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(earnPointsDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntosAcumulados").value(100));

        // 3. Canjear puntos
        PuntosDTO redeemPointsDTO = new PuntosDTO(clienteId, 30);
        mockMvc.perform(post("/api/points/redeem")
                        .header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(redeemPointsDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntosAcumulados").value(70));
    }
}
