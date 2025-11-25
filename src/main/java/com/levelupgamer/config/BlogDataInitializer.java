package com.levelupgamer.config;

import com.levelupgamer.contenido.Blog;
import com.levelupgamer.contenido.BlogRepository;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Component
@Profile("!test")
public class BlogDataInitializer implements CommandLineRunner {

    private final BlogRepository blogRepository;

    @Value("${aws.s3.bucket.name:}")
    private String bucketName;

    @Value("${blog.seed.local-markdown-dir:s3-files/contenido}")
    private String localMarkdownDir;

    public BlogDataInitializer(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        boolean updated = ensureS3PathConvention();
        if (updated) {
            System.out.println(">>> Se actualizaron las rutas S3 de blogs existentes para utilizar blogs/{id}. <<<");
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
        // Blog 1
        Blog blog1 = Blog.builder()
                .titulo("Los mejores juegos de mesa para una noche de diversión")
                .autor("Matías Gutiérrez")
                .fechaPublicacion(LocalDate.now())
                .descripcionCorta("Descubre los juegos de mesa que no pueden faltar en tus reuniones.")
                .altImagen("Una selección de juegos de mesa sobre una mesa de madera.")
                .build();
        persistWithAssetUrls(blog1, "blog1");

        // Blog 2
        Blog blog2 = Blog.builder()
                .titulo("Cómo armar tu propia PC gamer en 2024")
                .autor("Victor Mena")
                .fechaPublicacion(LocalDate.now().minusDays(5))
                .descripcionCorta("Una guía paso a paso para construir la computadora de tus sueños.")
                .altImagen("Componentes de una PC gamer listos para ser ensamblados.")
                .build();
        persistWithAssetUrls(blog2, "blog2");

        // Blog 3
        Blog blog3 = Blog.builder()
                .titulo("El resurgimiento de las consolas retro")
                .autor("David Larenas")
                .fechaPublicacion(LocalDate.now().minusDays(10))
                .descripcionCorta("Un viaje nostálgico a las consolas que marcaron una época.")
                .altImagen("Una colección de consolas de videojuegos retro.")
                .build();
        persistWithAssetUrls(blog3, "blog3");
    }

    private boolean ensureS3PathConvention() {
        if (!StringUtils.hasText(bucketName)) {
            return false;
        }
        String prefix = "https://" + bucketName + ".s3.amazonaws.com/blogs/";
        boolean updatedAny = false;
        for (Blog blog : blogRepository.findAll()) {
            boolean updated = false;
            if (needsS3Fix(blog.getContenidoUrl(), prefix, blog.getId())) {
                blog.setContenidoUrl(buildContentUrl(blog.getId(), String.valueOf(blog.getId())));
                updated = true;
            }
            if (needsS3Fix(blog.getImagenUrl(), prefix, blog.getId())) {
                blog.setImagenUrl(buildImageUrl(blog.getId(), String.valueOf(blog.getId())));
                updated = true;
            }
            if (updated) {
                blogRepository.save(blog);
                updatedAny = true;
            }
        }
        return updatedAny;
    }

    private boolean needsS3Fix(String url, String prefix, Long blogId) {
        if (!StringUtils.hasText(url) || blogId == null) {
            return false;
        }
        return url.startsWith(prefix) && !url.startsWith(prefix + blogId + "/");
    }

    @SuppressWarnings("null")
    private void persistWithAssetUrls(Blog blog, String slug) {
        Blog persisted = blogRepository.save(blog);
        persisted.setContenidoUrl(buildContentUrl(persisted.getId(), slug));
        persisted.setImagenUrl(buildImageUrl(persisted.getId(), slug));
        // No need to re-save explicitly; entity is managed, but call save to be explicit.
        blogRepository.save(persisted);
    }

    private String buildContentUrl(Long blogId, String slug) {
        if (StringUtils.hasText(bucketName) && blogId != null) {
            return "https://" + bucketName + ".s3.amazonaws.com/blogs/" + blogId + "/blog.md";
        }
        Path basePath = Paths.get(localMarkdownDir).toAbsolutePath().normalize();
        return basePath.resolve(slug + ".md").toUri().toString();
    }

    private String buildImageUrl(Long blogId, String slug) {
        if (StringUtils.hasText(bucketName) && blogId != null) {
            return "https://" + bucketName + ".s3.amazonaws.com/blogs/" + blogId + "/blog.jpg";
        }
        return "https://picsum.photos/seed/levelupgamer-" + slug + "/1200/600";
    }
}
