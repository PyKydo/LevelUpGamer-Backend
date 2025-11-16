# LevelUpGamer Backend

Backend para la tienda online LevelUpGamer, desarrollado como un monolito modular con Java 21 y Spring Boot. El proyecto gestiona usuarios, productos, carritos de compra, pedidos, contenido y un programa de gamificación.

## Características Principales

- **Gestión de Usuarios:** Registro, autenticación con JWT y roles (Administrador, Cliente).
- **Catálogo de Productos:** Administración de productos, categorías y control de stock.
- **Carrito de Compras:** Lógica para agregar, ver y modificar el contenido del carrito de un usuario.
- **Sistema de Pedidos:** Creación y seguimiento de pedidos a partir del carrito.
- **Gamificación:** Programa de puntos por compras y sistema de referidos.
- **Descuentos:** Lógica de descuentos para usuarios con correos específicos (Duoc).
- **Notificaciones:** Alertas de stock crítico.
- **Monitorización:** Endpoints de Actuator para supervisión de la salud y métricas de la aplicación.

## Tecnologías Utilizadas

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 3.x
- **Base de Datos:**
  - H2 (para perfil `dev`)
  - PostgreSQL (para perfil `prod`)
- **Seguridad:** Spring Security, JWT
- **Documentación de API:** SpringDoc (OpenAPI/Swagger)
- **Validación:** Spring Validation
- **Mapeo de Objetos:** Custom Mappers
- **Servicios AWS:** S3 (para almacenamiento de archivos).
- **Build Tool:** Maven

## Estructura del Proyecto

El backend está diseñado como un monolito modular. Cada dominio de negocio está encapsulado en su propio paquete, lo que facilita el mantenimiento y la posible migración a microservicios en el futuro.

- `com.levelupgamer.autenticacion`: Autenticación y seguridad.
- `com.levelupgamer.usuarios`: Gestión de usuarios.
- `com.levelupgamer.productos`: Gestión de productos y categorías.
- `com.levelupgamer.pedidos`: Gestión de carritos y pedidos.
- `com.levelupgamer.contenido`: Gestión de blogs y mensajes de contacto.
- `com.levelupgamer.gamificacion`: Lógica de puntos y referidos.
- `com.levelupgamer.common`: Clases y servicios comunes.
- `com.levelupgamer.config`: Clases de configuración (ej: AWS, perfiles).
- `com.levelupgamer.exception`: Manejo de excepciones globales.

## API Endpoints

La API sigue un patrón RESTful. La URL base es `/api`.

### Autenticación (`/api/auth`)
- `POST /login`: Inicia sesión y devuelve un token JWT.

### Usuarios (`/api/users`)
- `POST /register`: Registra un nuevo usuario.
- `GET /{id}`: Obtiene un usuario por su ID.
- `PUT /{id}`: Actualiza el perfil de un usuario.

### Productos (`/api/products`)
- `GET /`: Lista todos los productos.
- `GET /{id}`: Obtiene un producto por su ID.
- `POST /`: (Admin) Crea un nuevo producto.
- `PUT /{id}`: (Admin) Actualiza un producto.
- `DELETE /{id}`: (Admin) Elimina un producto.

### Carrito de Compras (`/api/cart`)
- `GET /{userId}`: Obtiene el carrito de un usuario.
- `POST /{userId}/add`: Agrega un producto al carrito.
- `DELETE /{userId}/remove`: Elimina un producto del carrito.

### Pedidos (`/api/orders`)
- `POST /`: Crea un nuevo pedido a partir del carrito.
- `GET /user/{userId}`: Lista los pedidos de un usuario.
- `GET /{id}`: Obtiene un pedido por su ID.

### Contenido (`/api/content`)
- `GET /blog-posts`: Lista todas las entradas del blog.
- `POST /contact-messages`: Envía un mensaje de contacto.

## Pruebas

El proyecto incluye un conjunto de pruebas para garantizar la calidad y el correcto funcionamiento del código.

### Pruebas Unitarias
- **Ubicación:** `src/test/java`
- **Descripción:** Se centran en probar unidades de código aisladas (ej: métodos de un servicio, mappers). Utilizan mocks (Mockito) para simular dependencias y asegurar que el componente bajo prueba funciona como se espera sin depender de otros sistemas como la base de datos.
- **Ejecución:** Se ejecutan automáticamente con el comando `mvn clean install` o `mvn test`.

### Pruebas de Integración / E2E
- **Descripción:** Estas pruebas validan flujos completos de la aplicación, desde el endpoint de la API hasta la base de datos. Se utilizan para verificar que los diferentes módulos interactúan correctamente. Se configuran con la anotación `@SpringBootTest` y utilizan la base de datos en memoria H2 para no interferir con los datos de desarrollo o producción.
- **Ejemplo:** Probar que al llamar a `POST /api/cart/{userId}/add`, el producto se añade correctamente a la base de datos y la respuesta de la API es la esperada.

## Configuración y Despliegue

La aplicación utiliza perfiles de Spring Boot para gestionar diferentes configuraciones de entorno.

- **`application-dev.properties` (Perfil `dev`):** Configuración por defecto para desarrollo local. Utiliza una base de datos en memoria H2, lo que permite ejecutar la aplicación sin necesidad de configurar una base de datos externa.
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
1.  Se activa al hacer un `push` a la rama `main`.
2.  Construye el proyecto y ejecuta las pruebas.
3.  Copia el archivo JAR resultante a la instancia EC2.
4.  Copia el archivo de servicio `levelupgamer.service` a `/etc/systemd/system/` en el servidor.
5.  Recarga `systemd`, reinicia el servicio de la aplicación y verifica su estado.

El archivo `levelupgamer.service` define cómo el sistema operativo debe gestionar el proceso de la aplicación, asegurando que se reinicie si falla.
