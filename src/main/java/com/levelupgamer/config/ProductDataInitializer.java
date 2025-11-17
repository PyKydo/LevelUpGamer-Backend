package com.levelupgamer.config;

import com.levelupgamer.productos.CategoriaProducto;
import com.levelupgamer.productos.Producto;
import com.levelupgamer.productos.ProductoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

@Component
@Profile("!test")
public class ProductDataInitializer implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    @Value("${aws.s3.bucket.url}")
    private String s3BucketUrl;

    public ProductDataInitializer(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (productoRepository.count() == 0) {
            createProducts();
            System.out.println(">>> Productos de prueba creados exitosamente! <<<");
        } else {
            System.out.println(">>> La base de datos ya contiene productos. No se crearon productos de prueba. <<<");
        }
    }

    private void createProducts() {
        Producto p1 = Producto.builder()
                .codigo("NIN-SW-OLED")
                .nombre("Nintendo Switch OLED")
                .descripcion("Consola con pantalla OLED de 7 pulgadas y 64GB de almacenamiento.")
                .precio(new BigDecimal("349990.00"))
                .stock(50)
                .categoria(CategoriaProducto.CONSOLAS)
                .imagenes(Collections.singletonList(s3BucketUrl + "/products/nintendo-switch.jpg"))
                .activo(true)
                .build();
        productoRepository.save(p1);

        Producto p2 = Producto.builder()
                .codigo("LOGI-G502")
                .nombre("Mouse Logitech G502 HERO")
                .descripcion("Mouse para gaming de alto rendimiento con sensor HERO 25K.")
                .precio(new BigDecimal("49990.00"))
                .stock(120)
                .categoria(CategoriaProducto.MOUSE)
                .imagenes(Collections.singletonList(s3BucketUrl + "/products/logitech-g502.jpg"))
                .activo(true)
                .build();
        productoRepository.save(p2);
    }
}
