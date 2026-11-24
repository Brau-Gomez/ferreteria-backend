# Ferretería Los Maestros — Backend

Backend basado en microservicios con Spring Boot para la plataforma de comercio y gestión de Ferretería Los Maestros. Proyecto académico (DuocUC, DSY1104 Desarrollo Fullstack II, EP3).

## Arquitectura

- `api-gateway`: entrada pública, validación JWT, CORS y enrutamiento.
- `auth-service`: registro, login, refresh, logout y emisión de JWT.
- `product-service`: productos, categorías, imágenes y stock.
- `order-service`: pedidos, estados y pagos.
- `user-service`: usuarios, direcciones, cuentas corrientes, abonos y movimientos.
- `catalog-service`: regiones, comunas y coberturas de despacho.
- `cart-service`: carrito persistente por usuario.
- PostgreSQL: una base de datos independiente por dominio.
- RabbitMQ: comunicación asíncrona entre servicios.

Roles del sistema: `ADMIN`, `VENDEDOR` y `CLIENTE` (el cliente puede ser un particular o un contratista).

## Tecnologías

- Java 21
- Spring Boot 3.5.16
- Spring Cloud 2025.0.3 / Spring Cloud Gateway
- Spring Security y OAuth2 Resource Server
- JJWT 0.12.6
- Maven Wrapper
- PostgreSQL 16
- RabbitMQ 3
- Docker y Docker Compose
- Flyway
- MapStruct y Lombok
- SpringDoc OpenAPI

## Puertos

Con Docker Compose, los servicios se publican en estos puertos del equipo:

| Componente      | Puerto con Docker Compose | Puerto sin Docker |
| --------------- | ------------------------: | ----------------: |
| API Gateway     |                      8180 |              8080 |
| Auth Service    |                      8181 |              8081 |
| Product Service |                      8182 |              8082 |
| Order Service   |                      8183 |              8083 |
| User Service    |                      8184 |              8084 |
| Catalog Service |                      8185 |              8085 |
| Cart Service    |                      8186 |              8086 |

| Infraestructura     |                                  Puerto local |
| ------------------- | --------------------------------------------: |
| PostgreSQL Auth     |                                          5431 |
| PostgreSQL Product  |                                          5432 |
| PostgreSQL Order    |                                          5433 |
| PostgreSQL User     | 5437 (5434 reservado por otro entorno Docker) |
| PostgreSQL Catalog  |                                          5435 |
| PostgreSQL Cart     |                                          5436 |
| RabbitMQ AMQP       |                                          5672 |
| RabbitMQ Management |                                         15672 |

## Requisitos

- Java 21
- Docker Desktop o Docker Engine con Compose
- Git

No es necesario instalar Maven: el proyecto incluye `mvnw` y `mvnw.cmd`.

## Configuración

Copia la configuración de ejemplo:

```bash
cp .env.example .env
```

En Windows (PowerShell):

```powershell
Copy-Item .env.example .env
```

Edita `.env` y completa los valores obligatorios. El archivo `.env` está excluido por Git y no debe subirse al repositorio.

> Importante: `JWT_SECRET` debe estar en **Base64** (solo letras, números, `+`, `/` y `=`). Un valor con guiones o espacios hace que `auth-service` no arranque (`Illegal base64 character`). En `.env` no escribas comentarios después del `=`.

Genera un secreto válido:

```bash
openssl rand -base64 48
```

Variables obligatorias para Docker Compose:

```env
JWT_SECRET=<secreto-base64>
DB_USER=ferreteria
DB_PASS=<contraseña-local>
RABBITMQ_USER=ferreteria
RABBITMQ_PASS=<contraseña-local>
INTERNAL_API_SECRET=<secreto-interno>
```

`ADMIN_PASSWORD` y `DEMO_PASSWORD` pueden quedar vacías: en ese caso se conservan las contraseñas demo de las migraciones. El conjunto completo de variables está en `.env.example`.

> Las credenciales de este README son solo para desarrollo local. Antes de desplegar, reemplázalas por valores propios.

## Ejecución con Docker Compose

Desde la raíz del proyecto:

```bash
docker compose up --build
```

La primera vez demora varios minutos porque compila los 7 servicios. Levantar en segundo plano:

```bash
docker compose up --build -d
```

Detener los contenedores:

```bash
docker compose down
```

Eliminar también los volúmenes locales (borra los datos):

```bash
docker compose down -v
```

Los servicios de aplicación esperan a que PostgreSQL y RabbitMQ estén saludables. En los logs de PostgreSQL pueden aparecer líneas `FATAL: database "ferreteria" does not exist`: son del healthcheck y no afectan el funcionamiento.

RabbitMQ Management queda disponible en <http://localhost:15672> con el usuario y la contraseña definidos en `RABBITMQ_USER` y `RABBITMQ_PASS`.

## Ejecución local sin Docker

Levanta una instancia de PostgreSQL por dominio y RabbitMQ. Después, ajusta las variables de entorno y ejecuta cada módulo desde la raíz:

```bash
./mvnw spring-boot:run -pl auth-service
./mvnw spring-boot:run -pl product-service
./mvnw spring-boot:run -pl order-service
./mvnw spring-boot:run -pl user-service
./mvnw spring-boot:run -pl catalog-service
./mvnw spring-boot:run -pl cart-service
./mvnw spring-boot:run -pl api-gateway
```

En Windows, usa `.\mvnw.cmd`.

## Compilar y probar

Compilar todos los módulos y ejecutar las pruebas:

```bash
./mvnw clean verify
```

Probar un módulo específico:

```bash
./mvnw test -pl product-service
```

El repositorio incluye un flujo de GitHub Actions (`.github/workflows/ci.yml`) que compila, ejecuta las pruebas y construye la imagen Docker de cada servicio en cada push y pull request a `master`.

## Usuarios de demostración

| Rol      | Correo                      | Contraseña |
| -------- | --------------------------- | ---------- |
| ADMIN    | `admin@duoc.cl`             | `demo123`  |
| VENDEDOR | `vendedor@profesor.duoc.cl` | `demo123`  |
| CLIENTE  | `cliente@gmail.com`         | `demo123`  |

Estos valores aplican cuando `ADMIN_PASSWORD` y `DEMO_PASSWORD` están vacías en `.env`. Si las defines, los usuarios semilla usan esas contraseñas.

## Acceso a las APIs

Todas las rutas se consumen a través del gateway:

```text
http://localhost:8180/api
```

Las rutas principales son:

| Dominio               | Ruta                                       |
| --------------------- | ------------------------------------------ |
| Autenticación         | `/api/auth/**`                             |
| Productos             | `/api/products/**`                         |
| Categorías e imágenes | `/api/categories/**`, `/api/images/**`     |
| Pedidos y pagos       | `/api/orders/**`, `/api/payments/**`       |
| Usuarios              | `/api/users/**`, `/api/usuarios/**`        |
| Direcciones           | `/api/usuarios/{usuarioId}/direcciones/**` |
| Cuenta corriente      | `/api/cuentas-corrientes/**`               |
| Regiones y comunas    | `/api/regions/**`, `/api/communes/**`      |
| Cobertura de despacho | `/api/coverage/**`                         |
| Carrito               | `/api/cart/**`                             |

Las rutas `v2` (`/api/products/v2`, `/api/users/v2`, `/api/auth/v2`, regiones, comunas y cobertura) devuelven respuestas HATEOAS de solo lectura.

Rutas públicas (sin token): login, registro, refresh, y las consultas `GET` de productos, categorías, imágenes, regiones, comunas y cobertura. El resto exige un access token:

```http
Authorization: Bearer <accessToken>
```

Ejemplo de autenticación:

```bash
curl -X POST http://localhost:8180/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@duoc.cl","password":"demo123"}'
```

Respuesta esperada: JSON con `accessToken` y `refreshToken`.

## Swagger/OpenAPI

Cada servicio expone SpringDoc en su puerto directo:

| Servicio | Swagger UI (Docker Compose)                 |
| -------- | ------------------------------------------- |
| Auth     | <http://localhost:8181/api/swagger-ui.html> |
| Product  | <http://localhost:8182/api/swagger-ui.html> |
| Order    | <http://localhost:8183/api/swagger-ui.html> |
| User     | <http://localhost:8184/api/swagger-ui.html> |
| Catalog  | <http://localhost:8185/api/swagger-ui.html> |
| Cart     | <http://localhost:8186/api/swagger-ui.html> |

La especificación OpenAPI está en `http://localhost:<puerto>/api/v3/api-docs`.

Para probar endpoints protegidos desde Swagger:

1. En Swagger de `auth-service`, ejecuta `POST /auth/login` y copia el `accessToken`.
2. En cualquier Swagger, presiona **Authorize**, pega el token **sin** la palabra `Bearer` y confirma.
3. Ejecuta el endpoint protegido. Sin token responde `401`.

## Migraciones de base de datos

Flyway ejecuta automáticamente las migraciones ubicadas en:

```text
*/src/main/resources/db/migration/
```

Las bases se crean desde las variables de Docker Compose. Los volúmenes preservan los datos locales entre reinicios.

## Salud y diagnóstico

Gateway:

```bash
curl http://localhost:8180/actuator/health
```

Ver contenedores:

```bash
docker compose ps
```

Ver logs de un servicio:

```bash
docker compose logs -f auth-service
```

## Convenciones de integración

- Consumir las APIs desde `/api` a través del gateway.
- Enviar `Authorization: Bearer <accessToken>` en rutas privadas.
- Tratar los UUID como cadenas.
- No enviar precios, nombres de producto, IVA ni costo de envío calculado por el cliente.
- La identidad del usuario se toma del JWT; no se confía en un `userId` enviado por el cliente en operaciones sensibles.

## Preparación de commits

Antes de confirmar cambios:

```bash
git status
git diff --stat
./mvnw clean verify
```

No confirmar:

- `.env`;
- directorios `target/`;
- archivos `*.jar`, `*.war` o `*.class`;
- credenciales reales;
- volúmenes o datos locales.
