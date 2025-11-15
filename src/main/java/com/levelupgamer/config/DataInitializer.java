package com.levelupgamer.config;

import com.levelupgamer.gamificacion.Puntos;
import com.levelupgamer.gamificacion.PuntosRepository;
import com.levelupgamer.usuarios.RolUsuario;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PuntosRepository puntosRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PuntosRepository puntosRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.puntosRepository = puntosRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            
            // 1. Usuario Admin
            HashSet<RolUsuario> adminRoles = new HashSet<>();
            adminRoles.add(RolUsuario.ADMINISTRADOR);
            adminRoles.add(RolUsuario.CLIENTE);
            Usuario admin = Usuario.builder()
                    .run("111111111")
                    .nombre("Admin")
                    .apellidos("User")
                    .correo("admin@levelupgamer.com")
                    .contrasena(passwordEncoder.encode("admin123"))
                    .fechaNacimiento(LocalDate.of(1990, 1, 1))
                    .region("Metropolitana")
                    .comuna("Santiago")
                    .direccion("Plaza de Armas 1")
                    .roles(adminRoles)
                    .activo(true)
                    .isDuocUser(false)
                    .build();
            usuarioRepository.save(admin);
            
            Puntos puntosAdmin = Puntos.builder()
                    .usuario(admin)
                    .puntosAcumulados(1000)
                    .build();
            puntosRepository.save(puntosAdmin);

            // 2. Usuario Cliente
            HashSet<RolUsuario> clienteRoles = new HashSet<>();
            clienteRoles.add(RolUsuario.CLIENTE);
            Usuario cliente = Usuario.builder()
                    .run("222222222")
                    .nombre("Cliente")
                    .apellidos("Leal")
                    .correo("cliente@levelupgamer.com")
                    .contrasena(passwordEncoder.encode("cliente123"))
                    .fechaNacimiento(LocalDate.of(1995, 5, 10))
                    .region("Valparaíso")
                    .comuna("Valparaíso")
                    .direccion("Avenida Siempre Viva 742")
                    .roles(clienteRoles)
                    .activo(true)
                    .isDuocUser(false)
                    .build();
            usuarioRepository.save(cliente);

            Puntos puntosCliente = Puntos.builder()
                    .usuario(cliente)
                    .puntosAcumulados(50)
                    .build();
            puntosRepository.save(puntosCliente);

            // 3. Usuario Duoc
            HashSet<RolUsuario> matiasRoles = new HashSet<>();
            matiasRoles.add(RolUsuario.CLIENTE);
            Usuario matias = Usuario.builder()
                    .run("333333333")
                    .nombre("Matias")
                    .apellidos("Gutierrez")
                    .correo("matias.gutierrez@duoc.cl")
                    .contrasena(passwordEncoder.encode("Matias123"))
                    .fechaNacimiento(LocalDate.parse("2000-10-20"))
                    .region("Valparaíso")
                    .comuna("Viña del Mar")
                    .direccion("Rodelillo, Calle 678")
                    .roles(matiasRoles)
                    .activo(true)
                    .isDuocUser(true)
                    .build();
            usuarioRepository.save(matias);
            // Matias empieza con 0 puntos, no se crea registro en Puntos.

            System.out.println(">>> Usuarios de prueba creados exitosamente! <<<");
        } else {
            System.out.println(">>> La base de datos ya contiene usuarios. No se crearon usuarios de prueba. <<<");
        }
    }
}
