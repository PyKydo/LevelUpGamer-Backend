package com.levelupgamer.usuarios;

import com.levelupgamer.exception.UserAlreadyExistsException;
import com.levelupgamer.gamificacion.Puntos;
import com.levelupgamer.gamificacion.PuntosRepository;
import com.levelupgamer.gamificacion.PuntosService;
import com.levelupgamer.gamificacion.dto.PuntosDTO;
import com.levelupgamer.usuarios.dto.UsuarioRegistroDTO;
import com.levelupgamer.usuarios.dto.UsuarioRespuestaDTO;
import com.levelupgamer.usuarios.dto.UsuarioUpdateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PuntosRepository puntosRepository;

    @Mock
    private PuntosService puntosService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioRegistroDTO validDto;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        validDto = UsuarioRegistroDTO.builder()
                .run("19812345-2")
                .nombre("Juan")
                .apellidos("Perez")
                .correo("juan@gmail.com")
                .contrasena("abcd123")
                .fechaNacimiento(LocalDate.now().minusYears(25))
                .build();

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRun("198123452");
        usuario.setNombre("Juan");
        usuario.setApellidos("Perez");
        usuario.setCorreo("juan@gmail.com");
        usuario.setContrasena("hashed");
        usuario.setFechaNacimiento(LocalDate.now().minusYears(25));
        usuario.setRoles(Set.of(RolUsuario.CLIENTE));
        usuario.setActivo(true);
    }

    @Test
    void registrarUsuario_success() {
        when(usuarioRepository.existsByCorreo(validDto.getCorreo())).thenReturn(false);
        when(usuarioRepository.existsByRun(anyString())).thenReturn(false);
        when(passwordEncoder.encode(validDto.getContrasena())).thenReturn("hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L); 
            return u;
        });
        when(puntosRepository.save(any(Puntos.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioRespuestaDTO res = usuarioService.registrarUsuario(validDto);

        assertThat(res).isNotNull();
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario saved = captor.getValue();
        assertThat(saved.getContrasena()).isEqualTo("hashed");
        verify(puntosRepository).save(any(Puntos.class));
    }

    @Test
    void registrarUsuario_duplicateCorreo_throws() {
        when(usuarioRepository.existsByCorreo(validDto.getCorreo())).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> usuarioService.registrarUsuario(validDto));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrarUsuario_duplicateRun_throws() {
        when(usuarioRepository.existsByCorreo(validDto.getCorreo())).thenReturn(false);
        when(usuarioRepository.existsByRun(anyString())).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> usuarioService.registrarUsuario(validDto));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void buscarPorId_usuarioExistente_retornaOptionalConUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        Optional<Usuario> result = usuarioService.buscarPorId(1L);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(usuario);
    }

    @Test
    void actualizarUsuario_usuarioExistente_actualizaYRetornaDTO() {
        
        UsuarioUpdateDTO updateDto = UsuarioUpdateDTO.builder()
                .nombre("Juan Actualizado")
                .apellidos("Perez Actualizado")
                .build();
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(puntosService.obtenerPuntosPorUsuario(1L)).thenReturn(new PuntosDTO(1L, 100));

        
        UsuarioRespuestaDTO result = usuarioService.actualizarUsuario(1L, updateDto);

        
        assertThat(result).isNotNull();
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario saved = captor.getValue();
        assertThat(saved.getNombre()).isEqualTo("Juan Actualizado");
    }

    @Test
    void actualizarUsuario_usuarioNoExistente_lanzaExcepcion() {
        
        UsuarioUpdateDTO updateDto = new UsuarioUpdateDTO();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizarUsuario(1L, updateDto));
        verify(usuarioRepository, never()).save(any());
    }
}
