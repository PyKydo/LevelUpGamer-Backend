package com.levelupgamer.autenticacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Autowired
    private JwtAutenticacionFilter jwtAuthenticationFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints Públicos
                .requestMatchers("/api/auth/**", "/api/users/register", "/api/blog-posts/**", "/api/contact-messages/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                
                // Endpoints de Administrador
                .requestMatchers("/api/users/roles").hasRole("ADMINISTRADOR")

                // Endpoints para cualquier usuario autenticado (CLIENTE, VENDEDOR, ADMIN)
                .requestMatchers(HttpMethod.GET, "/api/users/{id}").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/{id}").authenticated()
                .requestMatchers("/api/products/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR", "CLIENTE")
                .requestMatchers("/api/orders/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR", "CLIENTE")
                .requestMatchers("/api/points/**").authenticated()

                // Todas las demás peticiones requieren autenticación
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
