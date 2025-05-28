# API de Gestión de Zoológico

API REST desarrollada en Java Spring Boot para la gestión de un zoológico, permitiendo a los empleados dejar mensajes sobre los animales y administrar las diferentes áreas del zoológico.

## Descripción

Este proyecto implementa una API para la gestión de un zoológico con las siguientes características:

- Sistema de autenticación basado en JWT con roles de ADMIN y EMPLEADO
- Gestión de zonas del zoológico
- Gestión de especies de animales
- Gestión de animales individuales
- Sistema de comentarios y respuestas sobre animales
- Estadísticas y reportes
- Sesiones multidispositivo con logout individual
- Limpieza automática de sesiones con token expirado cada hora
- Requiere header `X-Device-Id` para validar sesión por dispositivo

## Requisitos

- Java 17 o superior
- Maven
- PostgreSQL 16+

## Configuración de Base de Datos

La aplicación utiliza PostgreSQL. Es necesario crear una base de datos antes de ejecutar la aplicación.

```sql
CREATE DATABASE zoo_db;
```

## Configuración de la aplicación

Ajustar los parámetros de conexión a la base de datos en el archivo `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/zoo_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.query.substitutions=true 1, false 0
springdoc.swagger-ui.url=/docs/openapi.yaml
jwt.secret=586E3272357538782F413F4428472B4B6250655368566B597033733676397924
jwt.expiration=86400000
```

## Documentación de la API con Swagger:
- Esta aplicación incluye Swagger UI para explorar y probar los endpoints de la API de manera interactiva.
- Accede a traves de: http://localhost:8080/swagger-ui/index.html
- Asegurate de colocar el token de autenticacion correcta en el botón Authorize 🔒

## Ejecutar la aplicación
```bash
mvn spring-boot:run
```

## Usuarios precargados

La aplicación viene con un usuario administrador precargado:

- **Email**: admin@mail.com
- **Contraseña**: admin

# Tests y Calidad
- Se han implementado tests unitarios y de integración usando JUnit y Mockito.
- Se utiliza JaCoCo para generar reportes de cobertura de código.
- La cobertura de tests supera el 80%.
- Se usa SonarLint para mantener un código limpio y libre de malas prácticas.

```bash
mvn clean test jacoco:report
```


## Estructura del Proyecto

El proyecto sigue una arquitectura de capas (Controller, Service, Repository):

```
src
├── main
│   ├── java
│   │   └── com
│   │       └── nelumbo
│   │           └── zoo_api
│   │               ├── config
│   │               ├── controller
│   │               ├── dto
│   │               ├── entity
│   │               ├── exception
│   │               ├── repository
│   │               ├── security
│   │               ├── service
│   │               └── util
│   └── resources
│       └── application.properties
```

## Endpoints disponibles

### Autenticación

| Método | URL | Descripción |
|--------|-----|-------------|
| POST | /api/auth/login | Inicio de sesión |

### Usuarios

| Método | URL | Descripción |
|--------|-----|-------------|
| POST | /api/users | Registro de usuario (solo ADMIN) |

### Zonas

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | /api/zones/{id} | Obtener una zona por ID |
| PUT | /api/zones/{id} | Actualizar zona (solo ADMIN) |
| DELETE | /api/zones/{id} | Eliminar zona (solo ADMIN) |
| GET | /api/zones | Listar todas las zonas |
| POST | /api/zones | Crear zona (solo ADMIN) |

### Especies

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | /api/species/{id} | Obtener especie por ID |
| PUT | /api/species/{id} | Actualizar especie (solo ADMIN) |
| DELETE | /api/species/{id} | Eliminar especie (solo ADMIN) |
| GET | /api/species | Listar todas las especies |
| POST | /api/species | Crear especie (solo ADMIN) |

### Animales

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | /api/animals/{id} | Obtener animal por ID |
| PUT | /api/animals/{id} | Actualizar animal (solo ADMIN) |
| DELETE | /api/animals/{id} | Eliminar animal (solo ADMIN) |
| GET | /api/animals | Listar todos los animales |
| POST | /api/animals | Crear animal (solo ADMIN) |
| GET | /api/animals/by-date | Listar animales por fecha de registro |

### Comentarios

| Método | URL                                           | Descripción                                                         |
|--------|-----------------------------------------------|---------------------------------------------------------------------|
| GET    | /api/comments                                 | Obtener todos los comentarios  (Admin)                              |
| POST   | /api/comments                                 | Crear comentario                                                    |
| POST   | /api/comments/{commentId}/replies             | Responder a un comentario                                           |
| GET    | /api/comments/{commentId}                     | Obtener comentario por ID                                           |
| DELETE | /api/comments/{commentId}                     | Eliminar comentario (solo ADMIN)                                    |
| GET    | /api/comments/zone/{zonaID}/animal/{animalID} | Listar comentarios de un animal especifico en una zona especificada |

### Estadísticas

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | /api/stats/search | Búsqueda global |
| GET | /api/stats/answered-comments-percentage | Porcentaje de comentarios respondidos |
| GET | /api/stats/animals-by-zone | Cantidad de animales por zona |
| GET | /api/stats/animals-by-species | Cantidad de animales por especie |
| GET | /api/stats/animals-by-date | Animales registrados en una fecha específica |

## Reglas de negocio

- No se puede eliminar una zona si tiene animales asociados
- No se puede eliminar una especie si tiene animales asociados
- Al eliminar un animal, se eliminan todos sus comentarios y respuestas
- Solo los administradores pueden crear, actualizar y eliminar zonas, especies y animales
- Los empleados pueden ver zonas, especies, animales y agregar/responder comentarios

## Seguridad

- Sistema de autenticación basado en JWT
- Token con validez de 1 día
- Protección de endpoints según rol del usuario
- Manejo de excepciones para respuestas HTTP 400

## Colección de Postman

Se incluye una colección de Postman en el archivo `Zoo_Spring.postman_collection.json` en la raíz del proyecto para facilitar las pruebas de los endpoints.


