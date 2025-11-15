package com.levelupgamer.productos;

import com.levelupgamer.productos.dto.ProductoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductos() {
        return productoRepository.findAll().stream()
            .map(ProductoMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public ProductoDTO crearProducto(Producto producto) {
        if (productoRepository.existsByCodigo(producto.getCodigo())) {
            throw new IllegalArgumentException("Código de producto ya existe");
        }
        producto.setActivo(true);
        Producto guardado = productoRepository.save(producto);
        return ProductoMapper.toDTO(guardado);
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id).filter(Producto::getActivo);
    }

    public Optional<Producto> actualizarProducto(Long id, Producto nuevo) {
        return productoRepository.findById(id).map(producto -> {
            producto.setNombre(nuevo.getNombre());
            producto.setDescripcion(nuevo.getDescripcion());
            producto.setPrecio(nuevo.getPrecio());
            producto.setStock(nuevo.getStock());
            producto.setStockCritico(nuevo.getStockCritico());
            producto.setCategoria(nuevo.getCategoria());
            producto.setImagenes(nuevo.getImagenes());
            productoRepository.save(producto);
            return producto;
        });
    }

    public boolean eliminarProducto(Long id) {
        return productoRepository.findById(id).map(producto -> {
            producto.setActivo(false);
            productoRepository.save(producto);
            return true;
        }).orElse(false);
    }

    // Lógica de stock crítico (placeholder: loguear o lanzar excepción)
    public void verificarStockCritico(Producto producto) {
        if (producto.getStock() != null && producto.getStockCritico() != null && producto.getStock() <= producto.getStockCritico()) {
            // Aquí se podría notificar al admin (por email, log, etc.)
            System.out.println("ALERTA: Stock crítico para producto " + producto.getNombre());
        }
    }
}
