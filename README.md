# kata-backend — Plataforma de Gestión y Reserva de Recursos

> API REST para gestionar y reservar recursos compartidos (salas, equipos, vehículos, etc.), evitando solapamientos, mostrando disponibilidad y ofreciendo historial de reservas.

---

## Tabla de contenidos

- [Descripción](#descripción)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Requisitos previos](#requisitos-previos)
- [Instalación y ejecución](#instalación-y-ejecución)
  - [Con Docker Compose (recomendado)](#con-docker-compose-recomendado)
  - [Ejecución local sin Docker](#ejecución-local-sin-docker)
- [Variables de entorno](#variables-de-entorno)
- [Endpoints de la API](#endpoints-de-la-api)
  - [Autenticación](#autenticación)
  - [Tipos de recurso](#tipos-de-recurso)
  - [Recursos](#recursos)
  - [Reservas](#reservas)
- [Control de acceso basado en roles (RBAC)](#control-de-acceso-basado-en-roles-rbac)
- [Migraciones de base de datos](#migraciones-de-base-de-datos)
- [Pruebas](#pruebas)
- [Documentación interactiva (Swagger)](#documentación-interactiva-swagger)
- [Colección de Postman](#colección-de-postman)
- [Decisiones de arquitectura](#decisiones-de-arquitectura)
- [Uso de IA](#uso-de-ia)

---

## Descripción

Aplicación backend desarrollada con **Spring Boot 4** y **Java 21** que expone una API RESTful para la gestión y reserva de recursos compartidos. Permite a equipos crear, editar y eliminar recursos, realizar reservas con validación de solapamientos, consultar disponibilidad y revisar el historial de reservas. La autenticación se realiza mediante JWT y se implementa un sistema de permisos basado en roles (RBAC) que filtra la visibilidad de recursos según el rol del usuario.

---

## Tecnologías

| Componente        | Tecnología                     |
| ----------------- | ------------------------------ |
| Lenguaje          | Java 21                        |
| Framework         | Spring Boot 4.0.2              |
| Persistencia      | Spring Data JPA (Hibernate)    |
| Base de datos     | PostgreSQL 16                  |
| Migraciones       | Flyway                         |
| Autenticación     | JWT (jjwt 0.12.6)              |
| Documentación API | SpringDoc OpenAPI (Swagger UI) |
| Contenerización   | Docker & Docker Compose        |
| Build             | Gradle                         |

---

## Arquitectura

El proyecto sigue una arquitectura por capas:

```
src/main/java/dev/jesusjimenezg/kata/
├── config/          # Configuración de la aplicación (CORS, Swagger, etc.)
├── controller/      # Controladores REST (endpoints)
├── dto/             # Objetos de transferencia de datos (request/response)
├── model/           # Entidades JPA (mapeo a tablas)
├── repository/      # Repositorios Spring Data JPA
├── security/        # Configuración de seguridad, filtros JWT, AuthEntryPoint
└── service/         # Lógica de negocio
```

- **Controladores** reciben peticiones HTTP, validan la entrada y delegan al servicio correspondiente.
- **Servicios** contienen la lógica de negocio (validaciones de solapamiento, permisos RBAC, etc.).
- **Repositorios** interactúan con la base de datos a través de Spring Data JPA.
- **Seguridad** usa filtros JWT para autenticación stateless y permisos basados en roles.
- **Migraciones** Flyway garantizan que el esquema de la base de datos se versione y aplique de forma consistente.

---

## Requisitos previos

- **Docker** y **Docker Compose** (modo recomendado)
- O bien: **Java 21**, **Gradle** y una instancia de **PostgreSQL 16** disponible

---

## Instalación y ejecución

### Con Docker Compose (recomendado)

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/jesusjimenezg/kata-backend.git
   cd kata-backend
   ```

2. Copiar el archivo de variables de entorno y ajustar si es necesario:

   ```bash
   cp .env.example .env
   ```

3. Levantar los servicios (PostgreSQL + aplicación):

   ```bash
   docker compose up --build
   ```

4. La API estará disponible en: `http://localhost:8080`

> **Nota:** Para desarrollo con recarga automática, asegúrese de que `APP_BUILD_TARGET=dev` en el archivo `.env`.

### Ejecución local sin Docker

1. Tener una instancia de PostgreSQL corriendo y configurada (ver [Variables de entorno](#variables-de-entorno)).

2. Copiar y ajustar las variables de entorno:

   ```bash
   cp .env.example .env
   source .env
   ```

   Asegúrese de que `SPRING_DATASOURCE_URL` apunte a `localhost`.

3. Compilar y ejecutar:

   ```bash
   ./gradlew bootRun
   ```

4. La API estará disponible en: `http://localhost:8080`

---

## Variables de entorno

| Variable                     | Descripción                                   | Valor por defecto                             |
| ---------------------------- | --------------------------------------------- | --------------------------------------------- |
| `POSTGRES_DB`                | Nombre de la base de datos                    | `mydatabase`                                  |
| `POSTGRES_USER`              | Usuario de PostgreSQL                         | `myuser`                                      |
| `POSTGRES_PASSWORD`          | Contraseña de PostgreSQL                      | `secret`                                      |
| `POSTGRES_PORT`              | Puerto expuesto de PostgreSQL                 | `5432`                                        |
| `APP_PORT`                   | Puerto expuesto de la aplicación              | `8080`                                        |
| `APP_BUILD_TARGET`           | Target del Dockerfile (`dev` o `prod`)        | `dev`                                         |
| `SPRING_DATASOURCE_URL`      | URL JDBC de conexión                          | `jdbc:postgresql://localhost:5432/mydatabase` |
| `SPRING_DATASOURCE_USERNAME` | Usuario JDBC                                  | `myuser`                                      |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña JDBC                               | `secret`                                      |
| `JWT_SECRET`                 | Clave secreta para firmar tokens JWT (Base64) | _(incluido en `.env.example`)_                |
| `JWT_ACCESS_EXPIRATION`      | Duración del access token en ms               | `900000` (15 min)                             |
| `JWT_REFRESH_EXPIRATION`     | Duración del refresh token en ms              | `604800000` (7 días)                          |
| `CORS_ALLOWED_ORIGINS`       | Orígenes permitidos para CORS                 | `http://localhost:8081`                       |

---

## Endpoints de la API

> Documentación completa disponible en [API_CONTRACT.md](API_CONTRACT.md) y en Swagger UI (`/swagger-ui.html`).

Todas las peticiones y respuestas usan `application/json`. Las fechas siguen formato ISO-8601 (`yyyy-MM-dd'T'HH:mm:ss`).

### Autenticación (`/api/auth`)

Los endpoints de autenticación son **públicos** (no requieren token).

| Método | Ruta                 | Descripción                    |
| ------ | -------------------- | ------------------------------ |
| POST   | `/api/auth/register` | Registro de usuario            |
| POST   | `/api/auth/login`    | Inicio de sesión (retorna JWT) |
| POST   | `/api/auth/logout`   | Cierre de sesión               |
| POST   | `/api/auth/refresh`  | Renovación del access token    |

### Tipos de recurso (`/api/resource-types`)

| Método | Ruta                       | Auth         | Descripción             |
| ------ | -------------------------- | ------------ | ----------------------- |
| GET    | `/api/resource-types`      | Bearer token | Listar tipos de recurso |
| GET    | `/api/resource-types/{id}` | Bearer token | Obtener tipo por ID     |
| POST   | `/api/resource-types`      | ADMIN        | Crear tipo de recurso   |
| PUT    | `/api/resource-types/{id}` | ADMIN        | Actualizar tipo         |
| DELETE | `/api/resource-types/{id}` | ADMIN        | Eliminar tipo           |

### Recursos (`/api/resources`)

| Método | Ruta                  | Auth         | Descripción                                              |
| ------ | --------------------- | ------------ | -------------------------------------------------------- |
| GET    | `/api/resources`      | Bearer token | Listar recursos (filtros opcionales: `active`, `typeId`) |
| GET    | `/api/resources/{id}` | Bearer token | Obtener recurso por ID                                   |
| POST   | `/api/resources`      | ADMIN        | Crear recurso                                            |
| PUT    | `/api/resources/{id}` | ADMIN        | Actualizar recurso                                       |
| DELETE | `/api/resources/{id}` | ADMIN        | Eliminar recurso (soft delete)                           |

### Reservas (`/api/reservations`)

| Método | Ruta                                                   | Auth            | Descripción                         |
| ------ | ------------------------------------------------------ | --------------- | ----------------------------------- |
| POST   | `/api/reservations`                                    | Bearer token    | Crear reserva                       |
| GET    | `/api/reservations/{id}`                               | Bearer token    | Obtener reserva por ID              |
| GET    | `/api/reservations/active`                             | Bearer token    | Listar reservas activas (globales)  |
| GET    | `/api/reservations/my`                                 | Bearer token    | Mis reservas activas                |
| GET    | `/api/reservations/my/history`                         | Bearer token    | Mi historial de reservas            |
| GET    | `/api/reservations/resource/{resourceId}/history`      | Bearer token    | Historial de reservas de un recurso |
| GET    | `/api/reservations/resource/{resourceId}/availability` | Bearer token    | Disponibilidad de un recurso        |
| PATCH  | `/api/reservations/{id}/cancel`                        | Creador o ADMIN | Cancelar reserva                    |

---

## Control de acceso basado en roles (RBAC)

La plataforma implementa un sistema de permisos que filtra la visibilidad de recursos y reservas según los roles del usuario. Los roles se incluyen en el claim `roles` del JWT.

| Rol                       | Tipos de recurso permitidos                                                 |
| ------------------------- | --------------------------------------------------------------------------- |
| `ROLE_USER`               | `ROOM`                                                                      |
| `ROLE_EMPLOYEE`           | `ROOM`, `CONFERENCE_ROOM`, `SHARED_TECH_EQUIPMENT`, `BILL_COUNTING_MACHINE` |
| `ROLE_MANAGER`            | Todos los de Employee + `VIP_ROOM`                                          |
| `ROLE_HEAD_OF_OPERATIONS` | Todos los de Manager + `CORPORATE_VEHICLE`                                  |
| `ROLE_ADMIN`              | Todos los tipos                                                             |

**Reglas:**

- Los listados solo devuelven recursos cuyos tipos estén permitidos para el rol del usuario.
- Solo se pueden crear reservas para tipos de recurso permitidos.
- Las operaciones de creación/edición/eliminación de recursos y tipos requieren `ROLE_ADMIN`.

---

## Migraciones de base de datos

Las migraciones se gestionan con **Flyway** y se encuentran en `src/main/resources/db/migration/`:

| Archivo                                  | Descripción                                      |
| ---------------------------------------- | ------------------------------------------------ |
| `V1__init.sql`                           | Esquema inicial (tablas, índices, restricciones) |
| `V2__mock_resources.sql`                 | Datos de prueba (recursos de ejemplo)            |
| `V3__role_resource_type_permissions.sql` | Permisos por rol y tipo de recurso               |
| `V4__resource_name_unique.sql`           | Restricción de nombre único en recursos          |

Las migraciones se aplican automáticamente al iniciar la aplicación.

---

## Pruebas

Ejecutar las pruebas unitarias:

```bash
./gradlew test
```

Los reportes de pruebas se generan en `build/reports/tests/test/index.html`.

---

## Documentación interactiva (Swagger)

Una vez la aplicación esté corriendo, la documentación interactiva de la API está disponible en:

```
http://localhost:8080/swagger-ui.html
```

Desde Swagger UI se pueden explorar y probar todos los endpoints directamente desde el navegador.

---

## Colección de Postman

El archivo `kata-backend.postman_collection.json` contiene una colección completa de Postman con todos los endpoints de la API preconfigurados. Importarlo en Postman para probar rápidamente la API.

---

## Decisiones de arquitectura

1. **Spring Boot 4 + Java 21**: se eligió la versión más reciente del framework para aprovechar las mejoras de rendimiento y las nuevas características del lenguaje (records, pattern matching, etc.).
2. **PostgreSQL**: base de datos relacional robusta, ideal para manejar restricciones de integridad, índices y consultas complejas de disponibilidad.
3. **Flyway**: control de versiones del esquema de base de datos, permitiendo migraciones reproducibles y trazables.
4. **JWT stateless**: autenticación sin estado en el servidor, facilitando escalabilidad horizontal.
5. **RBAC granular**: permisos por tipo de recurso según el rol, implementado tanto a nivel de consultas SQL como en la capa de servicios.
6. **Soft delete en recursos**: los recursos se desactivan en lugar de eliminarse físicamente, preservando la integridad referencial de las reservas históricas.
7. **Docker multi-stage**: el Dockerfile incluye targets `dev` (con hot-reload) y `prod` (JAR optimizado), facilitando tanto el desarrollo como el despliegue.
8. **Validación de solapamientos**: la lógica de prevención de reservas duplicadas se aplica tanto a nivel de servicio como con restricciones en base de datos.

---

## Uso de IA

- 2026-02-11: Inicialización del esquema de base de datos siguiendo las instrucciones provistas (tablas, roles, índices, restricciones).
- 2026-02-11: Se usó la IA para instanciar el proyecto, crear las reglas e instrucciones para el agente, y en cada commit para redactar mensajes mejorados.
- 2026-02-11: Se utilizó la IA de manera eficiente en la depuración de errores relacionados a Docker y el proceso de construcción de los contenedores para optimizar el tiempo.
- 2026-02-11: Se utilizó la IA para generar el boilerplate de los modelos y repositorios de la aplicación, según el esquema de la base de datos, siguiendo las mejores prácticas de PostgreSQL y JPA/Hibernate.
- 2026-02-11: Se utilizó la IA para inicializar la configuración de seguridad (autenticación y autorización), controladores y servicios de autenticación con JWT.
- 2026-02-11: Se utilizó la IA para documentar los endpoints de autenticación en Swagger/OpenAPI con descripciones detalladas y esquemas de respuesta.
- 2026-02-11: Se utilizó IA para generar todo el boilerplate CRUD de Recursos y Reservas (DTOs, Servicios, Controladores).
- 2026-02-11: Se utilizó la IA para generar el script de migración con datos de prueba (mock data).
- 2026-02-11: Se utilizó IA para generar el contrato de la API y la colección de Postman para facilitar la integración con clientes.
- 2026-02-11: Se utilizó IA para implementar permisos avanzados basados en roles, incluyendo la lógica de filtrado tanto a nivel de base de datos como en la capa de servicios y controladores de la aplicación.
- 2026-02-11: Se utilizó IA para generar instrucciones personalizadas para la configuración de CORS y actualizar el archivo .env.example.
- 2026-02-11: Se utilizó IA para generar la documentación completa del README en español.
