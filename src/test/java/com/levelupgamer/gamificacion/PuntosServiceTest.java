package com.levelupgamer.gamificacion;

import com.levelupgamer.gamificacion.dto.PuntosDTO;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
class PuntosServiceTest {

    @Mock
    private PuntosRepository puntosRepository;

    @Mock
    private UsuarioRepository usuarioRepository; 

    @Mock
    private MovimientoPuntosRepository movimientoPuntosRepository;

    @InjectMocks
    private PuntosService puntosService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(movimientoPuntosRepository.save(any(MovimientoPuntos.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void obtenerPuntosPorUsuario_conPuntosExistentes_retornaPuntosDTO() {
        
        Long usuarioId = 1L;
        Puntos puntos = Puntos.builder().usuarioId(usuarioId).puntosAcumulados(100).build();
        when(puntosRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(puntos));

        
        PuntosDTO result = puntosService.obtenerPuntosPorUsuario(usuarioId);

        
        assertNotNull(result);
        assertEquals(usuarioId, result.getUsuarioId());
        assertEquals(100, result.getPuntosAcumulados());
    }

    @Test
    void obtenerPuntosPorUsuario_sinPuntosExistentes_retornaPuntosDTOCero() {
        
        Long usuarioId = 1L;
        when(puntosRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.empty());

        
        PuntosDTO result = puntosService.obtenerPuntosPorUsuario(usuarioId);

        
        assertNotNull(result);
        assertEquals(usuarioId, result.getUsuarioId());
        assertEquals(0, result.getPuntosAcumulados());
    }

    @Test
    void sumarPuntos_usuarioNuevo_creaPuntosYAsigna() {
        
        Long usuarioId = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        PuntosDTO puntosDTO = new PuntosDTO(usuarioId, 50);

        when(puntosRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario)); 
        
        when(puntosRepository.save(any(Puntos.class))).thenAnswer(invocation -> {
            Puntos p = invocation.getArgument(0);
            p.setUsuarioId(usuarioId);
            return p;
        });

        
        PuntosDTO result = puntosService.sumarPuntos(puntosDTO);

        
        assertNotNull(result);
        assertEquals(usuarioId, result.getUsuarioId());
        assertEquals(50, result.getPuntosAcumulados());
        verify(puntosRepository, times(1)).save(any(Puntos.class));
    }

    @Test
    void sumarPuntos_usuarioExistente_actualizaPuntos() {
        
        Long usuarioId = 1L;
        PuntosDTO puntosDTO = new PuntosDTO(usuarioId, 50);
        Puntos puntosExistentes = Puntos.builder().usuarioId(usuarioId).puntosAcumulados(100).build();
        when(puntosRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(puntosExistentes));
        when(puntosRepository.save(any(Puntos.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        PuntosDTO result = puntosService.sumarPuntos(puntosDTO);

        
        assertNotNull(result);
        assertEquals(usuarioId, result.getUsuarioId());
        assertEquals(150, result.getPuntosAcumulados());
        verify(puntosRepository, times(1)).save(puntosExistentes);
    }

    @Test
    void canjearPuntos_conPuntosSuficientes_actualizaPuntos() {
        
        Long usuarioId = 1L;
        PuntosDTO puntosDTO = new PuntosDTO(usuarioId, 50);
        Puntos puntosExistentes = Puntos.builder().usuarioId(usuarioId).puntosAcumulados(100).build();
        when(puntosRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(puntosExistentes));
        when(puntosRepository.save(any(Puntos.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        PuntosDTO result = puntosService.canjearPuntos(puntosDTO);

        
        assertNotNull(result);
        assertEquals(usuarioId, result.getUsuarioId());
        assertEquals(50, result.getPuntosAcumulados());
        verify(puntosRepository, times(1)).save(puntosExistentes);
    }

    @Test
    void canjearPuntos_sinPuntosSuficientes_lanzaExcepcion() {
        
        Long usuarioId = 1L;
        PuntosDTO puntosDTO = new PuntosDTO(usuarioId, 150);
        Puntos puntosExistentes = Puntos.builder().usuarioId(usuarioId).puntosAcumulados(100).build();
        when(puntosRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(puntosExistentes));

        
        assertThrows(IllegalArgumentException.class, () -> puntosService.canjearPuntos(puntosDTO));
        verify(puntosRepository, never()).save(any(Puntos.class));
    }

    @Test
    void canjearPuntos_usuarioSinPuntos_lanzaExcepcion() {
        
        Long usuarioId = 1L;
        PuntosDTO puntosDTO = new PuntosDTO(usuarioId, 50);
        when(puntosRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.empty());

        
        assertThrows(IllegalArgumentException.class, () -> puntosService.canjearPuntos(puntosDTO));
        verify(puntosRepository, never()).save(any(Puntos.class));
    }
}
