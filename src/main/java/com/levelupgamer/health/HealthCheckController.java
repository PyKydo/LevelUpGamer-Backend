package com.levelupgamer.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;
import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping("/")
    public Map<String, String> healthCheck() {
        return Collections.singletonMap("status", "El backend de LevelUpGamer está funcionando correctamente!");
    }
}
