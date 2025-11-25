package com.levelupgamer.usuarios;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupgamer.autenticacion.LoginRequest;
import com.levelupgamer.usuarios.dto.UsuarioUpdateDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UsuarioE2ETest {

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
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                Usuario cliente = Usuario.builder()
                                .run("33" + uniqueId)
                                .nombre("Usuario")
                                .apellidos("E2E")
                                .correo("usuario-e2e-" + uniqueId + "@gmail.com")
                                .contrasena(passwordEncoder.encode("user12345"))
                                .fechaNacimiento(LocalDate.now().minusYears(25))
                                .region("Metropolitana")
                                .comuna("Santiago")
                                .direccion("Calle Test 123")
                                .roles(Set.of(RolUsuario.CLIENTE))
                                .activo(true)
                                .build();
                usuarioRepository.saveAndFlush(cliente);
                clienteId = cliente.getId();

                LoginRequest loginRequest = LoginRequest.builder()
                                .correo(cliente.getCorreo())
                                .contrasena("user12345")
                                .build();
                MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andReturn();
                JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
                clienteToken = root.get("accessToken").asText();
        }

        @Test
        void deberiaObtenerYActualizarPerfilDeUsuario() throws Exception {
                mockMvc.perform(get("/api/v1/users/" + clienteId)
                                .header("Authorization", "Bearer " + clienteToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(clienteId));

                UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
                                .nombre("Usuario Actualizado")
                                .apellidos("E2E Actualizado")
                                .direccion("Nueva Direccion 123")
                                .region("Metropolitana")
                                .comuna("Providencia")
                                .build();

                mockMvc.perform(put("/api/v1/users/" + clienteId)
                                .header("Authorization", "Bearer " + clienteToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nombre").value("Usuario Actualizado"))
                                .andExpect(jsonPath("$.direccion").value("Nueva Direccion 123"));
        }
}
