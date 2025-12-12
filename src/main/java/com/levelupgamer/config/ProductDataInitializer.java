package com.levelupgamer.config;

import com.levelupgamer.productos.Producto;
import com.levelupgamer.productos.ProductoRepository;
import com.levelupgamer.productos.categorias.Categoria;
import com.levelupgamer.productos.categorias.CategoriaRepository;
import com.levelupgamer.usuarios.RolUsuario;
import com.levelupgamer.usuarios.Usuario;
import com.levelupgamer.usuarios.UsuarioRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@Profile("!test")
@Order(2)
public class ProductDataInitializer implements CommandLineRunner {

        private static final Logger log = LoggerFactory.getLogger(ProductDataInitializer.class);
        private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "avif");

        private final ProductoRepository productoRepository;
        private final CategoriaRepository categoriaRepository;
        private final UsuarioRepository usuarioRepository;
        private final AwsStorageProperties awsStorageProperties;
        private final Path localProductAssetsBasePath;
        private final String localUploadsPrefix;
        private final String localBaseUrl;

        public ProductDataInitializer(ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
                        UsuarioRepository usuarioRepository,
                        AwsStorageProperties awsStorageProperties,
                        @Value("${storage.local.base-path:${user.dir}/s3-files}") String localBasePath,
                        @Value("${storage.local.public-url-prefix:/uploads/}") String localPublicPrefix,
                        @Value("${app.storage.local-base-url:}") String configuredLocalBaseUrl) {
                this.productoRepository = productoRepository;
                this.categoriaRepository = categoriaRepository;
                this.usuarioRepository = usuarioRepository;
                this.awsStorageProperties = awsStorageProperties;
                this.localProductAssetsBasePath = Paths.get(localBasePath).toAbsolutePath().normalize().resolve("products");
                this.localUploadsPrefix = normalizePrefix(localPublicPrefix);
                this.localBaseUrl = trimTrailingSlash(configuredLocalBaseUrl);
                try {
                        Files.createDirectories(this.localProductAssetsBasePath);
                } catch (IOException ex) {
                        throw new IllegalStateException("No se pudo preparar el directorio local de productos seed", ex);
                }
        }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
                boolean normalizedLocalImages = ensureLocalImageConvention();
                if (normalizedLocalImages) {
                        System.out.println(">>> Se normalizaron las URLs locales de imágenes de productos para usar el prefijo configurado. <<<");
                }
        if (productoRepository.count() == 0) {
            createProducts();
            System.out.println(">>> Productos de prueba creados exitosamente! <<<");
        } else {
            System.out.println(">>> La base de datos ya contiene productos. No se crearon productos de prueba. <<<");
        }
    }

        @SuppressWarnings("null")
        private void createProducts() {
        Usuario levelUpVendor = resolveLevelUpVendor();
        Categoria juegosMesa = obtenerOCrearCategoria("JUEGOS_MESA", "Juegos de Mesa",
                "Juegos de estrategia y mesa para toda la familia");
        Categoria accesorios = obtenerOCrearCategoria("ACCESORIOS", "Accesorios",
                "Periféricos y accesorios gamer");
        Categoria consolas = obtenerOCrearCategoria("CONSOLAS", "Consolas",
                "Consolas de última generación");
        Categoria computadores = obtenerOCrearCategoria("COMPUTADORES_GAMERS", "Computadores Gamers",
                "Equipos de alto rendimiento");
        Categoria sillas = obtenerOCrearCategoria("SILLAS_GAMERS", "Sillas Gamers",
                "Sillas ergonómicas para largas sesiones");
        Categoria mouse = obtenerOCrearCategoria("MOUSE", "Mouse Gamer",
                "Mouse diseñados para gaming");
        Categoria mousepad = obtenerOCrearCategoria("MOUSEPAD", "Mousepad",
                "Superficies para precisión");
        Categoria poleras = obtenerOCrearCategoria("POLERAS_PERSONALIZADAS", "Poleras Personalizadas",
                "Merchandising gamer personalizable");

        // JM001 - Catan
        Producto jm001 = Producto.builder()
                .codigo("JM001")
                .nombre("Catan")
                .descripcion("Un clásico juego de estrategia donde los jugadores compiten por colonizar y expandirse en la isla de Catan. Ideal para 3-4 jugadores y perfecto para noches de juego en familia o con amigos.")
                .precio(new BigDecimal("29990"))
                .stock(15)
                .stockCritico(5)
                .categoria(juegosMesa)
                .puntosLevelUp(200)
                .imagenes(buildSeedImagesForProduct("JM001", "JM001-catan"))
                .activo(true)
                .vendedor(levelUpVendor)
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
                .categoria(juegosMesa)
                .puntosLevelUp(200)
                .imagenes(buildSeedImagesForProduct("JM002", "JM002-carcassonne"))
                .activo(true)
                .vendedor(levelUpVendor)
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
                .categoria(accesorios)
                .puntosLevelUp(300)
                .imagenes(buildSeedImagesForProduct("AC001", "AC001-xbox-controller"))
                .activo(true)
                .vendedor(levelUpVendor)
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
            .categoria(accesorios)
            .puntosLevelUp(400)
            .imagenes(buildSeedImagesForProduct("AC002", "AC002-hyperx-cloud"))
            .activo(true)
            .vendedor(levelUpVendor)
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
                .categoria(consolas)
                .puntosLevelUp(800)
                .imagenes(buildSeedImagesForProduct("CQ001", "CQ001-ps5"))
                .activo(true)
                .vendedor(levelUpVendor)
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
                .categoria(computadores)
                .puntosLevelUp(1000)
                .imagenes(buildSeedImagesForProduct("CG001", "CG001-asus-rog"))
                .activo(true)
                .vendedor(levelUpVendor)
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
                .categoria(sillas)
                .puntosLevelUp(300)
                .imagenes(buildSeedImagesForProduct("SG001", "SG001-secretlab-titan"))
                .activo(true)
                .vendedor(levelUpVendor)
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
                .categoria(mouse)
                .puntosLevelUp(200)
                .imagenes(buildSeedImagesForProduct("MS001", "MS001-logitech-g502"))
                .activo(true)
                .vendedor(levelUpVendor)
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
                .categoria(mousepad)
                .puntosLevelUp(100)
                .imagenes(buildSeedImagesForProduct("MP001", "MP001-razer-goliathus"))
                .activo(true)
                .vendedor(levelUpVendor)
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
                .categoria(poleras)
                .puntosLevelUp(100)
                .imagenes(buildSeedImagesForProduct("PP001", "PP001-levelup-tshirt"))
                .activo(true)
                .vendedor(levelUpVendor)
                .build();
        productoRepository.save(pp001);
    }

        private List<String> buildSeedImagesForProduct(String productCode, String placeholderSeed) {
                List<String> assetNames = collectLocalAssetNames(productCode);
                if (!assetNames.isEmpty()) {
                        if (awsStorageProperties.hasBucketConfigured()) {
                                String bucketBase = resolveBucketBaseUrl();
                                return assetNames.stream()
                                                .map(name -> bucketBase + "products/" + productCode + "/" + name)
                                                .toList();
                        }
                        return assetNames.stream()
                                        .map(name -> buildLocalUrl("products/" + productCode + "/" + name))
                                        .toList();
                }
                return Collections.singletonList(buildPlaceholderImage(placeholderSeed));
        }

        private List<String> collectLocalAssetNames(String productCode) {
                Path productDir = localProductAssetsBasePath.resolve(productCode);
                if (!Files.exists(productDir)) {
                        return Collections.emptyList();
                }
                try (Stream<Path> stream = Files.list(productDir)) {
                        return stream.filter(Files::isRegularFile)
                                        .map(path -> path.getFileName().toString())
                                        .filter(this::isSupportedImage)
                                        .sorted()
                                        .toList();
                } catch (IOException ex) {
                        log.warn("No se pudieron leer los assets locales de {}: {}", productCode, ex.getMessage());
                        return Collections.emptyList();
                }
        }

        private boolean isSupportedImage(String fileName) {
                int dot = fileName.lastIndexOf('.') + 1;
                if (dot <= 0 || dot >= fileName.length()) {
                        return false;
                }
                String extension = fileName.substring(dot).toLowerCase(Locale.ROOT);
                return SUPPORTED_IMAGE_EXTENSIONS.contains(extension);
        }

        private String resolveBucketBaseUrl() {
                String bucketUrl = awsStorageProperties.getBucketUrl();
                if (StringUtils.hasText(bucketUrl)) {
                        return bucketUrl.endsWith("/") ? bucketUrl : bucketUrl + "/";
                }
                String bucketName = awsStorageProperties.getBucketName();
                return "https://" + bucketName + ".s3.amazonaws.com/";
        }

        private String buildPlaceholderImage(String seed) {
                String sanitized = seed.replaceAll("[^A-Za-z0-9]", "");
                if (!StringUtils.hasText(sanitized)) {
                        sanitized = "default";
                }
                return "https://picsum.photos/seed/levelup-" + sanitized.toLowerCase(Locale.ROOT) + "/800/800";
        }

        private String normalizePrefix(String prefix) {
                String normalized = prefix;
                if (!normalized.startsWith("/")) {
                        normalized = "/" + normalized;
                }
                if (!normalized.endsWith("/")) {
                        normalized = normalized + "/";
                }
                return normalized;
        }

        private String trimTrailingSlash(String value) {
                if (!StringUtils.hasText(value)) {
                        return null;
                }
                String normalized = value.trim();
                while (normalized.endsWith("/")) {
                        normalized = normalized.substring(0, normalized.length() - 1);
                }
                return normalized;
        }

        private String buildLocalUrl(String relativePath) {
                if (StringUtils.hasText(localBaseUrl)) {
                        return localBaseUrl + localUploadsPrefix + relativePath;
                }
                return localUploadsPrefix + relativePath;
        }

        private boolean ensureLocalImageConvention() {
                if (awsStorageProperties.hasBucketConfigured()) {
                        return false;
                }
                boolean updatedAny = false;
                for (Producto producto : productoRepository.findAll()) {
                        List<String> imagenes = producto.getImagenes();
                        if (imagenes == null || imagenes.isEmpty()) {
                                continue;
                        }
                        List<String> normalizadas = imagenes.stream()
                                        .map(this::normalizeLocalImageUrl)
                                        .collect(Collectors.toList());
                        if (!normalizadas.equals(imagenes)) {
                                producto.setImagenes(normalizadas);
                                productoRepository.save(producto);
                                updatedAny = true;
                        }
                }
                return updatedAny;
        }

        private String normalizeLocalImageUrl(String original) {
                if (!StringUtils.hasText(original)) {
                        return original;
                }
                String trimmed = original.trim();
                if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                        return original;
                }
                String relativePath = extractRelativeProductPath(trimmed);
                if (!StringUtils.hasText(relativePath)) {
                        return original;
                }
                return buildLocalUrl(relativePath);
        }

        private String extractRelativeProductPath(String url) {
                String withoutBase = stripLocalBaseUrl(url);
                String normalized = withoutBase.replace("\\", "/");
                int index = normalized.indexOf("products/");
                if (index >= 0) {
                        return normalized.substring(index);
                }
                return null;
        }

        private String stripLocalBaseUrl(String value) {
                if (!StringUtils.hasText(value) || !StringUtils.hasText(localBaseUrl)) {
                        return value;
                }
                if (value.startsWith(localBaseUrl)) {
                        return value.substring(localBaseUrl.length());
                }
                return value;
        }

        @SuppressWarnings("null")
        private Usuario resolveLevelUpVendor() {
                return usuarioRepository.findFirstByRolesContaining(RolUsuario.ADMINISTRADOR)
                                .orElseThrow(() -> new IllegalStateException(
                                                "Debe existir al menos un usuario ADMINISTRADOR para asociar los productos seed a LevelUp"));
        }

    @SuppressWarnings("null")
    private Categoria obtenerOCrearCategoria(String codigo, String nombre, String descripcion) {
        Categoria existente = categoriaRepository.findByCodigoIgnoreCase(codigo).orElse(null);
        if (existente != null) {
            return existente;
        }

        Categoria nueva = Categoria.builder()
                .codigo(codigo)
                .nombre(nombre)
                .descripcion(descripcion)
                .activo(true)
                .build();
        categoriaRepository.save(nueva);
        return categoriaRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new IllegalStateException("No se pudo persistir la categoria " + codigo));
    }
}
