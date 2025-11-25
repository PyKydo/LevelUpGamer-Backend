package com.levelupgamer.autenticacion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupgamer.autenticacion.dto.ChangePasswordRequest;
import com.levelupgamer.usuarios.dto.UsuarioRegistroDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
        
        UsuarioRegistroDTO newUser = UsuarioRegistroDTO.builder()
                .run("11111111-1") 
                .nombre("E2E")
                .apellidos("Tester")
                .correo("e2e.tester@gmail.com")
                .contrasena("pass12345")
                .fechaNacimiento(LocalDate.of(1999, 1, 1))
                .region("Metropolitana")
                .comuna("Santiago")
                .direccion("Calle Falsa 123")
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("e2e.tester@gmail.com"));

        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setCorreo("e2e.tester@gmail.com");
        loginRequest.setContrasena("pass12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void deberiaPermitirCambiarContrasenaConTokenValido() throws Exception {
        
        UsuarioRegistroDTO newUser = UsuarioRegistroDTO.builder()
                .run("11111111-1")
                .nombre("Cambio")
                .apellidos("Password")
                .correo("change.pass@gmail.com")
                .contrasena("pass1234")
                .fechaNacimiento(LocalDate.of(1995, 1, 1))
                .region("Metropolitana")
                .comuna("Santiago")
                .direccion("Calle Verdadera 123")
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());

        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setCorreo("change.pass@gmail.com");
        loginRequest.setContrasena("pass1234");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponseBody);
        String accessToken = loginJson.get("accessToken").asText();

        
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("pass1234")
                .newPassword("nueva123")
                .build();

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk());

        
        LoginRequest loginConAntigua = new LoginRequest();
        loginConAntigua.setCorreo("change.pass@gmail.com");
        loginConAntigua.setContrasena("pass1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginConAntigua)))
                .andExpect(status().is4xxClientError());

        
        LoginRequest loginConNueva = new LoginRequest();
        loginConNueva.setCorreo("change.pass@gmail.com");
        loginConNueva.setContrasena("nueva123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginConNueva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void noDeberiaPermitirCambiarContrasenaConActualIncorrecta() throws Exception {
        
        UsuarioRegistroDTO newUser = UsuarioRegistroDTO.builder()
                .run("11111111-1")
                .nombre("Cambio2")
                .apellidos("Password2")
                .correo("change.pass2@gmail.com")
                .contrasena("pass1234")
                .fechaNacimiento(LocalDate.of(1995, 1, 1))
                .region("Metropolitana")
                .comuna("Santiago")
                .direccion("Otra Calle 123")
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());

        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setCorreo("change.pass2@gmail.com");
        loginRequest.setContrasena("pass1234");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponseBody);
        String accessToken = loginJson.get("accessToken").asText();

        
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("incorrecta")
                .newPassword("nueva123")
                .build();

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void noDeberiaPermitirCambiarContrasenaConNuevaInvalida() throws Exception {
        
        UsuarioRegistroDTO newUser = UsuarioRegistroDTO.builder()
                .run("11111111-1")
                .nombre("Cambio3")
                .apellidos("Password3")
                .correo("change.pass3@gmail.com")
                .contrasena("pass1234")
                .fechaNacimiento(LocalDate.of(1995, 1, 1))
                .region("Metropolitana")
                .comuna("Santiago")
                .direccion("Otra Calle 456")
                .build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());

        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setCorreo("change.pass3@gmail.com");
        loginRequest.setContrasena("pass1234");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponseBody);
        String accessToken = loginJson.get("accessToken").asText();

        
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("pass1234")
                .newPassword("123")
                .build();

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest());
    }
}
