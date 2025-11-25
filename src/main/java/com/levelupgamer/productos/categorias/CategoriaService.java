package com.levelupgamer.productos.categorias;

import com.levelupgamer.productos.dto.CategoriaDTO;
import com.levelupgamer.productos.dto.CategoriaRequest;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional
    public CategoriaDTO crear(CategoriaRequest request) {
        Categoria categoria = Categoria.builder()
                .codigo(normalizarCodigo(request.getCodigo()))
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .activo(request.getActivo() == null ? true : request.getActivo())
                .build();

        validarCodigoUnico(categoria.getCodigo());
        validarNombreUnico(categoria.getNombre());

        return CategoriaMapper.toDTO(categoriaRepository.save(categoria));
    }

    @Transactional
    public Optional<CategoriaDTO> actualizar(Long id, CategoriaRequest request) {
        return categoriaRepository.findById(id).map(existing -> {
            if (request.getCodigo() != null) {
                String codigo = normalizarCodigo(request.getCodigo());
                if (!codigo.equalsIgnoreCase(existing.getCodigo())) {
                    validarCodigoUnico(codigo);
                    existing.setCodigo(codigo);
                }
            }
            if (request.getNombre() != null) {
                String nombre = request.getNombre().trim();
                if (!nombre.equalsIgnoreCase(existing.getNombre())) {
                    validarNombreUnico(nombre);
                    existing.setNombre(nombre);
                }
            }
            if (request.getDescripcion() != null) {
                existing.setDescripcion(request.getDescripcion());
            }
            if (request.getActivo() != null) {
                existing.setActivo(request.getActivo());
            }
            return CategoriaMapper.toDTO(categoriaRepository.save(existing));
        });
    }

    @Transactional
    public boolean desactivar(Long id) {
        return categoriaRepository.findById(id).map(categoria -> {
            categoria.setActivo(false);
            categoriaRepository.save(categoria);
            return true;
        }).orElse(false);
    }

    @Transactional
    public Categoria asegurarActiva(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .filter(Categoria::getActivo)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada o inactiva"));
    }

    @Transactional
    public Categoria asegurarPorCodigo(String codigo) {
        return categoriaRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
    }

    @Transactional
    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public List<CategoriaDTO> listar(boolean includeInactive) {
        return categoriaRepository.findAll().stream()
                .filter(cat -> includeInactive || Boolean.TRUE.equals(cat.getActivo()))
                .map(CategoriaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<CategoriaDTO> buscarPorId(Long id) {
        return categoriaRepository.findById(id).map(CategoriaMapper::toDTO);
    }

    private void validarCodigoUnico(String codigo) {
        if (categoriaRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new IllegalArgumentException("El código de la categoría ya existe");
        }
    }

    private void validarNombreUnico(String nombre) {
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("El nombre de la categoría ya existe");
        }
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null ? null : codigo.trim().toUpperCase();
    }
}
