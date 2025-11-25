package com.levelupgamer.productos;

import com.levelupgamer.common.storage.FileStorageService;
import com.levelupgamer.productos.dto.ProductoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {
    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductos() {
        return productoRepository.findAll().stream()
            .map(ProductoMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public ProductoDTO crearProducto(Producto producto, MultipartFile imagen) throws IOException {
        if (productoRepository.existsByCodigo(producto.getCodigo())) {
            throw new IllegalArgumentException("Código de producto ya existe");
        }

        producto.setPuntosLevelUp(normalizarPuntos(producto.getPuntosLevelUp()));

        if (imagen != null && !imagen.isEmpty()) {
            String imageUrl = fileStorageService.uploadFile(
                    imagen.getInputStream(),
                    imagen.getOriginalFilename(),
                    imagen.getSize());
            producto.setImagenes(Collections.singletonList(imageUrl));
        } else if (producto.getImagenes() == null) {
            producto.setImagenes(Collections.emptyList());
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
            producto.setPuntosLevelUp(normalizarPuntos(nuevo.getPuntosLevelUp()));
            
            
            productoRepository.save(producto);
            return producto;
        });
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarDestacados() {
        return productoRepository.findTop5ByActivoTrueOrderByPuntosLevelUpDesc().stream()
                .map(ProductoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public boolean eliminarProducto(Long id) {
        return productoRepository.findById(id).map(producto -> {
            producto.setActivo(false);
            productoRepository.save(producto);
            return true;
        }).orElse(false);
    }

    public void verificarStockCritico(Producto producto) {
        if (producto.getStock() != null && producto.getStockCritico() != null && producto.getStock() <= producto.getStockCritico()) {
            System.out.println("ALERTA: Stock crítico para producto " + producto.getNombre());
        }
    }

    private int normalizarPuntos(Integer puntos) {
        if (puntos == null) {
            return 0;
        }
        if (puntos < 0 || puntos > 1000 || puntos % 100 != 0) {
            throw new IllegalArgumentException("puntosLevelUp debe estar entre 0 y 1000 en incrementos de 100");
        }
        return puntos;
    }
}
