package com.levelupgamer.productos;

import com.levelupgamer.common.storage.FileStorageService;
import com.levelupgamer.productos.categorias.Categoria;
import com.levelupgamer.productos.categorias.CategoriaRepository;
import com.levelupgamer.productos.dto.ProductoDTO;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final FileStorageService fileStorageService;
    private final CategoriaRepository categoriaRepository;

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

        Categoria categoria = resolverCategoria(producto.getCategoria());
        producto.setPuntosLevelUp(normalizarPuntos(producto.getPuntosLevelUp()));
        producto.setCategoria(categoria);

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
            Categoria categoria = null;
            if (nuevo.getCategoria() != null && nuevo.getCategoria().getId() != null) {
                categoria = resolverCategoria(nuevo.getCategoria());
            }
            if (categoria != null) {
                producto.setCategoria(categoria);
            }
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

    private Categoria resolverCategoria(Categoria categoriaPayload) {
        if (categoriaPayload == null || categoriaPayload.getId() == null) {
            throw new IllegalArgumentException("Debe especificar una categoría válida (categoria.id)");
        }
        return categoriaRepository.findById(categoriaPayload.getId())
                .filter(Categoria::getActivo)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada o inactiva"));
    }
}
