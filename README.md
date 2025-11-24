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

La arquitectura del sistema expone una interfaz de programación de aplicaciones (API) basada en los principios REST (Representational State Transfer). Todos los recursos son accesibles a través de la URL base `/api`. A continuación, se detalla la especificación de los endpoints disponibles, organizados por dominio funcional.

### 1. Autenticación y Seguridad
Controlador: `AutenticacionController`
Ruta base: `/api/auth`

Este módulo gestiona el ciclo de vida de la sesión del usuario y la seguridad de las credenciales.

- **POST** `/login`
  - **Descripción:** Permite a un usuario ingresar al sistema proporcionando sus credenciales. Si el usuario tiene múltiples roles, debe especificar el rol deseado en el cuerpo de la petición.
  - **Cuerpo de la Petición:** Objeto JSON con `username` (correo electrónico), `password` y opcionalmente `rol`.
  - **Respuesta:** Objeto JSON conteniendo el `token` de acceso y el `rol` del usuario.

- **POST** `/refresh`
  - **Descripción:** Renueva el token de acceso actual para extender la sesión del usuario sin necesidad de reingresar credenciales.
  - **Cuerpo de la Petición:** Objeto JSON con el token de refresco.
  - **Respuesta:** Nuevo token de acceso.

- **POST** `/change-password`
  - **Descripción:** Permite al usuario autenticado modificar su contraseña de acceso.
  - **Cuerpo de la Petición:** Objeto JSON con la contraseña actual y la nueva contraseña.
  - **Respuesta:** Código de estado 200 OK si el cambio fue exitoso.

### 2. Gestión de Usuarios
Controlador: `UsuarioController`
Ruta base: `/api/users`

Administra la información de los usuarios registrados en la plataforma.

- **POST** `/register`
  - **Descripción:** Registra un nuevo usuario en la base de datos con el rol predeterminado de CLIENTE.
  - **Cuerpo de la Petición:** Objeto JSON con `nombre`, `apellido`, `email` y `password`.
  - **Respuesta:** Datos del usuario creado, excluyendo información sensible.

- **GET** `/{id}`
  - **Descripción:** Recupera la información detallada de un usuario específico mediante su identificador único.
  - **Respuesta:** Objeto JSON con los datos del perfil del usuario y sus puntos acumulados.

- **PUT** `/{id}`
  - **Descripción:** Actualiza la información personal de un usuario existente.
  - **Cuerpo de la Petición:** Objeto JSON con los campos a modificar.
  - **Respuesta:** Datos actualizados del usuario.

- **GET** `/roles`
  - **Descripción:** Lista los roles disponibles en el sistema (ej. ADMINISTRADOR, CLIENTE).
  - **Respuesta:** Array JSON con los nombres de los roles.

- **GET** `/` (Requiere Rol: ADMINISTRADOR)
  - **Descripción:** Obtiene un listado completo de todos los usuarios registrados en el sistema.
  - **Respuesta:** Lista de objetos JSON de usuarios.

- **POST** `/admin` (Requiere Rol: ADMINISTRADOR)
  - **Descripción:** Crea un nuevo usuario con privilegios administrativos.
  - **Cuerpo de la Petición:** Datos del nuevo administrador.
  - **Respuesta:** Datos del usuario administrador creado.

- **DELETE** `/{id}` (Requiere Rol: ADMINISTRADOR)
  - **Descripción:** Elimina permanentemente un usuario del sistema.
  - **Respuesta:** Código de estado 204 No Content si la eliminación fue exitosa.

### 3. Catálogo de Productos
Controlador: `ProductoController`
Ruta base: `/api/products`

Gestiona el inventario de productos disponibles para la venta.

- **GET** `/`
  - **Descripción:** Recupera el listado completo de productos disponibles en el catálogo.
  - **Respuesta:** Lista de objetos JSON con detalles de cada producto (nombre, precio, stock, imagen).

