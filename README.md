# Ferretería Los Maestros — Backend

Backend basado en microservicios con Spring Boot para la plataforma de comercio y gestión de Ferretería Los Maestros.

> Estado actual: proyecto académico en integración. El backend contiene los servicios principales, pero algunos contratos y controles de seguridad documentados todavía están en validación. Consulta `GUIA_FINAL_BACKEND.md` para el alcance pendiente y los criterios de aceptación.

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

## Tecnologías

- Java 21
- Spring Boot 3.3.5
- Spring Cloud 2023.0.4 / Spring Cloud Gateway
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

| Componente | Puerto local |
|---|---:|
| API Gateway | 8080 |
| Auth Service | 8081 |
| Product Service | 8082 |
| Order Service | 8083 |
| User Service | 8084 |
| Catalog Service | 8085 |
| Cart Service | 8086 |
| PostgreSQL Auth | 5431 |
| PostgreSQL Product | 5432 |
| PostgreSQL Order | 5433 |
| PostgreSQL User | 5437 (5434 reservado por otro entorno Docker) |
| PostgreSQL Catalog | 5435 |
| PostgreSQL Cart | 5436 |
| RabbitMQ AMQP | 5672 |
| RabbitMQ Management | 15672 |

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

Genera un secreto JWT seguro:

```bash
openssl rand -base64 32
```

Asigna el resultado a `JWT_SECRET` en `.env`. El archivo `.env` está excluido por Git y no debe subirse al repositorio.

Variables principales:

```env
JWT_SECRET=<secreto-base64>
JWT_ISSUER_URI=http://localhost:8081
JWT_JWK_SET_URI=http://localhost:8081/.well-known/jwks.json
DB_USER=postgres
DB_PASS=postgres
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASS=guest
CATALOG_SERVICE_URL=http://localhost:8085
PRODUCT_SERVICE_URL=http://localhost:8082
```

El conjunto completo disponible está en `.env.example`.

> Importante: las credenciales conocidas actuales son apropiadas únicamente para desarrollo local. Antes de desplegar, reemplázalas y elimina cualquier valor predeterminado conocido.

## Ejecución con Docker Compose

Desde la raíz del proyecto:

```bash
docker compose up --build
```

Levantar en segundo plano:

```bash
docker compose up --build -d
```

Detener los contenedores:

```bash
docker compose down
```

Eliminar también los volúmenes locales:

```bash
docker compose down -v
```

El orden definido incluye PostgreSQL y RabbitMQ, seguido de los servicios de aplicación y el gateway. RabbitMQ Management queda disponible en:

- <http://localhost:15672>
- usuario: `guest`
- contraseña: `guest`

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

En Windows, usa `mvnw.cmd`.

## Compilar y probar

Compilar todos los módulos y ejecutar las pruebas:

```bash
./mvnw clean verify
```

Probar un módulo específico:

```bash
./mvnw test -pl product-service
```

Limpiar artefactos Maven:

```bash
./mvnw clean
```

## Acceso a las APIs

Todas las rutas públicas se consumen preferentemente a través del gateway:

```text
http://localhost:8080/api
```

Las rutas principales son:

| Dominio | Ruta |
|---|---|
| Autenticación | `/api/auth/**` |
| Productos | `/api/products/**` |
| Categorías e imágenes | `/api/categories/**`, `/api/images/**` |
| Pedidos y pagos | `/api/orders/**`, `/api/payments/**` |
| Usuarios | `/api/users/**`, `/api/usuarios/**` |
| Direcciones | `/api/usuarios/{usuarioId}/direcciones/**` |
| Cuenta corriente | `/api/cuentas-corrientes/**` |
| Regiones y comunas | `/api/regions/**`, `/api/communes/**` |
| Cobertura de despacho | `/api/coverage/**` |
| Carrito | `/api/cart/**` |

Los endpoints privados esperan un access token:

```http
Authorization: Bearer <accessToken>
```

Ejemplo de autenticación:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"usuario@example.com","password":"ContraseñaSegura123!"}'
```

Respuesta esperada: JSON con `accessToken` y `refreshToken`. Los nombres exactos deben confirmarse en la implementación de `AuthController` antes de integrar el frontend.

## Swagger/OpenAPI

Cada servicio expone SpringDoc en su puerto directo, normalmente:

```text
http://localhost:<puerto>/api/swagger-ui.html
```

La especificación OpenAPI suele estar disponible en:

```text
http://localhost:<puerto>/api/v3/api-docs
```

Ejemplos:

- <http://localhost:8081/api/swagger-ui.html>
- <http://localhost:8082/api/swagger-ui.html>
- <http://localhost:8080/actuator/gatewayroutes>

Swagger puede deshabilitarse o protegerse en producción.

## Migraciones de base de datos

Flyway ejecuta automáticamente las migraciones ubicadas en:

```text
*/src/main/resources/db/migration/
```

Las bases se crean desde las variables de Docker Compose. Los volúmenes preservan los datos locales entre reinicios.

## Salud y diagnóstico

Gateway:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/gatewayroutes
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
- Asociar la identidad del usuario al JWT y no confiar en `userId` enviado por el cliente en operaciones sensibles.
- Usar `Idempotency-Key` para operaciones de compra repetibles cuando el contrato esté implementado completamente.

## Estado conocido y riesgos

Antes de publicar o desplegar se deben revisar especialmente:

- secretos y contraseñas conocidas en configuración local;
- exposición de rutas internas a través del gateway;
- eliminación de cabeceras internas falsas recibidas del cliente;
- autorización por propietario en usuarios, carrito y pedidos;
- concurrencia de stock y cuentas corrientes;
- idempotencia real de pedidos;
- integraciones HTTP y eventos RabbitMQ;
- consistencia de precios, estados y pagos.

La hoja de trabajo completa está en `GUIA_FINAL_BACKEND.md`.

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

El `.gitignore` actual excluye `.env`, `target/` y artefactos de compilación.
