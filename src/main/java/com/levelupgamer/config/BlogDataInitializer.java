package com.levelupgamer.config;

import com.levelupgamer.contenido.Blog;
import com.levelupgamer.contenido.BlogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@Profile("!test")
public class BlogDataInitializer implements CommandLineRunner {

    private final BlogRepository blogRepository;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

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
        String s3BaseUrl = "https://" + bucketName + ".s3.amazonaws.com/";

        // Blog 1
        Blog blog1 = Blog.builder()
                .titulo("Los mejores juegos de mesa para una noche de diversión")
                .autor("Matías Gutiérrez")
                .fechaPublicacion(LocalDate.now())
                .descripcionCorta("Descubre los juegos de mesa que no pueden faltar en tus reuniones.")
                .contenidoUrl(s3BaseUrl + "blogs/1/blog1.md")
                .imagenUrl(s3BaseUrl + "blogs/1/blog1.jpg")
                .altImagen("Una selección de juegos de mesa sobre una mesa de madera.")
                .build();
        blogRepository.save(blog1);

        // Blog 2
        Blog blog2 = Blog.builder()
                .titulo("Cómo armar tu propia PC gamer en 2024")
                .autor("Victor Mena")
                .fechaPublicacion(LocalDate.now().minusDays(5))
                .descripcionCorta("Una guía paso a paso para construir la computadora de tus sueños.")
                .contenidoUrl(s3BaseUrl + "blogs/2/blog2.md")
                .imagenUrl(s3BaseUrl + "blogs/2/blog2.jpg")
                .altImagen("Componentes de una PC gamer listos para ser ensamblados.")
                .build();
        blogRepository.save(blog2);

        // Blog 3
        Blog blog3 = Blog.builder()
                .titulo("El resurgimiento de las consolas retro")
                .autor("David Larenas")
                .fechaPublicacion(LocalDate.now().minusDays(10))
                .descripcionCorta("Un viaje nostálgico a las consolas que marcaron una época.")
                .contenidoUrl(s3BaseUrl + "blogs/3/blog3.md")
                .imagenUrl(s3BaseUrl + "blogs/3/blog3.jpg")
                .altImagen("Una colección de consolas de videojuegos retro.")
                .build();
        blogRepository.save(blog3);
    }
}
