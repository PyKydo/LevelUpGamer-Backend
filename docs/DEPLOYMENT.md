# Guía de Despliegue en AWS EC2

Esta guía describe los pasos para desplegar el backend de LevelUpGamer en una instancia de AWS EC2.

## Prerrequisitos

- **Java 21:** Asegúrate de que Java 21 esté instalado en la instancia EC2.
- **PostgreSQL:** Una base de datos PostgreSQL accesible desde la instancia EC2.
- **Cuenta de AWS:**
  - Una instancia EC2 (se recomienda la capa gratuita para estudiantes).
  - Un rol de IAM con permisos para Amazon SES (Simple Email Service) adjunto a la instancia EC2.

## 1. Configuración de la Base de Datos

1. Crea una base de datos PostgreSQL.
2. Crea un usuario con permisos para acceder a la base de datos.

## 2. Configuración de la Aplicación

La aplicación se configura mediante variables de entorno. Asegúrate de configurar las siguientes variables en la instancia EC2:

- `DB_URL`: La URL de conexión a la base de datos PostgreSQL. Ejemplo: `jdbc:postgresql://localhost:5432/levelupgamer`
- `DB_USERNAME`: El nombre de usuario para la base de datos.
- `DB_PASSWORD`: La contraseña para el usuario de la base de datos.

## 3. Ejecución de la Aplicación

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

## 4. Verificación

- La aplicación debería estar disponible en el puerto 8080 (o el puerto que hayas configurado).
- Revisa los logs de la aplicación para asegurarte de que se haya conectado correctamente a la base de datos y que no haya errores.
- Puedes usar los endpoints de Actuator para verificar el estado de la aplicación (ej: `/actuator/health`).
