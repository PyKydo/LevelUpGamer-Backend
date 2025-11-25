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
        if (blogRepository.count() == 0) {
            createBlogs();
            System.out.println(">>> Blogs de prueba creados exitosamente! <<<");
        } else {
            System.out.println(">>> La base de datos ya contiene blogs. No se crearon blogs de prueba. <<<");
        }
    }

    private void createBlogs() {
        String contentBase = resolveContentBase();

        // Blog 1
        Blog blog1 = Blog.builder()
                .titulo("Los mejores juegos de mesa para una noche de diversión")
                .autor("Matías Gutiérrez")
                .fechaPublicacion(LocalDate.now())
                .descripcionCorta("Descubre los juegos de mesa que no pueden faltar en tus reuniones.")
                .contenidoUrl(contentBase + "blog1.md")
                .imagenUrl(buildImageUrl("blog1"))
                .altImagen("Una selección de juegos de mesa sobre una mesa de madera.")
                .build();
        blogRepository.save(blog1);

        // Blog 2
        Blog blog2 = Blog.builder()
                .titulo("Cómo armar tu propia PC gamer en 2024")
                .autor("Victor Mena")
                .fechaPublicacion(LocalDate.now().minusDays(5))
                .descripcionCorta("Una guía paso a paso para construir la computadora de tus sueños.")
                .contenidoUrl(contentBase + "blog2.md")
                .imagenUrl(buildImageUrl("blog2"))
                .altImagen("Componentes de una PC gamer listos para ser ensamblados.")
                .build();
        blogRepository.save(blog2);

        // Blog 3
        Blog blog3 = Blog.builder()
                .titulo("El resurgimiento de las consolas retro")
                .autor("David Larenas")
                .fechaPublicacion(LocalDate.now().minusDays(10))
                .descripcionCorta("Un viaje nostálgico a las consolas que marcaron una época.")
                .contenidoUrl(contentBase + "blog3.md")
                .imagenUrl(buildImageUrl("blog3"))
                .altImagen("Una colección de consolas de videojuegos retro.")
                .build();
        blogRepository.save(blog3);
    }

    private String resolveContentBase() {
        if (StringUtils.hasText(bucketName)) {
            return "https://" + bucketName + ".s3.amazonaws.com/blogs/";
        }
        Path basePath = Paths.get(localMarkdownDir).toAbsolutePath().normalize();
        return basePath.toUri().toString();
    }

    private String buildImageUrl(String slug) {
        if (StringUtils.hasText(bucketName)) {
            return "https://" + bucketName + ".s3.amazonaws.com/blogs/" + slug + ".jpg";
        }
        return "https://picsum.photos/seed/levelupgamer-" + slug + "/1200/600";
    }
}
