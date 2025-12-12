package com.levelupgamer.productos;

import com.levelupgamer.productos.dto.ProductoDTO;
import com.levelupgamer.productos.dto.ProductoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Productos", description = "Catálogo con control de propiedad por vendedor")
public class ProductoController {
    @Autowired
    private ProductoService productoService;

    @Operation(summary = "Listar productos",
            description = "Admins/Clientes ven el catálogo completo. Vendedores sólo ven los productos cuyo vendedor coincide con su sesión.")
    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarProductos() {
        return ResponseEntity.ok(productoService.listarProductos());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductoDTO>> listarDestacados() {
        return ResponseEntity.ok(productoService.listarDestacados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> getProducto(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .map(ProductoMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/assets")
    public ResponseEntity<List<String>> listarAssets(@PathVariable Long id) {
        return productoService.listarAssets(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

        @Operation(summary = "Crear producto",
            description = "Asocia automáticamente el producto al usuario autenticado. Los vendedores no pueden enviar un ID de vendedor manual.")
        @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProductoDTO> crearProducto(
                @RequestPart("producto") @Valid ProductoRequest producto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) throws IOException {
        return ResponseEntity.ok(productoService.crearProducto(producto, imagen));
    }

        @Operation(summary = "Actualizar producto",
            description = "Un vendedor sólo puede modificar sus productos activos. Los productos corporativos de LevelUp son exclusivos de administradores.")
        @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    @PutMapping("/{id}")
        public ResponseEntity<ProductoDTO> actualizarProducto(@PathVariable Long id, @Valid @RequestBody ProductoRequest producto) {
        return productoService.actualizarProducto(id, producto)
                .map(ProductoMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

        @Operation(summary = "Eliminar producto",
            description = "Elimina definitivamente el producto y sus recursos asociados. Los vendedores sólo pueden operar sobre su inventario y nunca sobre LevelUp.")
        @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        if (productoService.eliminarProducto(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