- **GET** `/{id}`
  - **Descripción:** Obtiene los detalles específicos de un producto individual.
  - **Respuesta:** Objeto JSON con la información completa del producto.

- **POST** `/` (Requiere Rol: ADMINISTRADOR)
  - **Descripción:** Ingresa un nuevo producto al catálogo. Soporta la carga de imágenes mediante `multipart/form-data`.
  - **Cuerpo de la Petición:** Datos del producto y archivo de imagen.
  - **Respuesta:** Datos del producto creado.

- **PUT** `/{id}` (Requiere Rol: ADMINISTRADOR)
  - **Descripción:** Modifica los atributos de un producto existente.
  - **Cuerpo de la Petición:** Objeto JSON con los datos actualizados.
  - **Respuesta:** Datos del producto actualizados.

- **DELETE** `/{id}` (Requiere Rol: ADMINISTRADOR)
  - **Descripción:** Retira un producto del catálogo.
  - **Respuesta:** Código de estado 204 No Content.

### 4. Carrito de Compras
Controlador: `CarritoController`
Ruta base: `/api/cart`

Maneja la selección temporal de productos antes de la compra.

- **GET** `/{userId}`
  - **Descripción:** Consulta el estado actual del carrito de compras de un usuario.
  - **Respuesta:** Objeto JSON representando el carrito y sus ítems.

- **POST** `/{userId}/add`
  - **Descripción:** Agrega una cantidad específica de un producto al carrito del usuario.
  - **Parámetros de Consulta:** `productId` (ID del producto), `quantity` (cantidad).
  - **Respuesta:** Estado actualizado del carrito.

- **DELETE** `/{userId}/remove`
  - **Descripción:** Elimina un producto específico del carrito de compras.
  - **Parámetros de Consulta:** `productId` (ID del producto a remover).
  - **Respuesta:** Estado actualizado del carrito.

- **DELETE** `/{userId}`
  - **Descripción:** Vacía completamente el carrito de compras del usuario.
  - **Respuesta:** Estado actualizado del carrito (vacío).

### 5. Gestión de Pedidos
Controlador: `PedidoController`
Ruta base: `/api/orders`

Procesa la confirmación de compras y el seguimiento de pedidos.

- **POST** `/` (Roles: CLIENTE, ADMINISTRADOR)
  - **Descripción:** Formaliza la compra generando un pedido a partir de los productos en el carrito.
  - **Cuerpo de la Petición:** Datos necesarios para el envío y facturación.
  - **Respuesta:** Confirmación del pedido con su número de seguimiento.

- **GET** `/user/{userId}`
  - **Descripción:** Lista el historial de pedidos realizados por un usuario.
  - **Respuesta:** Lista de pedidos con sus respectivos estados y detalles.

- **GET** `/{id}`
  - **Descripción:** Consulta los detalles de un pedido específico.
  - **Respuesta:** Información completa del pedido, incluyendo ítems y totales.

### 6. Contenido y Blog
Controlador: `BlogController`
Ruta base: `/api/blog-posts`

Gestiona el contenido editorial y noticias de la plataforma.

- **GET** `/`
  - **Descripción:** Lista las entradas de blog publicadas.
  - **Respuesta:** Lista de resúmenes de artículos.

- **GET** `/{id}`
  - **Descripción:** Obtiene los metadatos de una entrada de blog específica.
  - **Respuesta:** Detalles del artículo.

- **GET** `/{id}/content`
  - **Descripción:** Recupera el contenido completo (texto/markdown) de un artículo.
  - **Respuesta:** Texto plano o markdown del contenido.

- **POST** `/` (Requiere Rol: ADMINISTRADOR)
  - **Descripción:** Publica una nueva entrada en el blog.
  - **Cuerpo de la Petición:** Datos del artículo e imagen opcional.
  - **Respuesta:** Artículo creado.

- **PUT** `/{id}` (Requiere Rol: ADMINISTRADOR)
  - **Descripción:** Edita una entrada de blog existente.
  - **Cuerpo de la Petición:** Datos actualizados.
  - **Respuesta:** Artículo actualizado.

