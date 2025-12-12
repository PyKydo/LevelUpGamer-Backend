package com.levelupgamer.config;

import com.levelupgamer.contenido.Blog;
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
import org.springframework.util.StringUtils;


@Component
@Profile("!test")
public class BlogDataInitializer implements CommandLineRunner {

    private final BlogRepository blogRepository;
    private final AwsStorageProperties awsStorageProperties;
    private final Path localMarkdownBasePath;
    private final String localUploadsPrefix;
    private final String localBaseUrl;

    public BlogDataInitializer(BlogRepository blogRepository,
            AwsStorageProperties awsStorageProperties,
            @Value("${blog.seed.local-markdown-dir:${user.dir}/s3-files/blogs}") String localMarkdownDir,
            @Value("${storage.local.public-url-prefix:/uploads/}") String localPublicPrefix,
            @Value("${app.storage.local-base-url:}") String configuredLocalBaseUrl) {
        this.blogRepository = blogRepository;
        this.awsStorageProperties = awsStorageProperties;
        this.localMarkdownBasePath = Paths.get(localMarkdownDir).toAbsolutePath().normalize();
        this.localUploadsPrefix = normalizePrefix(localPublicPrefix);
        this.localBaseUrl = trimTrailingSlash(configuredLocalBaseUrl);
        try {
            Files.createDirectories(this.localMarkdownBasePath);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo crear el directorio local para blogs seed", ex);
        }
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        boolean updated = ensureS3PathConvention();
        if (updated) {
            System.out.println(">>> Se actualizaron las rutas S3 de blogs existentes para utilizar blogs/{id}. <<<");
        }
        boolean localUpdated = ensureLocalPathConvention();
        if (localUpdated) {
            System.out.println(">>> Se actualizaron las rutas locales de blogs existentes para apuntar a /uploads/blogs/{id}. <<<");
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
            .titulo("Los videojuegos más influyentes de la historia")
            .autor("Matías Gutiérrez")
            .fechaPublicacion(LocalDate.now())
            .descripcionCorta("Un repaso por los títulos que definieron generaciones enteras.")
            .altImagen("Collage de videojuegos icónicos que marcaron la industria.")
                .build();
        persistWithAssetUrls(blog1);

        // Blog 2
        Blog blog2 = Blog.builder()
            .titulo("PC gamer definitiva 2025: Guía de hardware y presupuesto")
                .autor("Victor Mena")
                .fechaPublicacion(LocalDate.now().minusDays(5))
            .descripcionCorta("Componentes recomendados y configuraciones equilibradas para este año.")
            .altImagen("Componentes modernos de PC gamer dispuestos sobre una mesa iluminada.")
                .build();
            persistWithAssetUrls(blog2);

        // Blog 3
        Blog blog3 = Blog.builder()
            .titulo("Cómo entrenar como un pro: Estrategias para subir de rango")
                .autor("David Larenas")
                .fechaPublicacion(LocalDate.now().minusDays(10))
            .descripcionCorta("Técnicas mentales y mecánicas para destacar en competitivo.")
            .altImagen("Jugador profesional concentrado frente a su setup competitivo.")
                .build();
            persistWithAssetUrls(blog3);
    }

    private boolean ensureS3PathConvention() {
        if (!awsStorageProperties.hasBucketConfigured()) {
            return false;
        }
        String bucketName = awsStorageProperties.getBucketName();
        String prefix = "https://" + bucketName + ".s3.amazonaws.com/blogs/";
        boolean updatedAny = false;
        for (Blog blog : blogRepository.findAll()) {
            boolean updated = false;
            if (needsS3Fix(blog.getContenidoUrl(), prefix, blog.getId())) {
                blog.setContenidoUrl(buildContentUrl(blog.getId()));
                updated = true;
            }
            if (needsS3Fix(blog.getImagenUrl(), prefix, blog.getId())) {
                blog.setImagenUrl(buildImageUrl(blog.getId()));
                updated = true;
            }
            if (updated) {
                blogRepository.save(blog);
                updatedAny = true;
            }
        }
        return updatedAny;
    }

    private boolean ensureLocalPathConvention() {
        if (awsStorageProperties.hasBucketConfigured()) {
            return false;
        }
        boolean updatedAny = false;
        for (Blog blog : blogRepository.findAll()) {
            boolean updated = false;
            if (needsLocalFix(blog.getContenidoUrl())) {
                blog.setContenidoUrl(buildContentUrl(blog.getId()));
                updated = true;
            }
            if (needsLocalFix(blog.getImagenUrl())) {
                blog.setImagenUrl(buildImageUrl(blog.getId()));
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

    private boolean needsLocalFix(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        String trimmed = url.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return false;
        }
        if (trimmed.startsWith("file:" ) || trimmed.startsWith("local://")) {
            return true;
        }
        String withoutBase = stripLocalBaseUrl(trimmed);
        if (withoutBase.startsWith(localUploadsPrefix)) {
            return false;
        }
        if (withoutBase.startsWith("/")) {
            withoutBase = withoutBase.substring(1);
        }
        if (withoutBase.startsWith("uploads/")) {
            return true;
        }
        return withoutBase.startsWith("blogs/");
    }

    @SuppressWarnings("null")
    private void persistWithAssetUrls(Blog blog) {
        Blog persisted = blogRepository.save(blog);
        persisted.setContenidoUrl(buildContentUrl(persisted.getId()));
        persisted.setImagenUrl(buildImageUrl(persisted.getId()));
        
        blogRepository.save(persisted);
    }

    private String buildContentUrl(Long blogId) {
        if (awsStorageProperties.hasBucketConfigured() && blogId != null) {
            String bucketName = awsStorageProperties.getBucketName();
            return "https://" + bucketName + ".s3.amazonaws.com/blogs/" + blogId + "/blog.md";
        }
        Path target = localMarkdownBasePath.resolve(String.valueOf(blogId)).resolve("blog.md");
        ensureFolderExists(target.getParent());
        return buildLocalUrl("blogs/" + blogId + "/blog.md");
    }

    private String buildImageUrl(Long blogId) {
        if (awsStorageProperties.hasBucketConfigured() && blogId != null) {
            String bucketName = awsStorageProperties.getBucketName();
            return "https://" + bucketName + ".s3.amazonaws.com/blogs/" + blogId + "/blog.jpg";
        }
        Path target = localMarkdownBasePath.resolve(String.valueOf(blogId)).resolve("blog.jpg");
        ensureFolderExists(target.getParent());
        if (Files.exists(target)) {
            return buildLocalUrl("blogs/" + blogId + "/blog.jpg");
        }
        return "https://picsum.photos/seed/levelupgamer-" + blogId + "/1200/600";
    }

    private void ensureFolderExists(Path folder) {
        if (folder == null) {
            return;
        }
        try {
            Files.createDirectories(folder);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo preparar el directorio de blogs seed", ex);
        }
    }

    private String buildLocalUrl(String relativePath) {
        if (StringUtils.hasText(localBaseUrl)) {
            return localBaseUrl + localUploadsPrefix + relativePath;
        }
        return localUploadsPrefix + relativePath;
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

}
