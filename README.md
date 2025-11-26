# LevelUpGamer Backend

Backend monolito modular para la tienda LevelUpGamer, construido con Java 21 y Spring Boot 3.5. La aplicación concentra autenticación JWT, catálogo de productos, carritos, boletas, blogs, reseñas y gamificación en un único despliegue.

Todos los endpoints REST están versionados bajo el prefijo `/api/v1/...` (ej. `/api/v1/boletas`, `/api/v1/cart`, `/api/v1/products`).

## Características Principales

- **Usuarios y roles**: registro con validaciones de RUN y dominios permitidos, contraseñas hasheadas con BCrypt y roles `ADMINISTRADOR`, `VENDEDOR` (puede crear/operar únicamente su inventario y ver sus propias boletas de seguimiento) y `CLIENTE`.
- **Autenticación JWT**: login con selección explícita de rol cuando un usuario posee múltiples perfiles, emisión de tokens de acceso y refresh + endpoint de cambio de contraseña.
- **Catálogo y stock**: CRUD de productos con ownership obligatorio (`Producto.vendedor`), carga de imágenes a S3, alertas de stock crítico en logs, campo `puntosLevelUp` (0-1000 en saltos de 100) para gamificación y endpoint de destacados (`GET /api/v1/products/featured`).
- **Carrito persistente + Boletas**: carritos por usuario, generación de boletas (`POST /api/v1/boletas`) que descuentan stock, calculan subtotales y asignan puntos multiplicando `producto.puntosLevelUp * cantidad`; soporte para descuentos combinados (20% correos Duoc + cupones con tope 90%).
- **Contenido**: blogs con cabeceras e imágenes almacenadas en S3 y recuperación del contenido en formato Markdown; todas las rutas siguen la convención `blogs/{id}/blog.{md|jpg}` (el inicializador migra automáticamente rutas antiguas) y el controlador fuerza UTF-8 al actuar como proxy de archivos externos para evitar corrupción de acentos; formulario de contacto persistido en BD.
- **Programa de puntos y referidos**: saldo de puntos en tabla dedicada (`Puntos`) con operaciones de earn/redeem, acumulación basada en atributos `puntosLevelUp`, conversión de puntos a cupones (%5-%30) y bonificación automática a referidos en el registro.
- **Sistema de cupones**: conversión de puntos a cupones únicos, listado de opciones disponibles, verificación de pertenencia/estado y soporte de stacking controlado con el descuento Duoc.
- **Reseñas verified purchase**: endpoint `POST /api/v1/reviews` (CLIENTE/ADMIN) valida `texto` (1-1000), `calificacion` (1-5) y `productoId` con Bean Validation, comprueba compras en `BoletaRepository` y responde `400` con mensajes claros cuando no se cumplen las reglas; los listados públicos salen desde `GET /api/v1/products/{productId}/reviews` sin exigir autenticación y los usuarios pueden eliminar sus propias reseñas (o cualquier ADMIN) vía `DELETE /api/v1/reviews/{id}`.
- **Observabilidad y documentación**: Swagger UI (`/swagger-ui/index.html`), Actuator y documentación funcional en `docs/personal`.

## Tecnologías y Dependencias Clave

- **Spring Boot Starter Stack**: Web, Data JPA, Security, Validation, Actuator y DevTools.
- **Persistencia**: H2 en `dev`/`test`, PostgreSQL en `prod`, Flyway listo para migraciones (deshabilitado en prod hasta definir scripts).
- **Seguridad**: Spring Security + filtros JWT personalizados (`JwtAutenticacionFilter`, `JwtProvider`).
- **Validaciones personalizadas**: anotaciones `@Rut`, `@Adult` y `@AllowedEmailDomain` con factoría de validadores registrada en `ValidationConfig`.
- **Integraciones AWS**: SDK v2 para S3 y SES (pendiente de uso), `FileStorageService` con implementación S3 para prod y stub local en dev/test.
- **Herramientas**: Lombok, SpringDoc OpenAPI (`springdoc-openapi-starter-webmvc-ui` 2.8.14 para estabilizar `/v3/api-docs`), driver PostgreSQL 42.7.8 (mitiga CVE-2024-1597) y Maven Wrapper.