- **DELETE** `/{id}` (Requiere Rol: ADMINISTRADOR)
  - **Descripción:** Elimina una entrada del blog.
  - **Respuesta:** Código de estado 204 No Content.

### 7. Reseñas y Comentarios
Controlador: `ResenaController`
Ruta base: `/api`

Permite la interacción de los usuarios mediante valoraciones de productos.

- **POST** `/reviews` (Roles: CLIENTE, ADMINISTRADOR)
  - **Descripción:** Registra una nueva reseña y calificación para un producto.
  - **Cuerpo de la Petición:** ID del producto, texto de la reseña y calificación numérica.
  - **Respuesta:** Datos de la reseña creada.

- **GET** `/products/{productId}/reviews`
  - **Descripción:** Lista todas las reseñas asociadas a un producto.
  - **Respuesta:** Lista de reseñas con comentarios y calificaciones.

### 8. Gamificación y Puntos
Controlador: `PuntosController`
Ruta base: `/api/points`

Administra el sistema de recompensas y fidelización.

- **GET** `/{userId}`
  - **Descripción:** Consulta el saldo de puntos acumulados por un usuario.
  - **Respuesta:** Objeto JSON con el total de puntos.

- **POST** `/earn` (Roles: CLIENTE, ADMINISTRADOR)
  - **Descripción:** Asigna puntos a un usuario (generalmente por compras o acciones específicas).
  - **Cuerpo de la Petición:** Cantidad de puntos a sumar.
  - **Respuesta:** Saldo actualizado.

- **POST** `/redeem` (Roles: CLIENTE, ADMINISTRADOR)
  - **Descripción:** Procesa el canje de puntos por recompensas o descuentos.
  - **Cuerpo de la Petición:** Cantidad de puntos a canjear.
  - **Respuesta:** Saldo actualizado post-canje.

### 9. Contacto
Controlador: `ContactoController`
Ruta base: `/api/contact-messages`

- **POST** `/`
  - **Descripción:** Recibe y almacena mensajes enviados a través del formulario de contacto.
  - **Cuerpo de la Petición:** Nombre, correo y mensaje del usuario.
  - **Respuesta:** Confirmación de recepción.

### 10. Monitoreo (Health Check)
Controlador: `HealthCheckController`
Ruta base: `/`

- **GET** `/`
  - **Descripción:** Endpoint de diagnóstico para verificar que el servicio backend se encuentra operativo.
  - **Respuesta:** Mensaje de estado "OK".

## Pruebas

El proyecto incluye un conjunto de pruebas para garantizar la calidad y el correcto funcionamiento del código.

### Pruebas Unitarias
- **Ubicación:** `src/test/java`
- **Descripción:** Se centran en probar unidades de código aisladas (ej: métodos de un servicio, mappers). Utilizan mocks (Mockito) para simular dependencias y asegurar que el componente bajo prueba funciona como se espera sin depender de otros sistemas como la base de datos.
- **Ejecución:** Se ejecutan automáticamente con el comando `mvn clean install` o `mvn test`.

### Pruebas de Integración / E2E
- **Descripción:** Estas pruebas validan flujos completos de la aplicación, desde el endpoint de la API hasta la base de datos. Se utilizan para verificar que los diferentes módulos interactúan correctamente. Se configuran con la anotación `@SpringBootTest` y utilizan la base de datos en memoria H2 para no interferir con los datos de desarrollo o producción.
- **Ejemplo:** Probar que al llamar a `POST /api/cart/{userId}/add`, el producto se añade correctamente a la base de datos y la respuesta de la API es la esperada.
- **Ubicación de Pruebas E2E:**
  - `AutenticacionE2ETest.java`
  - `UsuarioE2ETest.java`
  - `ProductoE2ETest.java`
  - `PedidoE2ETest.java`
  - `CarritoE2ETest.java`
  - `ContenidoE2ETest.java`
  - `GamificacionE2ETest.java`

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
