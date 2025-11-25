package com.levelupgamer.boletas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.levelupgamer.boletas.dto.BoletaCrearDTO;
import com.levelupgamer.boletas.dto.BoletaDetalleCrearDTO;
import com.levelupgamer.boletas.dto.BoletaRespuestaDTO;
import com.levelupgamer.gamificacion.PuntosService;
import com.levelupgamer.gamificacion.cupones.Cupon;
import com.levelupgamer.gamificacion.cupones.CuponService;
import com.levelupgamer.gamificacion.cupones.EstadoCupon;
import com.levelupgamer.gamificacion.dto.PuntosDTO;
import com.levelupgamer.productos.Producto;
import com.levelupgamer.productos.ProductoRepository;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("null")
class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PuntosService puntosService;

    @Mock
    private CuponService cuponService;

    @InjectMocks
    private BoletaService boletaService;

    private Usuario usuario;
    private Producto producto;
    private BoletaCrearDTO boletaCrearDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCorreo("test@example.com");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto Test");
        producto.setPrecio(new BigDecimal("100.00"));
        producto.setStock(10);
        producto.setStockCritico(5);
        producto.setPuntosLevelUp(100);

        BoletaDetalleCrearDTO detalle = new BoletaDetalleCrearDTO();
        detalle.setProductoId(1L);
        detalle.setCantidad(2);

        boletaCrearDTO = BoletaCrearDTO.builder()
                .usuarioId(1L)
                .detalles(Collections.singletonList(detalle))
                .build();
    }

    @Test
    void crearBoleta_conDatosValidos_creaBoletaYActualizaStock() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BoletaRespuestaDTO result = boletaService.crearBoletaInterna(boletaCrearDTO);

        assertNotNull(result);
        assertEquals(new BigDecimal("200.00"), result.getTotalAntesDescuentos());
        assertEquals(new BigDecimal("200.00"), result.getTotal());
        assertEquals(8, producto.getStock());

        verify(puntosService, times(1)).sumarPuntos(new PuntosDTO(1L, 200));
        verify(productoRepository, times(1)).save(producto);
        verify(boletaRepository, times(1)).save(any(Boleta.class));
        verifyNoInteractions(cuponService);
    }

    @Test
    void crearBoleta_conUsuarioDuoc_aplicaDescuento() {
        usuario.setCorreo("cliente@duoc.cl");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BoletaRespuestaDTO result = boletaService.crearBoletaInterna(boletaCrearDTO);

        assertNotNull(result);
        assertEquals(new BigDecimal("200.00"), result.getTotalAntesDescuentos());
        assertEquals(new BigDecimal("160.00"), result.getTotal());
        assertEquals(20, result.getDescuentoDuoc());

        verify(puntosService, times(1)).sumarPuntos(new PuntosDTO(1L, 200));
    }

    @Test
    void crearBoleta_conCupon_aplicaDescuentoYLoMarcaUsado() {
        Cupon cupon = new Cupon();
        cupon.setId(99L);
        cupon.setPorcentajeDescuento(10);
        cupon.setEstado(EstadoCupon.ACTIVO);

        boletaCrearDTO.setCuponId(99L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(cuponService.buscarCuponValido(1L, 99L, null)).thenReturn(Optional.of(cupon));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BoletaRespuestaDTO result = boletaService.crearBoletaInterna(boletaCrearDTO);

        assertEquals(new BigDecimal("200.00"), result.getTotalAntesDescuentos());
        assertEquals(new BigDecimal("180.00"), result.getTotal());
        assertEquals(10, result.getDescuentoCupon());
        assertEquals(0, result.getDescuentoDuoc());
        verify(cuponService).buscarCuponValido(1L, 99L, null);
        verify(cuponService).marcarComoUsado(cupon);
    }

    @Test
    void crearBoleta_conStockInsuficiente_lanzaExcepcion() {
        producto.setStock(1);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThrows(IllegalArgumentException.class, () -> boletaService.crearBoletaInterna(boletaCrearDTO));
        verify(boletaRepository, never()).save(any(Boleta.class));
        verify(puntosService, never()).sumarPuntos(any());
    }
}
