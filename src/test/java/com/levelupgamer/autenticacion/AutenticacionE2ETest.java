package com.levelupgamer.autenticacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupgamer.usuarios.dto.UsuarioRegistroDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AutenticacionE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deberiaRegistrarUsuarioYLuegoHacerLogin() throws Exception {
        // --- 1. Probar el Registro ---
        UsuarioRegistroDTO newUser = UsuarioRegistroDTO.builder()
                .run("19812345-5") // RUT Válido
                .nombre("E2E")
                .apellidos("Tester")
                .correo("e2e.tester@gmail.com")
                .contrasena("pass12345")
                .fechaNacimiento(LocalDate.of(1999, 1, 1))
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("e2e.tester@gmail.com"));

        // --- 2. Probar el Login con el usuario recién creado ---
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setCorreo("e2e.tester@gmail.com");
        loginRequest.setContrasena("pass12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