## Arquitectura por Dominios

Cada dominio vive bajo `com.levelupgamer.{dominio}` y expone controladores REST bajo `/api/v1/{dominio}`:

- `autenticacion`: login, refresh, cambio de contraseña, configuración de seguridad y filtros JWT.
- `usuarios`: entidades, DTOs, mappers y servicios para usuarios, roles y referidos.
- `productos`: productos, categorías, reseñas, `puntosLevelUp` y seeding inicial (`ProductDataInitializer`).
- `boletas`: carritos persistentes, boletas, DTOs y lógica de puntos/stock/alertas con descuentos compuestos y trazabilidad de cupones usados.
- `gamificacion`: entidad `Puntos`, repositorio y servicios de earn/redeem + módulo de cupones con conversión, listado y canje.
- `contenido`: blogs (lectura desde S3) con generación/migración automática de rutas `blogs/{id}` y forzado UTF-8 al proxyear contenido remoto, mensajes de contacto y seeds (`BlogDataInitializer`).
- `common` y `config`: integraciones S3, validación, inicializadores y beans utilitarios.
- `exception`: `GlobalExceptionHandler` con formato consistente de errores, incluyendo respuestas `403` para `AccessDeniedException` cuando un vendedor intenta manipular productos ajenos o corporativos y `400` para reglas de negocio (`IllegalStateException`, ej. reseñas sin compra).

## Perfiles, Puertos y Configuración

- **Puerto por defecto**: `8081` (configurable vía `server.port`).
- **Perfil activo estándar**: `dev` (H2 en memoria y consola H2 habilitada).
- **Perfiles**:
  - `dev`: H2 (`jdbc:h2:mem:testdb`), bucket/region definidos en `application-dev.properties` y "safe defaults" que redirigen cargas a imágenes/payloads fallback (Picsum) sin necesidad de credenciales reales.
  - `test`: H2 aislado + propiedades dummy para AWS (`application-test.properties`) que reutilizan los mismos fallbacks, permitiendo ejecutar suites sin configurar secretos.
  - `prod`: PostgreSQL via `DB_URL`, deshabilita Flyway temporalmente y toma bucket/region/credenciales desde variables de entorno.

### Variables de Entorno para `prod`

| Variable | Descripción |
| --- | --- |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Datos de conexión a PostgreSQL. |
| `AWS_REGION` | Región del bucket S3 (ej: `us-east-1`). |
| `S3_BUCKET_NAME` | Nombre del bucket usado para imágenes y markdown. |
| `AWS_ACCESS_KEY` / `AWS_SECRET_KEY` | Credenciales con permisos `s3:GetObject`/`PutObject`. |
| `JWT_SECRET` | Clave HMAC usada por `JwtProvider` (reemplaza la default hardcodeada en prod). |
| `SERVER_PORT` | Opcional para exponer el backend en otro puerto (ej: 80/443 detrás de Nginx). |

Consulta `docs/personal/DEPLOYMENT.md` para el detalle de `EnvironmentFile` y el servicio systemd `levelupgamer.service`.

## Datos Semilla y Archivos S3

- `DataInitializer`, `ProductDataInitializer` y `BlogDataInitializer` crean usuarios, productos e historias de blog cuando la base está vacía (perfiles `!test`). El seed incluye cuentas demo de administrador, cliente y vendedor para probar los distintos roles.
- El contenido markdown de ejemplo vive en `s3-files/contenido/*.md`; al subirlo a S3 respeta la convención `blogs/{id}/blog.md` y `blogs/{id}/blog.jpg`. Si ya existían entradas bajo otro path, el `BlogDataInitializer` las corrige al iniciar cuando detecta un bucket configurado.
- Para desarrollo se puede apuntar `aws.s3.bucket.url` a un bucket público o a un mock (ej: LocalStack).

