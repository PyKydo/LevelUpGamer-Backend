# LevelUpGamer Backend

Backend para la tienda online LevelUpGamer, desarrollado como un monolito modular con Java 21 y Spring Boot. El proyecto gestiona usuarios, productos, pedidos, contenido y un programa de gamificación.

## Características

- **Gestión de Usuarios:** Registro, autenticación con JWT y roles (Admin, Vendedor, Cliente).
- **Catálogo de Productos:** Administración de productos, categorías y control de stock.
- **Sistema de Pedidos:** Creación y seguimiento de pedidos.
- **Gamificación:** Programa de puntos por compras y sistema de referidos.
- **Descuentos:** Lógica de descuentos para usuarios con correos específicos (Duoc).
- **Notificaciones:** Alertas de stock crítico y confirmaciones por correo electrónico (AWS SES).
- **Monitorización:** Endpoints de Actuator para supervisión de la salud y métricas de la aplicación.

## Tecnologías Utilizadas

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 3.5.0
- **Base de Datos:**
  - H2 (para desarrollo y pruebas)
  - PostgreSQL (para producción)
- **Seguridad:** Spring Security, JWT
- **Documentación de API:** SpringDoc (OpenAPI/Swagger)
- **Validación:** Spring Validation
- **Mapeo de Objetos:** Lombok
- **Envío de Correos:** AWS SES (Simple Email Service)
- **Build Tool:** Maven

## Estructura del Proyecto

El backend está diseñado como un monolito modular. Cada dominio de negocio está encapsulado en su propio paquete, lo que facilita el mantenimiento y la posible migración a microservicios en el futuro.

- `com.levelupgamer.auth`: Autenticación y seguridad.
- `com.levelupgamer.users`: Gestión de usuarios.
- `com.levelupgamer.products`: Gestión de productos y categorías.
- `com.levelupgamer.orders`: Gestión de pedidos.
- `com.levelupgamer.content`: Gestión de blogs y mensajes de contacto.
- `com.levelupgamer.gamification`: Lógica de puntos y referidos.
- `com.levelupgamer.common`: Clases y servicios comunes (ej: EmailService).
- `com.levelupgamer.config`: Clases de configuración (ej: AWS, validación).
- `com.levelupgamer.exception`: Manejo de excepciones globales.

## Modelo de Entidades (ERD)

A continuación se muestra una representación simple de las relaciones entre las entidades principales:

```diagram
[Usuario] 1--* [Pedido]
[Pedido] 1--* [PedidoItem]
[Producto] 1--* [PedidoItem]
[Usuario] 1--1 [Puntos] (Opcional, dependiendo del diseño)
```

- **Usuario:** Almacena la información de los usuarios, incluyendo su rol y puntos.
- **Producto:** Representa los artículos en venta.
- **Pedido:** Contiene la información de una compra, incluyendo el usuario y el total.
- **PedidoItem:** Tabla intermedia que almacena los productos y cantidades de un pedido.
- **Blog:** Entradas del blog.
- **Contacto:** Mensajes enviados desde el formulario de contacto.
- **Puntos:** (Diseño actual desacoplado) Historial de puntos de un usuario.

## API Endpoints

La API sigue un patrón RESTful. La URL base es `/api`.

### Autenticación (`/api/auth`)

- `POST /login`: Inicia sesión y devuelve un token JWT.

### Usuarios (`/api/users`)

- `POST /register`: Registra un nuevo usuario.
- `GET /{id}`: Obtiene un usuario por su ID.
- `PUT /{id}`: Actualiza el perfil de un usuario.
- `GET /roles`: (Admin) Obtiene la lista de roles de usuario.

### Productos (`/api/products`)

- `GET /`: Lista todos los productos con filtros opcionales.
- `GET /{id}`: Obtiene un producto por su ID.
- `POST /`: (Admin/Vendedor) Crea un nuevo producto.
- `PUT /{id}`: (Admin/Vendedor) Actualiza un producto.
- `DELETE /{id}`: (Admin) Elimina un producto.

### Pedidos (`/api/orders`)

- `POST /`: Crea un nuevo pedido.
- `GET /user/{userId}`: Lista los pedidos de un usuario.
- `GET /{id}`: Obtiene un pedido por su ID.

### Contenido (`/api/content`)

- `GET /blog-posts`: Lista todas las entradas del blog.
- `GET /blog-posts/{id}`: Obtiene una entrada del blog por su ID.
- `POST /contact-messages`: Envía un mensaje de contacto.

## Configuración y Despliegue

### Prerrequisitos

- Java 21
- PostgreSQL
- Cuenta de AWS con una instancia EC2 y un rol de IAM con permisos para SES.

### Configuración de la Base de Datos

1. Crea una base de datos PostgreSQL.
2. Crea un usuario con permisos para acceder a la base de datos.

### Configuración de la Aplicación

La aplicación se configura mediante variables de entorno en producción.

- `DB_URL`: URL de conexión a PostgreSQL. (ej: `jdbc:postgresql://localhost:5432/levelupgamer`)
- `DB_USERNAME`: Nombre de usuario de la base de datos.
- `DB_PASSWORD`: Contraseña del usuario de la base de datos.

### Ejecución de la Aplicación

1. **Construir la Aplicación:**

    ```bash
    ./mvnw clean install
    ```

2. **Copiar el Artefacto:**
    Copia el archivo JAR generado (`target/levelupgamer-backend-1.0.0.jar`) a la instancia EC2.

3. **Ejecutar la Aplicación:**
    Ejecuta la aplicación con el perfil `prod` activado:

    ```bash
    java -jar levelupgamer-backend-1.0.0.jar --spring.profiles.active=prod
    ```

## Flujo de la Aplicación

### Registro de Usuario con Referido

1. Un usuario se registra a través del endpoint `POST /api/users/register`.
2. Si se proporciona un `codigoReferido` válido, el sistema busca al usuario referente.
3. Se le asignan puntos de gamificación al referente.
4. El nuevo usuario se guarda en la base de datos.

### Creación de un Pedido

1. Un usuario autenticado realiza una solicitud a `POST /api/orders`.
2. El sistema verifica el stock de los productos solicitados.
3. Si el usuario tiene un correo Duoc (`isDuocUser = true`), se aplica un 20% de descuento.
4. Se calcula el total del pedido y se actualiza el stock de los productos.
5. Si el stock de un producto cae por debajo de su `stockCritico`, se emite una alerta en los logs.
6. Se asignan puntos al usuario en función del monto total de la compra.
7. El pedido se guarda en la base de datos.
