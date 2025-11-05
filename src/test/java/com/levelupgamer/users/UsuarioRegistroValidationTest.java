package com.levelupgamer.users;

import com.levelupgamer.users.dto.UsuarioRegistroDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UsuarioRegistroValidationTest {
    @Autowired
    private Validator validator;

    @Test
    void validDto_shouldHaveNoViolations() {
        UsuarioRegistroDTO dto = UsuarioRegistroDTO.builder()
                .run("19011022K")
                .nombre("Juan")
                .apellidos("Perez")
                .correo("juan@gmail.com")
                .contrasena("abcd")
                .fechaNacimiento(LocalDate.now().minusYears(25))
                .region("Region")
                .comuna("Comuna")
                .direccion("Direccion 123")
                .build();

        Set<ConstraintViolation<UsuarioRegistroDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void invalidRun_shouldFail() {
        UsuarioRegistroDTO dto = UsuarioRegistroDTO.builder()
                .run("19.011022-K")
                .nombre("Juan")
                .apellidos("Perez")
                .correo("juan@gmail.com")
                .contrasena("abcd")
                .fechaNacimiento(LocalDate.now().minusYears(25))
                .region("Region")
                .comuna("Comuna")
                .direccion("Direccion 123")
                .build();

        Set<ConstraintViolation<UsuarioRegistroDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("run"));
    }

    @Test
    void invalidEmailDomain_shouldFail() {
        UsuarioRegistroDTO dto = UsuarioRegistroDTO.builder()
                .run("19011022K")
                .nombre("Juan")
                .apellidos("Perez")
                .correo("juan@hotmail.com")
                .contrasena("abcd")
                .fechaNacimiento(LocalDate.now().minusYears(25))
                .region("Region")
                .comuna("Comuna")
                .direccion("Direccion 123")
                .build();

        Set<ConstraintViolation<UsuarioRegistroDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("correo"));
    }

    @Test
    void underage_shouldFail() {
        UsuarioRegistroDTO dto = UsuarioRegistroDTO.builder()
                .run("19011022K")
                .nombre("Juan")
                .apellidos("Perez")
                .correo("juan@gmail.com")
                .contrasena("abcd")
                .fechaNacimiento(LocalDate.now().minusYears(16))
                .region("Region")
                .comuna("Comuna")
                .direccion("Direccion 123")
                .build();

        Set<ConstraintViolation<UsuarioRegistroDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fechaNacimiento"));
    }
}