## Validaciones y Reglas de Negocio Clave

- RUN chileno sin puntos/guion (7-9 caracteres) y única columna `run`.
- Dominios permitidos para correo: `gmail.com`, `hotmail.com`, `outlook.com`, `yahoo.com`, `duoc.cl`, `profesor.duoc.cl`.
- Edad mínima 18 años (`@Adult`).
- Contraseñas entre 8 y 32 caracteres (pendiente extensión configurable).
- Reseñas: `texto` obligatorio (≤1000 caracteres), `calificacion` entre 1 y 5 y `productoId` requerido; las violaciones responden `400` con mapa campo -> error y los intentos sin compra previa retornan `400` vía `IllegalStateException`.
- Descuento 20% para correos `duoc.cl` / `profesor.duoc.cl` al generar boletas.
- Stock crítico: log `WARN` cuando `stock <= stockCritico`.
- Puntos LevelUp: cada producto define `puntosLevelUp` (0-1000, múltiplos de 100) que se multiplican por la cantidad al cerrar la boleta y se acumulan en `Puntos`.
- Cupones: conversión de 500-3000 puntos a cupones del 5%-30% (saltos de 5), canjeables una sola vez y con tope global del 90% al combinar con el descuento Duoc.

## Seguridad y Autorización

- Filtro `JwtAutenticacionFilter` agrega `SecurityContext` a partir del header `Authorization: Bearer <token>`.
- `SecurityConfig` habilita CORS global (`*`) y define accesos:
- Público: `/`, `/api/v1/auth/login`, `/api/v1/auth/refresh`, `/api/v1/users/register`, `/api/v1/products`, `/api/v1/products/{id}`, `/api/v1/products/featured`, `GET /api/v1/products/{id}/reviews`, `/api/v1/blog-posts/**`, `/api/v1/contact-messages`, `/swagger-ui/**`, `/v3/api-docs/**`.
- Requiere autenticación: `/api/v1/users/{id}`, `/api/v1/categories/**`, `/api/v1/boletas/**`, `/api/v1/points/**`, `/api/v1/cart/**`, `POST /api/v1/reviews` (limitado a CLIENTE/ADMIN), `DELETE /api/v1/reviews/{id}` (CLIENTE dueño o ADMIN), además de cualquier operación **POST/PUT/PATCH/DELETE** sobre `/api/v1/products/**`.
- Rol vendedor: `VENDEDOR` puede listar solo sus productos, crear/actualizar/eliminar elementos de su inventario (no los corporativos) y consultar boletas para seguimiento comercial; cualquier intento fuera de esos límites responde `403`.
- Solo admins: `/api/v1/users`, `/api/v1/users/roles`, `/api/v1/users/admin`, mutaciones de categorías y blogs, además de la gestión de productos corporativos “LevelUp”.
- Errores se devuelven como `{ "error": "mensaje" }` o mapas campo -> error en validaciones.

## Pruebas

- **Unitarias y de servicio**: ubicadas por dominio (`src/test/java/com/levelupgamer/{dominio}`) con Mockito y H2.
- **E2E / integración**: `AutenticacionE2ETest`, `UsuarioE2ETest`, `ProductoE2ETest`, `BoletaE2ETest`, `CarritoE2ETest`, `ContenidoE2ETest`, `GamificacionE2ETest` ejecutan escenarios completos usando el perfil `test`.
- Ejecuta todo con `./mvnw verify` o `./mvnw test -Ptest`.

## Documentación y Utilidades

- **Referencia REST detallada**: `docs/personal/api-endpoints.md` (métodos, roles, payloads, códigos de respuesta y cabeceras).
- **Guía rápida Spring Boot CLI**: `docs/personal/SpringBoot CLI.md`.
- **Este README**: arquitectura, perfiles y reglas de negocio siempre actualizadas.
- **Swagger/OpenAPI**: `http://localhost:8081/swagger-ui/index.html` (o el puerto configurado).
- **Actuator**: `GET /actuator/health` y métricas estándar.

