package com.levelupgamer.productos;

import com.levelupgamer.common.storage.FileStorageService;
import com.levelupgamer.productos.categorias.Categoria;
import com.levelupgamer.productos.categorias.CategoriaRepository;
import com.levelupgamer.productos.dto.ProductoDTO;
import com.levelupgamer.productos.dto.ProductoRequest;
import com.levelupgamer.usuarios.RolUsuario;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProductoService {

    private static final String PRODUCT_IMAGE_FOLDER = "products";
    private final ProductoRepository productoRepository;
    private final FileStorageService fileStorageService;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        boolean isAdmin = isAuthenticated && hasRole(authentication, RolUsuario.ADMINISTRADOR);
        boolean isVendor = isAuthenticated && hasRole(authentication, RolUsuario.VENDEDOR);

        List<Producto> productos;
        if (isAuthenticated && isVendor && !isAdmin) {
            Usuario vendedor = resolveCurrentUser(authentication);
            productos = productoRepository.findAllByVendedorId(vendedor.getId());
        } else {
            productos = productoRepository.findAll();
        }

        return productos.stream()
                .map(ProductoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoDTO crearProducto(ProductoRequest request, MultipartFile imagen) throws IOException {
        Authentication authentication = requireAuthentication();
        boolean isAdmin = hasRole(authentication, RolUsuario.ADMINISTRADOR);
        boolean isVendor = hasRole(authentication, RolUsuario.VENDEDOR);

        if (!isAdmin && !isVendor) {
            throw new AccessDeniedException("Solo administradores o vendedores pueden registrar productos");
        }

        if (request.getCodigo() == null || request.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El código del producto es obligatorio");
        }

        if (productoRepository.existsByCodigo(request.getCodigo())) {
            throw new IllegalArgumentException("Código de producto ya existe");
        }

        Producto producto = mapearEntidadDesdeRequest(request);
        producto.setPuntosLevelUp(normalizarPuntos(request.getPuntosLevelUp()));
        producto.setCategoria(resolverCategoria(request.getCategoriaId()));
        producto.setVendedor(resolveCurrentUser(authentication));

        if (imagen != null && !imagen.isEmpty()) {
                String imageUrl = fileStorageService.uploadFile(
                    imagen.getInputStream(),
                    imagen.getOriginalFilename(),
                    imagen.getSize(),
                    PRODUCT_IMAGE_FOLDER,
                    imagen.getContentType());
            producto.setImagenes(Collections.singletonList(imageUrl));
        } else if (producto.getImagenes() == null) {
            producto.setImagenes(Collections.emptyList());
        }

        producto.setActivo(true);
        Producto guardado = productoRepository.save(producto);
        return ProductoMapper.toDTO(guardado);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id).filter(Producto::getActivo);
    }

    @Transactional
    public Optional<Producto> actualizarProducto(Long id, ProductoRequest request) {
        Authentication authentication = requireAuthentication();
        Usuario usuarioActual = resolveCurrentUser(authentication);
        boolean isAdmin = hasRole(authentication, RolUsuario.ADMINISTRADOR);
        boolean isVendor = hasRole(authentication, RolUsuario.VENDEDOR);

        return productoRepository.findById(id).map(producto -> {
            validarPermisosSobreProducto(producto, usuarioActual, isAdmin, isVendor);
            producto.setNombre(request.getNombre());
            producto.setDescripcion(request.getDescripcion());
            producto.setPrecio(request.getPrecio());
            producto.setStock(request.getStock());
            producto.setStockCritico(request.getStockCritico());
            if (request.getCategoriaId() != null) {
                Categoria categoria = resolverCategoria(request.getCategoriaId());
                producto.setCategoria(categoria);
            }
            producto.setPuntosLevelUp(normalizarPuntos(request.getPuntosLevelUp()));
            if (request.getImagenes() != null) {
                producto.setImagenes(request.getImagenes());
            }
            
            
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

    @Transactional
    public boolean eliminarProducto(Long id) {
        Authentication authentication = requireAuthentication();
        Usuario usuarioActual = resolveCurrentUser(authentication);
        boolean isAdmin = hasRole(authentication, RolUsuario.ADMINISTRADOR);
        boolean isVendor = hasRole(authentication, RolUsuario.VENDEDOR);

        return productoRepository.findById(id).map(producto -> {
            validarPermisosSobreProducto(producto, usuarioActual, isAdmin, isVendor);
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

    private Categoria resolverCategoria(Long categoriaId) {
        if (categoriaId == null) {
            throw new IllegalArgumentException("Debe especificar una categoría válida (categoriaId)");
        }
        return categoriaRepository.findById(categoriaId)
                .filter(Categoria::getActivo)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada o inactiva"));
    }

    private Producto mapearEntidadDesdeRequest(ProductoRequest request) {
        Producto producto = new Producto();
        producto.setCodigo(request.getCodigo());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setStockCritico(request.getStockCritico());
        producto.setImagenes(request.getImagenes());
        producto.setActivo(true);
        return producto;
    }

    private void validarPermisosSobreProducto(Producto producto, Usuario usuarioActual, boolean isAdmin, boolean isVendor) {
        if (isAdmin) {
            return;
        }

        if (!isVendor) {
            throw new AccessDeniedException("No cuenta con permisos sobre este producto");
        }

        if (esProductoCorporativo(producto)) {
            throw new AccessDeniedException("Los productos corporativos (LevelUp) sólo pueden ser gestionados por administradores");
        }

        if (!producto.getVendedor().getId().equals(usuarioActual.getId())) {
            throw new AccessDeniedException("No puede modificar productos de otro vendedor");
        }
    }

    private boolean esProductoCorporativo(Producto producto) {
        Usuario vendedor = producto.getVendedor();
        return vendedor != null && vendedor.getRoles() != null
                && vendedor.getRoles().contains(RolUsuario.ADMINISTRADOR)
                && !vendedor.getRoles().contains(RolUsuario.VENDEDOR);
    }

    private Authentication requireAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No hay un usuario autenticado en el contexto");
        }
        return authentication;
    }

    private Usuario resolveCurrentUser(Authentication authentication) {
        String correo = authentication.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado"));
    }

    private boolean hasRole(Authentication authentication, RolUsuario rol) {
        if (authentication == null) {
            return false;
        }
        String requiredAuthority = "ROLE_" + rol.name();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (requiredAuthority.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
