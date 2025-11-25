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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                
                .requestMatchers(HttpMethod.GET, "/").permitAll() 
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll() 
                .requestMatchers("/api/v1/blog-posts/**", "/api/v1/contact-messages/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                
                
                .requestMatchers("/api/v1/users/roles").hasRole("ADMINISTRADOR")

                
                .requestMatchers(HttpMethod.GET, "/api/v1/users/{id}").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/users/{id}").authenticated()

                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").hasAnyRole("ADMINISTRADOR", "CLIENTE", "VENDEDOR")
                .requestMatchers("/api/v1/products/**").hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").hasAnyRole("ADMINISTRADOR", "CLIENTE", "VENDEDOR")
                .requestMatchers("/api/v1/categories/**").hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.GET, "/api/v1/boletas/**").hasAnyRole("ADMINISTRADOR", "CLIENTE", "VENDEDOR")
                .requestMatchers("/api/v1/boletas/**").hasAnyRole("ADMINISTRADOR", "CLIENTE")
                .requestMatchers("/api/v1/points/**").authenticated()

                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
