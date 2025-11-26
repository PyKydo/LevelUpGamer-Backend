package com.levelupgamer.productos;

import com.levelupgamer.boletas.BoletaRepository;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResenaService {
    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BoletaRepository boletaRepository;

    @Transactional
    @SuppressWarnings("null")
    public Resena crearResena(Long productoId, Long usuarioId, Resena resena) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        boolean comproProducto = boletaRepository.existsByUsuarioIdAndDetallesProductoId(usuarioId, productoId);
        if (!comproProducto) {
            throw new IllegalStateException("Solo puedes reseñar productos que ya compraste");
        }

        resena.setProducto(producto);
        resena.setUsuario(usuario);

        return resenaRepository.save(resena);
    }

    @Transactional(readOnly = true)
    public List<Resena> listarResenasPorProducto(Long productoId) {
        return resenaRepository.findByProductoId(productoId);
    }

    @Transactional
    @SuppressWarnings("null")
    public void eliminarResena(Long resenaId, Long usuarioId, boolean esAdmin) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada"));

        if (!esAdmin && !resena.getUsuario().getId().equals(usuarioId)) {
            throw new AccessDeniedException("No puedes eliminar reseñas de otros usuarios");
        }

        resenaRepository.delete(resena);
    }
}
