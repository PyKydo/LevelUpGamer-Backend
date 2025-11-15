package com.levelupgamer.autenticacion;

import com.levelupgamer.usuarios.RolUsuario;
import com.levelupgamer.usuarios.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtProvider {
    // La clave ahora tiene 64 caracteres para cumplir con el requisito de 512 bits de HS512.
    private final String jwtSecret = "levelupgamerSecretKey123!levelupgamerSecretKey123!MoreBytesNeeded!!";
    private final long jwtExpirationMs = 86400000; // 1 día

    private Key key;

    @PostConstruct
    public void init() {
        // Convierte la clave secreta en un objeto Key para usarlo con jjwt
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getCorreo())
                .claim("roles", usuario.getRoles().stream().map(RolUsuario::name).collect(Collectors.toList()))
                .claim("usuarioId", usuario.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Aquí podrías loguear el error específico (e.g., token expirado, firma inválida)
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
