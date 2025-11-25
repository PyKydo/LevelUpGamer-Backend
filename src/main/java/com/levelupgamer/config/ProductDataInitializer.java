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

        @SuppressWarnings("null")
        private void createProducts() {
        // JM001 - Catan
        Producto jm001 = Producto.builder()
                .codigo("JM001")
                .nombre("Catan")
                .descripcion("Un clásico juego de estrategia donde los jugadores compiten por colonizar y expandirse en la isla de Catan. Ideal para 3-4 jugadores y perfecto para noches de juego en familia o con amigos.")
                .precio(new BigDecimal("29990"))
                .stock(15)
                .stockCritico(5)
                .categoria(CategoriaProducto.JUEGOS_DE_MESA)
                .puntosLevelUp(200)
                .imagenes(Collections.singletonList(s3BucketUrl + "/products/JM001-catan.webp"))
                .activo(true)
                .build();
        productoRepository.save(jm001);

        // JM002 - Carcassonne
        Producto jm002 = Producto.builder()
                .codigo("JM002")
                .nombre("Carcassonne")
                .descripcion("Un juego de colocación de fichas donde los jugadores construyen el paisaje alrededor de la fortaleza medieval de Carcassonne. Ideal para 2-5 jugadores y fácil de aprender.")
                .precio(new BigDecimal("24990"))
                .stock(12)
                .stockCritico(4)
                .categoria(CategoriaProducto.JUEGOS_DE_MESA)
                .puntosLevelUp(200)
                .imagenes(Collections.singletonList(s3BucketUrl + "/products/JM002-carcassonne.webp"))
                .activo(true)
                .build();
        productoRepository.save(jm002);

        // AC001 - Controlador Inalámbrico Xbox Series X
        Producto ac001 = Producto.builder()
                .codigo("AC001")
                .nombre("Controlador Inalámbrico Xbox Series X")
                .descripcion("Ofrece una experiencia de juego cómoda con botones mapeables y una respuesta táctil mejorada. Compatible con consolas Xbox y PC.")
                .precio(new BigDecimal("59990"))
                .stock(8)
                .stockCritico(3)
                .categoria(CategoriaProducto.ACCESORIOS)
                .puntosLevelUp(300)
                .imagenes(Collections.singletonList(s3BucketUrl + "/products/AC001-xbox-controller.webp"))
                .activo(true)
                .build();
        productoRepository.save(ac001);

        // AC002 - Auriculares Gamer HyperX Cloud II
        Producto ac002 = Producto.builder()
            .codigo("AC002")
            .nombre("Auriculares Gamer HyperX Cloud II")
            .descripcion("Proporcionan un sonido envolvente de calidad con un micrófono desmontable y almohadillas de espuma viscoelástica para mayor comodidad durante largas sesiones de juego.")
            .precio(new BigDecimal("79990"))
            .stock(6)
            .stockCritico(2)
            .categoria(CategoriaProducto.ACCESORIOS)
            .puntosLevelUp(400)
            .imagenes(Collections.singletonList(s3BucketUrl + "/products/AC002-hyperx-cloud.webp"))
            .activo(true)
            .build();
        productoRepository.save(ac002);

        // CQ001 - PlayStation 5
        Producto cq001 = Producto.builder()
                .codigo("CQ001")
                .nombre("PlayStation 5")
                .descripcion("La consola de última generación de Sony, que ofrece gráficos impresionantes y tiempos de carga ultrarrápidos para una experiencia de juego inmersiva.")
                .precio(new BigDecimal("549990"))
                .stock(3)
                .stockCritico(1)
                .categoria(CategoriaProducto.CONSOLAS)
                .puntosLevelUp(800)
                .imagenes(Collections.singletonList(s3BucketUrl + "/products/CQ001-ps5.webp"))
                .activo(true)
                .build();
        productoRepository.save(cq001);

        // CG001 - PC Gamer ASUS ROG Strix
        Producto cg001 = Producto.builder()
                .codigo("CG001")
                .nombre("PC Gamer ASUS ROG Strix")
                .descripcion("Un potente equipo diseñado para los gamers más exigentes, equipado con los últimos componentes para ofrecer un rendimiento excepcional en cualquier juego.")
                .precio(new BigDecimal("1299990"))
                .stock(2)
                .stockCritico(1)
                .categoria(CategoriaProducto.COMPUTADORES_GAMERS)
                .puntosLevelUp(1000)
                .imagenes(Collections.singletonList(s3BucketUrl + "/products/CG001-asus-rog.webp"))
                .activo(true)
                .build();
        productoRepository.save(cg001);

        // SG001 - Silla Gamer SecretLab Titan
        Producto sg001 = Producto.builder()
                .codigo("SG001")
                .nombre("Silla Gamer SecretLab Titan")
                .descripcion("Diseñada para el máximo confort, esta silla ofrece un soporte ergonómico y personalización ajustable para sesiones de juego prolongadas.")
                .precio(new BigDecimal("349990"))
                .stock(4)
                .stockCritico(1)
                .categoria(CategoriaProducto.SILLAS_GAMERS)
                .puntosLevelUp(300)
                .imagenes(Collections.singletonList(s3BucketUrl + "/products/SG001-secretlab-titan.webp"))
                .activo(true)
                .build();
        productoRepository.save(sg001);

        // MS001 - Mouse Gamer Logitech G502 HERO
        Producto ms001 = Producto.builder()
                .codigo("MS001")
                .nombre("Mouse Gamer Logitech G502 HERO")
                .descripcion("Con sensor de alta precisión y botones personalizables, este mouse es ideal para gamers que buscan un control preciso y personalización.")
                .precio(new BigDecimal("49990"))
                .stock(10)
                .stockCritico(3)
                .categoria(CategoriaProducto.MOUSE)
                .puntosLevelUp(200)
                .imagenes(Collections.singletonList(s3BucketUrl + "/products/MS001-logitech-g502.webp"))
                .activo(true)
                .build();
        productoRepository.save(ms001);

        // MP001 - Mousepad Razer Goliathus Extended Chroma
        Producto mp001 = Producto.builder()
                .codigo("MP001")
                .nombre("Mousepad Razer Goliathus Extended Chroma")
                .descripcion("Ofrece un área de juego amplia con iluminación RGB personalizable, asegurando una superficie suave y uniforme para el movimiento del mouse.")
                .precio(new BigDecimal("29990"))
                .stock(15)
                .stockCritico(5)
                .categoria(CategoriaProducto.MOUSEPAD)
                .puntosLevelUp(100)
                .imagenes(Collections.singletonList(s3BucketUrl + "/products/MP001-razer-goliathus.webp"))
                .activo(true)
                .build();
        productoRepository.save(mp001);

        // PP001 - Polera Gamer Personalizada 'Level-Up'
        Producto pp001 = Producto.builder()
                .codigo("PP001")
                .nombre("Polera Gamer Personalizada 'Level-Up'")
                .descripcion("Una camiseta cómoda y estilizada, con la posibilidad de personalizarla con tu gamer tag o diseño favorito.")
                .precio(new BigDecimal("14990"))
                .stock(20)
                .stockCritico(8)
                .categoria(CategoriaProducto.POLERAS_PERSONALIZADAS)
                .puntosLevelUp(100)
                .imagenes(Collections.singletonList(s3BucketUrl + "/products/PP001-levelup-tshirt.webp"))
                .activo(true)
                .build();
        productoRepository.save(pp001);
    }
}
