package com.levelupgamer.productos;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.levelupgamer.boletas.BoletaRepository;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BoletaRepository boletaRepository;

    @InjectMocks
    private ResenaService resenaService;

    private Producto producto;
    private Usuario usuario;
    private Usuario vendedor;

    @BeforeEach
    void init() {
        producto = new Producto();
        producto.setId(10L);

        vendedor = new Usuario();
        vendedor.setId(99L);
        vendedor.setNombre("Vendor Test");

        producto.setVendedor(vendedor);

        usuario = Usuario.builder()
                .id(5L)
                .nombre("Test")
                .apellidos("User")
                .build();
    }

    @Test
    void crearResena_conCompra_prevalida() {
        Resena resena = new Resena();
        resena.setTexto("Excelente");
        resena.setCalificacion(5);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(boletaRepository.existsByUsuarioIdAndDetallesProductoId(5L, 10L)).thenReturn(true);
        when(resenaRepository.save(resena)).thenReturn(resena);

        Resena guardada = resenaService.crearResena(10L, 5L, resena);

        assertTrue(guardada.getProducto() != null);
        verify(resenaRepository, times(1)).save(resena);
    }

    @Test
    void crearResena_sinCompra_rechaza() {
        Resena resena = new Resena();
        resena.setTexto("No compré");
        resena.setCalificacion(3);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(boletaRepository.existsByUsuarioIdAndDetallesProductoId(5L, 10L)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> resenaService.crearResena(10L, 5L, resena));
        verifyNoInteractions(resenaRepository);
    }
}
