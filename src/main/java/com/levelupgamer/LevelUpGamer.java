package com.levelupgamer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@OpenAPIDefinition(
    info = @Info(
        title = "LevelUpGamer API",
        version = "1.0",
        description = "Documentación OpenAPI para el backend de LevelUpGamer"
    )
)
@SpringBootApplication
@EnableJpaAuditing
public class LevelUpGamer {

	public static void main(String[] args) {
		SpringApplication.run(LevelUpGamer.class, args);
	}

}
