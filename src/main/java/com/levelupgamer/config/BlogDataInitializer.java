package com.levelupgamer.config;

import com.levelupgamer.contenido.Blog;
import com.levelupgamer.contenido.BlogAssetStorageService;
import com.levelupgamer.contenido.BlogRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class BlogDataInitializer implements CommandLineRunner {

    private final BlogRepository blogRepository;
    private final BlogAssetStorageService blogAssetStorageService;
    private final Path seedBasePath;

    public BlogDataInitializer(
            BlogRepository blogRepository,
            BlogAssetStorageService blogAssetStorageService,
            @Value("${blog.seed.local-markdown-dir:${user.dir}/s3-files/blogs}") String seedDir) {
        this.blogRepository = blogRepository;
        this.blogAssetStorageService = blogAssetStorageService;
        this.seedBasePath = Paths.get(seedDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.seedBasePath);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo preparar el directorio de seeds para blogs", ex);
        }
    }

    @Override
    @Transactional
    public void run(String... args) {
        boolean synced = sincronizarAssetsExistentes();
        if (synced) {
            System.out.println(">>> Se sincronizaron los assets de blogs con el proveedor de almacenamiento configurado. <<<");
        }
        if (blogRepository.count() == 0) {
            createBlogs();
            System.out.println(">>> Blogs de prueba creados exitosamente! <<<");
        } else {
            System.out.println(">>> La base de datos ya contiene blogs. No se crearon blogs de prueba. <<<");
        }
    }

    @SuppressWarnings("null")
    private void createBlogs() {
        Blog blog1 = Blog.builder()
                .titulo("Los videojuegos más influyentes de la historia")
                .autor("Matías Gutiérrez")
                .fechaPublicacion(LocalDate.now())
                .descripcionCorta("Un repaso por los títulos que definieron generaciones enteras.")
                .altImagen("Collage de videojuegos icónicos que marcaron la industria.")
                .build();
        persistWithAssets(blog1);

        Blog blog2 = Blog.builder()
                .titulo("PC gamer definitiva 2025: Guía de hardware y presupuesto")
                .autor("Victor Mena")
                .fechaPublicacion(LocalDate.now().minusDays(5))
                .descripcionCorta("Componentes recomendados y configuraciones equilibradas para este año.")
                .altImagen("Componentes modernos de PC gamer dispuestos sobre una mesa iluminada.")
                .build();
        persistWithAssets(blog2);

        Blog blog3 = Blog.builder()
                .titulo("Cómo entrenar como un pro: Estrategias para subir de rango")
                .autor("David Larenas")
                .fechaPublicacion(LocalDate.now().minusDays(10))
                .descripcionCorta("Técnicas mentales y mecánicas para destacar en competitivo.")
                .altImagen("Jugador profesional concentrado frente a su setup competitivo.")
                .build();
        persistWithAssets(blog3);
    }

    private void persistWithAssets(Blog blog) {
        Blog persisted = blogRepository.save(blog);
        if (syncAssetsForBlog(persisted)) {
            blogRepository.save(persisted);
        }
    }

    private boolean sincronizarAssetsExistentes() {
        boolean updatedAny = false;
        for (Blog blog : blogRepository.findAll()) {
            if (syncAssetsForBlog(blog)) {
                blogRepository.save(blog);
                updatedAny = true;
            }
        }
        return updatedAny;
    }

    private boolean syncAssetsForBlog(Blog blog) {
        if (blog.getId() == null) {
            return false;
        }
        Path blogDir = seedBasePath.resolve(String.valueOf(blog.getId()));
        boolean updated = false;

        Path markdown = blogDir.resolve("blog.md");
        if (Files.exists(markdown)) {
            String contenidoUrl = blogAssetStorageService.storeSeedMarkdown(blog.getId(), markdown)
                    .orElse(null);
            if (contenidoUrl != null && !contenidoUrl.equals(blog.getContenidoUrl())) {
                blog.setContenidoUrl(contenidoUrl);
                updated = true;
            }
        }

        Path imagen = blogDir.resolve("blog.jpg");
        if (Files.exists(imagen)) {
            String imagenUrl = blogAssetStorageService.storeSeedImage(blog.getId(), imagen)
                    .orElse(null);
            if (imagenUrl != null && !imagenUrl.equals(blog.getImagenUrl())) {
                blog.setImagenUrl(imagenUrl);
                updated = true;
            }
        }

        return updated;
    }
}