## Ejecución Rápida en Desarrollo

1. Configura (opcional) `aws.s3.bucket.name`, `aws.region`, `aws.accessKey`, `aws.secretKey` en `application-dev.properties` o variables de entorno.
2. Ejecuta `./mvnw spring-boot:run` (perfil `dev` activado por defecto).
3. Usa las credenciales sembradas (por ejemplo `admin@gmail.com / admin123`). También están disponibles `cliente@gmail.com / cliente123` y `vendedor@gmail.com / vendedor123` para validar los roles CLIENTE y VENDEDOR.
4. Abre Swagger para probar los endpoints o consulta la guía en `docs/personal/API Endpoints.md`.

## Despliegue

- Provisiona PostgreSQL administrado (RDS) y crea la base `levelupgamer` con usuario dedicado. Expone la cadena vía `DB_URL`, `DB_USER`, `DB_PASS`.
- Configura en el servidor un archivo `/etc/levelupgamer.env` (o variables del sistema) con las claves descritas en la sección **Variables de Entorno (Producción)**.
- Empaqueta con `./mvnw clean package -DskipTests` y copia `target/levelupgamer-backend-*.jar` junto al unit file `src/main/resources/levelupgamer.service`.
- Actualiza el unit file apuntando al path real del JAR y al archivo de variables, cópialo a `/etc/systemd/system/`, ejecuta `systemctl daemon-reload` y luego `systemctl enable --now levelupgamer`.
- `journalctl -u levelupgamer -f` permite auditar logs en tiempo real.
- El flujo `.github/workflows/despliegue-ec2.yml` automatiza build + copia del artefacto + reinicio del servicio `levelupgamer.service` en la instancia.

- **`application-prod.properties` (Perfil `prod`):** Configuración para el entorno de producción. Utiliza variables de entorno para datos sensibles.

### Variables de Entorno (Producción)

Para desplegar en producción, es necesario configurar las siguientes variables de entorno en el servidor:

| Variable          | Descripción                                           | Ejemplo                                                 |
|-------------------|-------------------------------------------------------|---------------------------------------------------------|
| `DB_URL`          | URL de conexión a la base de datos PostgreSQL.        | `jdbc:postgresql://host:port/db_name`                   |
| `DB_USER`         | Usuario de la base de datos.                          | `admin`                                                 |
| `DB_PASS`         | Contraseña de la base de datos.                       | `secret_password`                                       |
| `S3_BUCKET_NAME`  | Nombre del bucket de AWS S3 para almacenar archivos.  | `levelupgamer-assets`                                   |
| `AWS_ACCESS_KEY_ID` | Clave de acceso de IAM para AWS.                      | `AKIAIOSFODNN7EXAMPLE`                                  |
| `AWS_SECRET_ACCESS_KEY` | Clave de acceso secreta de IAM para AWS.          | `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY`              |
| `JWT_SECRET`      | Clave secreta para firmar los tokens JWT.             | `una_clave_muy_larga_y_segura_para_produccion`          |

### Despliegue Automatizado con GitHub Actions

El repositorio está configurado con un flujo de trabajo de GitHub Actions (`.github/workflows/despliegue-ec2.yml`) que automatiza el despliegue en una instancia EC2 de AWS.

El flujo de trabajo realiza los siguientes pasos:

1. Se activa al hacer un `push` a la rama `main`.
2. Construye el proyecto y ejecuta las pruebas.
3. Copia el archivo JAR resultante a la instancia EC2.
4. Copia el archivo de servicio `levelupgamer.service` a `/etc/systemd/system/` en el servidor.
5. Recarga `systemd`, reinicia el servicio de la aplicación y verifica su estado.

El archivo `levelupgamer.service` define cómo el sistema operativo debe gestionar el proceso de la aplicación, asegurando que se reinicie si falla.
