# LevelUpGamer Backend

Backend monolítico (Java 21 + Spring Boot 3.5) que expone todo bajo `/api/v1/**`.

## Stack
- Spring Boot Web / Security / Data JPA / Validation / Actuator
- JWT + roles `ADMINISTRADOR`, `VENDEDOR`, `CLIENTE`
- H2 (dev/test), PostgreSQL (prod)
- AWS S3 para archivos, Swagger UI y Maven Wrapper

## Funcionaliades
- Autenticación/login, refresh y cambio de contraseña
- Gestión de usuarios con validaciones de RUN/edad/domino
- Catálogo y stock con ownership por vendedor + destacados
- Carritos, boletas, cupones y puntos LevelUp
- Blogs/Markdown en S3 y formulario de contacto
- Reseñas solo para compras confirmadas

## Perfiles
| Perfil | Base de datos | Uso |
| --- | --- | --- |
| dev | H2 en memoria | Desarrollo local (`./mvnw spring-boot:run`) |
| test | H2 aislada | Suites automáticas (`./mvnw test`) |
| prod | PostgreSQL | Despliegue (requiere `DB_URL`, `DB_USER`, `DB_PASS`, `S3_BUCKET_NAME`, `AWS_REGION`, `JWT_SECRET`) |

Credenciales demo (`dev/test`):
- admin@gmail.com / admin123
- cliente@gmail.com / cliente123
- vendedor@gmail.com / vendedor123

## Ejecutar
```bash
./mvnw spring-boot:run      # Dev
./mvnw test                 # Pruebas
./mvnw clean package        # Jar listo para deploy
```
Swagger: http://localhost:8081/swagger-ui/index.html

## Despliegue
1. Exporta variables de entorno (ver tabla de perfiles) y `JWT_SECRET`.
2. `./mvnw clean package -DskipTests`.
3. Copia el jar de `target/` al servidor.
4. Levanta con `java -jar levelupgamer-backend.jar` o via systemd (`levelupgamer.service`).

GitHub Actions (`.github/workflows/despliegue-ec2.yml`) automatiza build + copia + reinicio en EC2.
