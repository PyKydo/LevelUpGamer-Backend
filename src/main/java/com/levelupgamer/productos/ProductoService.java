package com.levelupgamer.productos;

import com.levelupgamer.common.S3Service;
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
    private S3Service s3Service;

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

        // Subir la imagen a S3 y obtener la URL
        String imageUrl = s3Service.uploadFile(imagen.getInputStream(), imagen.getOriginalFilename(), imagen.getSize());
        producto.setImagenes(Collections.singletonList(imageUrl));

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
            // La actualización de imágenes requeriría un endpoint separado
            // producto.setImagenes(nuevo.getImagenes());
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

    public void verificarStockCritico(Producto producto) {
        if (producto.getStock() != null && producto.getStockCritico() != null && producto.getStock() <= producto.getStockCritico()) {
            System.out.println("ALERTA: Stock crítico para producto " + producto.getNombre());
        }
    }
}
