# Guía final para terminar el backend y conectarlo al frontend

## 0. Objetivo y alcance

Este documento define el trabajo necesario para dejar el backend listo para integración real con el frontend. Está organizado por fases. La numeración indica el orden recomendado: no se deben implementar mejoras menores mientras existan problemas críticos abiertos.

### Alcance funcional esperado

El backend debe soportar:

- Registro, inicio de sesión, renovación y cierre de sesión.
- Perfiles y direcciones de usuario.
- Roles: administrador, vendedor y cliente/contratista según la nomenclatura real del proyecto.
- Productos, categorías, imágenes y stock.
- Catálogo de regiones y comunas.
- Cobertura de despachos.
- Carrito persistente por usuario.
- Creación de pedidos.
- Pagos, estados de pedido y cancelaciones.
- Cuentas corrientes, cargos, abonos, movimientos e intereses.
- Comunicación entre microservicios mediante HTTP y RabbitMQ.

### Convenciones de integración

- Todas las rutas públicas se consumen a través del API Gateway en `http://localhost:8080/api`.
- El frontend debe enviar `Authorization: Bearer <accessToken>` salvo en login, registro, refresh y recursos públicos explícitos.
- Todos los UUID deben respetarse como cadenas.
- Los importes monetarios deben viajar como cadenas o como números, pero una vez acordado el contrato no debe haber cambios por servicio.
- Para operaciones que puedan repetirse se debe enviar `Idempotency-Key`.
- Los errores deben usar el formato disponible en `GlobalExceptionHandler` de cada servicio y no exponer detalles técnicos internos.
- La carga asumida es de múltiples usuarios y procesos concurrentes. No basta con que una operación funcione una sola vez.

### Criterio general de terminado

Una fase termina cuando:

1. El contrato HTTP está documentado.
2. Existe una prueba automatizada del flujo normal.
3. Existe una prueba de rechazo o error.
4. Existe una prueba de concurrencia o reintento si la operación modifica dinero o stock.
5. La operación protege datos ajenos.
6. Docker Compose arranca todos los servicios y las migraciones.

---

# Fase 1. Seguridad, identidad y secretos

## 1.1 Eliminar secretos conocidos

### Archivos relacionados

- `docker-compose.yml`
- `.env.example`
- `api-gateway/src/main/resources/application.properties`
- `auth-service/src/main/resources/application-dev.properties`
- Archivos `application.yml` de los servicios

### Problema

PostgreSQL, RabbitMQ y JWT tienen valores predeterminados conocidos. El mismo JWT secreto se usa por defecto en Compose y el gateway.

### Implementación

1. Eliminar credenciales predeterminadas de credenciales sensibles.
2. Exigir variables de entorno para Docker Compose.
3. Para desarrollo, crear un `.env` local basado en `.env.example`.
4. Mantener `.env` fuera de Git.
5. Generar un secreto JWT con al menos 32 bytes aleatorios:

```bash
openssl rand -base64 32
```

6. Configurar el mismo secreto únicamente en auth-service y API Gateway.
7. Configurar contraseñas distintas para cada base PostgreSQL si el entorno deja de ser local.
8. Cambiar las credenciales predeterminadas de RabbitMQ.
9. Revisar el historial Git antes de publicar si alguna credencial real fue versionada.

### Ejemplo de configuración local segura

```env
JWT_SECRET=<valor-base64-generado>
DB_USER=postgres
DB_PASS=<contraseña-local>
RABBITMQ_USER=ferreteria
RABBITMQ_PASS=<contraseña-local>
```

No agregar valores reales a `.env.example` ni al Compose.

### Criterio de aceptación

- Compose falla o advierte si faltan secretos críticos.
- Los valores conocidos no aparecen en código o configuración de producción.
- Auth-service y gateway aceptan los mismos tokens después de configurar una sola clave.
- Las bases y RabbitMQ no aceptan las credenciales predeterminadas `guest` o `postgres/postgres` en producción.

## 1.2 Proteger rutas internas

### Archivos relacionados

- `api-gateway/src/main/java/com/ferreteria/api_gateway/security/JwtAuthenticationFilter.java`
- `user-service/src/main/java/com/ferreteria/user_service/controller/InternalPerfilUsuarioController.java`
- `user-service/src/main/resources/application.yml`

### Problema

El gateway marca `/api/internal/users/**` como ruta pública y no garantiza que esas llamadas provengan de otro servicio.

### Implementación mínima

Opción recomendada para el backend actual:

1. No publicar las rutas internas a través del API Gateway público.
2. Configurar el gateway para que no tenga una ruta pública hacia `/internal/**`.
3. El servicio de usuario debe aceptar esas llamadas solo desde la red interna o mediante una credencial de servicio.
4. Si se conserva una API interna HTTP, usar un secreto compartido independiente del JWT de usuarios y validar un header específico en el servicio destino.
5. No permitir que el frontend conozca esa ruta.

### Criterio de aceptación

- Una llamada desde Internet a `/api/internal/users/**` retorna `401` o `404`.
- Solo el auth-service puede crear el perfil interno.
- No se pueden crear perfiles internos indicando datos arbitrarios sin autenticación de servicio.

## 1.3 Limpiar cabeceras internas de identidad

### Archivo relacionado

- `api-gateway/src/main/java/com/ferreteria/api_gateway/security/JwtAuthenticationFilter.java`

### Problema

El gateway agrega `X-User-Subject`, `X-User-Email` y `X-User-Roles`, pero no elimina versiones anteriores enviadas por el cliente.

### Implementación

1. Antes de agregar las cabeceras internas, eliminar todas las cabeceras del mismo nombre recibidas por el cliente.
2. Agregar las nuevas cabeceras desde claims validados del JWT.
3. Limitar cada servicio a usar las cabeceras internas solo cuando haya llegado desde el gateway o aplicar directamente el JWT.
4. No aceptar que el servicio confíe en cabeceras enviadas directamente por el puerto publicado.

### Criterio de aceptación

- Si el cliente envía `X-User-Subject: otro-usuario`, el gateway reemplaza o elimina el valor antes del reenvío.
- Una llamada directa al puerto interno no puede suplantar identidad si el usuario no tiene un JWT válido.

## 1.4 Unificar roles

### Archivos relacionados

- Controladores de todos los microservicios.
- Migración `auth-service/src/main/resources/db/migration/V1__create_auth_schema.sql`.
- Modelo de usuario de auth-service y user-service.

### Problema actual

Conviven roles con nombres distintos:

- `ADMIN` y `ADMINISTRADOR`.
- `VENDEDOR`.
- Roles diferentes en auth-service y user-service.

Esto puede provocar que un usuario sea reconocido por un microservicio pero no por otro.

### Implementación

Elegir una nomenclatura oficial antes de seguir. Se recomienda:

- `ADMIN`
- `VENDEDOR`
- `CONTRATISTA`
- `CLIENTE`

Si el frontend ya usa `ADMINISTRADOR`, documentar explícitamente la traducción y corregir controladores hasta que haya una única cadena.

No hacer traducciones distintas en cada servicio. La traducción debe suceder una sola vez, idealmente al emitir el JWT.

### Criterio de aceptación

- Un usuario con el mismo UUID tiene el mismo rol en todos los servicios.
- No hay combinaciones de `hasRole('ADMIN')` y `hasRole('ADMINISTRADOR')` ambiguas.
- El frontend recibe la lista oficial de roles.

## 1.5 Probar el ciclo de autenticación

### Archivos relacionados

- `auth-service/src/main/java/com/ferreteria/ms_auth/controller/AuthController.java`
- `AuthService.java`
- `RefreshTokenRepository.java`

### Implementación

1. Registro.
2. Login.
3. Validación del access token.
4. Obtención de perfil.
5. Refresh de tokens.
6. Rotación del refresh token.
7. Logout.
8. Cambio o migración de contraseña.

### Contratos mínimos

`POST /api/auth/register`

```json
{
  "username": "cliente1",
  "email": "cliente1@example.com",
  "password": "ContraseñaSegura123!",
  "fullName": "Cliente Uno",
  "phone": "+56900000000"
}
```

`POST /api/auth/login`

```json
{
  "email": "cliente1@example.com",
  "password": "ContraseñaSegura123!"
}
```

El frontend debe guardar el access token y el refresh token según el contrato de respuesta, no reconstruir datos del usuario desde claims.

### Casos de prueba

- Contraseña incorrecta.
- Usuario inexistente.
- Email duplicado.
- Refresh inválido.
- Refresh expirado.
- Refresh revocado.
- Reutilización del mismo refresh: se debe rechazar y revocar la sesión asociada si ya fue rotado.
- Logout seguido de refresh.
- Cambio de contraseña revoca sesiones existentes.

### Criterio de aceptación

- No hay refresh reutilizable después de un refresh exitoso.
- Logout revoca el refresh indicado.
- Un access token vencido no pasa filtros internos.
- El login no revela si un email existe en casos de credenciales inválidas si eso está dentro del alcance de privacidad.

---

# Fase 2. Contratos de usuarios y autorización por propietario

## 2.1 Definir la fuente oficial de identidad

El JWT `sub` debe ser la identidad canónica. El frontend no debe enviar el ID del propietario en operaciones sensibles.

El backend debe derivar la identidad del JWT para:

- Crear perfil.
- Consultar perfil propio.
- Actualizar perfil propio.
- Crear o consultar carrito.
- Crear pedido.
- Consultar pedidos propios.
- Cancelar pedido propio.
- Consultar direcciones propias.
- Consultar cuenta corriente propia.

Un administrador puede consultar otros usuarios únicamente mediante UUID y autorización explícita.

## 2.2 Proteger usuarios

### Archivos relacionados

- `user-service/src/main/java/com/ferreteria/user_service/controller/UsuarioController.java`
- `DireccionController.java`
- `CuentaCorrienteController.java`
- `AbonoController.java`
- `MovimientoCuentaCorrienteController.java`

### Implementación

Agregar permisos por propietario en cada operación:

- `GET /usuarios/{id}`: dueño o administrador.
- `PUT /usuarios/{id}`: dueño o administrador.
- Cambio de estado: administrador.
- Cambio de rol: administrador.
- Eliminación: administrador, preferiblemente baja lógica.
- Dirección: dueño o administrador.
- Cuenta corriente: dueño o administrador/vendedor autorizado.
- Movimientos: dueño o administrador/vendedor autorizado.
- Abonos: dueño para crear/consultar según diseño acordado; administrador/vendedor para aplicar/rechazar/anular.

### Criterio de aceptación

- Usuario A no puede leer ni modificar usuario B cambiando el UUID.
- Las rutas de usuario no pueden ser llamadas sin JWT salvo las rutas públicas expresamente acordadas.
- El endpoint no acepta un `procesadoPor` libre; se obtiene del JWT.

## 2.3 Cerrar la creación pública de usuarios

Los controles actuales permiten crear usuarios desde `/api/users` o `/api/usuarios` sin autorización explícita. Esto puede duplicar el registro realizado por auth-service.

### Implementación

Elegir una única fuente:

1. Registro público: auth-service crea usuario auth y publica evento.
2. user-service crea perfil por `InternalPerfilUsuarioController` al recibir el evento o llamada interna.

Como mínimo:

- Marcar `POST /usuarios` como interno o administrativo.
- Evitar que dos fuentes puedan crear el mismo perfil con datos inconsistentes.
- Revisar una clave única por UUID o email.

### Criterio de aceptación

- Registrar una cuenta crea un perfil coherente una sola vez.
- Una llamada pública duplicada no crea dos perfiles diferentes.
- El frontend no necesita llamar directamente a auth-service y user-service para registrar.

## 2.4 Bajas lógicas y borrados

### Archivos relacionados

- `UsuarioService.java`
- `CuentaCorrienteService.java`
- `ProductService.java`

### Implementación

- Usuario eliminado: pasar a estado inactivo, no borrar el registro histórico.
- Cuenta corriente eliminada: prohibir eliminar si tiene saldo usado o movimientos/abonos; preferir estado cerrado.
- Producto eliminado: considerar baja lógica para preservar líneas históricas de pedidos.
- Categoría eliminada: verificar productos asociados.

### Criterio de aceptación

- El borrado no destruye movimientos históricos ni pedidos.
- Los recursos inactivos quedan fuera de búsqueda pública.
- ADMIN puede auditar que una entidad fue cerrada o eliminada.

---

# Fase 3. Producto, catálogo y stock

## 3.1 Crear contratos reales de producto

### Archivos relacionados

- `ProductController.java`
- `ProductService.java`
- `ProductRequestDTO.java`
- `ProductResponseDTO.java`
- `ProductRepository.java`

### Implementación

El frontend necesita operaciones claras:

- Listar productos paginados.
- Filtrar por texto, categoría y rango de precio.
- Consultar producto por ID.
- Consultar producto por código.
- Crear/editar/desactivar producto.
- Modificar stock.
- Agregar/eliminar imágenes.

### Reglas

- El precio debe validar `>= 0` y escala consistente.
- El código debe ser único.
- El stock no puede quedar negativo.
- El stock mínimo no puede superar el máximo si ambos están definidos.
- El producto inactivo no puede agregarse al carrito ni venderse.
- Los endpoints públicos deben mostrar solo activos en listados públicos.
- Las imágenes deben tener `url`, `altText`, `sortOrder` y `primary`.

### Criterio de aceptación

- No se puede crear un producto sin categoría real.
- No se puede actualizar el código a uno ya usado.
- Stock negativo retorna error antes de guardar.
- Solo aparece una imagen primaria por producto.

## 3.2 Resolver concurrencia de stock

### Problema

`actualizarStock` suma o resta stock sin bloqueo pesimista ni reserva transaccional.

### Implementación mínima

1. Crear un método de repositorio que consulte el producto con bloqueo pesimista al reservar stock.
2. Crear una reserva por pedido con cantidad y estado.
3. Al crear el pedido:
   - bloquear cada producto;
   - validar existencia, activo y stock;
   - obtener precio real;
   - disminuir stock o crear una reserva;
   - guardar el pedido y sus items en la misma transacción.
4. Al cancelar:
   - liberar stock una sola vez;
   - conservar el estado CANCELLED.
5. Al cambiar stock desde administración:
   - registrar movimiento previo y nuevo si se necesita auditoría.

Si no se implementa reserva diferida, al menos usar bloqueo pesimista y descontar stock dentro de la creación del pedido.

### Criterio de aceptación

- Dos solicitudes simultáneas no pueden vender más unidades que el stock disponible.
- Cancelar dos veces no libera stock dos veces.
- El precio guardado en el pedido no cambia si el producto modifica su precio después.

## 3.3 Completar catálogo de despacho

### Archivos relacionados

- `catalog-service/src/main/java/com/ferreteria/catalog_service/controller/*`
- `RegionService.java`
- `ComunaService.java`
- `CoberturaDespachoService.java`

### Implementación

El frontend debe poder:

- Listar regiones públicas.
- Listar comunas por región.
- Consultar una comuna.
- Consultar cobertura por comuna.
- Administrar regiones/comunas/cobertura.

### Reglas

- Región activa por defecto.
- Comuna pertenece a una región real.
- Cobertura no duplica región/comuna.
- No se pueden crear coberturas duplicadas.
- Los GET públicos no exponen operaciones administrativas.

### Criterio de aceptación

- Región/comuna inexistente retorna 404.
- Cobertura inexistente o inactiva retorna una respuesta estable.
- Crear o editar requiere `ADMIN`.

---

# Fase 4. Carrito persistente

## 4.1 Completar contrato del carrito

### Archivos relacionados

- `cart-service/src/main/java/com/ferreteria/cart_service/controller/CartController.java`
- `CartService.java`
- `ProductClient.java`
- `CartItemRepository.java`

### Implementación

El carrito debe:

- Obtenerse por identidad del JWT.
- Agregar item por código.
- Incrementar cantidad si ya existe el mismo código.
- Actualizar cantidad.
- Eliminar item.
- Vaciar carrito.

### Reglas

- Nunca aceptar `userId` desde el body o query.
- Consultar el producto real antes de agregar o actualizar.
- Validar activo y stock.
- Guardar el precio actual en el item para mostrarlo estable mientras esté en el carrito.
- Recalcular el precio al entrar al checkout contra product-service.

### Criterio de aceptación

- Usuario A no puede leer ni modificar el carrito de usuario B.
- Agregar 2 unidades a una existente crea una sola fila con cantidad 4.
- Actualizar una cantidad mayor al stock retorna 400.
- Vaciar el carrito no modifica stock.

## 4.2 Evitar llamadas HTTP innecesarias

Cada item del carrito no debe consultar el producto por una ruta nueva si se puede resolver en lote.

No es crítico al inicio, pero si el carrito crece, debe existir una consulta por lote desde product-service.

### Criterio de aceptación

- Obtener el carrito no ejecuta una consulta HTTP por item en el caso normal.
- El tamaño de página o del carrito tiene límites definidos.

---

# Fase 5. Pedidos: el flujo más crítico

## 5.1 Crear el pedido con datos reales

### Archivo principal

- `order-service/src/main/java/com/ferreteria/order_service/service/OrderService.java`

### Problema

El pedido usa precios ficticios, nombres ficticios, IVA fijo y envío mock.

### Implementación

1. Crear una integración desde order-service hacia product-service.
2. Crear una integración hacia catalog-service para validar comuna y costo de envío.
3. Para cada item recibido:
   - validar código;
   - obtener producto activo;
   - validar stock;
   - obtener precio real;
   - guardar nombre y precio al momento de compra;
   - calcular el subtotal real.
4. Validar dirección y comuna.
5. Calcular impuesto con la política fiscal acordada.
6. Calcular envío con la tarifa acordada por comuna.
7. Calcular el total como subtotal + impuestos + envío.
8. Guardar items y pedido en la misma transacción.
9. Descontar o reservar stock.
10. Publicar el evento después del commit.

### Campos mínimos del pedido

```json
{
  "items": [
    {
      "productCode": "TORN-001",
      "quantity": 2
    }
  ],
  "shippingAddress": "Calle ejemplo 123",
  "shippingCommuneId": "<uuid-comuna>",
  "shippingRegionId": "<uuid-region>",
  "notes": "Dejar en portería"
}
```

El frontend no debe enviar precio, nombre ni IVA.

### Criterio de aceptación

- El nombre y precio del item son los valores reales obtenidos por backend.
- Producto inexistente retorna 400 o 404 antes de crear el pedido.
- Producto inactivo no puede venderse.
- Stock insuficiente no crea pedido.
- Dirección/comuna inconsistente no crea pedido.
- El pedido queda asociado al usuario del JWT.

## 5.2 Implementar idempotencia real

### Archivos relacionados

- `OrderService.java`
- `OrderRepository.java`
- `V1__create_order_schema.sql`

### Implementación

1. El frontend envía `Idempotency-Key` con un UUID por intención de compra.
2. La clave debe ser única por usuario y operación.
3. Agregar restricción única en base de datos.
4. Si la clave ya existe:
   - devolver la orden original si pertenece al mismo usuario;
   - rechazar si pertenece a otro usuario.
5. Si la clave se está procesando, la segunda solicitud espera o obtiene un estado explícito `PROCESSING`.
6. No usar solo `existsByIdempotencyKey`.

Si no se desea reutilizar `idempotency_keys`, al menos agregar `UNIQUE(user_id, idempotency_key)` a `orders`.

### Criterio de aceptación

- Dos solicitudes simultáneas con la misma clave crean una sola orden.
- Reintentar el mismo pedido retorna la misma respuesta.
- Reintentar con otra cuenta no reutiliza el pedido ajeno.

## 5.3 Validar estados de pedido

### Archivo relacionado

- `OrderService.java:257-260`

### Implementación

Definir estados permitidos:

- `PENDING`
- `CONFIRMED`
- `SHIPPED`
- `DELIVERED`
- `CANCELLED`

Ejemplo mínimo de transiciones:

- `PENDING -> CONFIRMED`
- `PENDING -> CANCELLED`
- `CONFIRMED -> SHIPPED`
- `CONFIRMED -> CANCELLED`
- `SHIPPED -> DELIVERED`
- `SHIPPED -> CANCELLED` solo si la política lo permite.
- `DELIVERED ->` ninguna transición salvo devolución futura.
- `CANCELLED ->` ninguna transición.

### Criterio de aceptación

- Estado inválido retorna 400.
- Cancelar una orden entregada retorna 400.
- Cancelar una orden cancelada retorna 400.
- Los timestamps corresponden al estado real.

## 5.4 Corregir pagos

### Archivos relacionados

- `OrderController.java`
- `OrderService.java`
- `PaymentRepository.java`
- Migración de orden

### Implementación

Definir primero si existe pasarela real. Mientras no haya integración real:

- No marcar pagos como completados por una llamada administrativa libre.
- Registrar pagos con estado `PENDING` o `PROCESSING`.
- Exigir `transactionId` único.
- Impedir importes negativos o cero.
- Impedir pagos superiores al saldo restante si la política no admite pagos parciales.
- Impedir duplicar el mismo `transactionId`.
- Solo cambiar a `COMPLETED` al recibir confirmación verificable.

### Criterio de aceptación

- Dos registros con el mismo `transactionId` no crean dos pagos.
- Un monto que excede el total no se acepta si la política no contempla saldo a favor.
- El estado confirmado depende de una suma real de pagos completados.
- El frontend no puede marcar directamente una orden como pagada.

## 5.5 Proteger lectura y cancelación de pedidos

### Implementación

- `GET /orders/{id}`: dueño o administrador.
- `GET /orders`: solo órdenes del usuario del JWT.
- `GET /orders/admin`: administrador.
- Cancelar: dueño o administrador según política.
- Actualizar estado: ADMIN o VENDEDOR.
- Registrar pago: ADMIN o VENDEDOR, nunca usuario genérico si no existe validación externa.

### Criterio de aceptación

- Usuario A no puede consultar ni cancelar pedido B.
- Vendedor no puede listar cuentas financieras de clientes si no está autorizado.
- ADMIN puede consultar todas las órdenes.

---

# Fase 6. Cuenta corriente y dinero

## 6.1 Cerrar autorización financiera

### Archivos relacionados

- `CuentaCorrienteController.java`
- `AbonoController.java`
- `MovimientoCuentaCorrienteController.java`
- `CuentaCorrienteService.java`
- `AbonoService.java`

### Implementación

Definir claramente estos permisos:

- Crear cuenta: administrador.
- Consultar cuenta propia: contratista dueño.
- Consultar cuentas: administrador/vendedor autorizado.
- Actualizar límite: administrador.
- Aplicar cargo: administrador o vendedor autorizado.
- Registrar abono: dueño, administrador o vendedor según política.
- Aplicar/rechazar/anular abono: administrador.
- Procesar interés: administrador o proceso interno programado.

La identidad `procesadoPor` debe salir del JWT y no de un parámetro libre.

### Criterio de aceptación

- Un usuario autenticado no puede consultar la cuenta de otro por UUID.
- Un vendedor no puede aprobar su propio cargo si no está permitido.
- Todos los cambios financieros dejan movimiento con usuario o proceso responsable.

## 6.2 Agregar bloqueo de cuenta a todas las operaciones

### Archivos relacionados

- `CuentaCorrienteRepository.java`
- `CuentaCorrienteService.java`
- `AbonoService.java`

Ya existe `findByIdForUpdate`, que es una base correcta.

### Implementación

1. Usar el bloqueo en cada operación que lea/modifique saldo:
   - crear;
   - actualizar límite;
   - cargo;
   - abono;
   - interés;
   - anulación.
2. Mantener el movimiento y el cambio de saldo en la misma transacción.
3. Evitar transacciones largas en consultas y servicios de respuesta.
4. Bloquear por UUID de cuenta.

### Criterio de aceptación

- Dos cargos simultáneos no sobrepasan el cupo disponible.
- Dos solicitudes de aplicar el mismo abono dejan un solo movimiento.
- Dos anulaciones no reviven una deuda dos veces.

## 6.3 Hacer único un abono aplicado

### Problema

La aplicación verifica el estado pero dos solicitudes simultáneas pueden cargar el abono antes del cambio.

### Implementación

1. Bloquear el abono y su cuenta.
2. Revalidar el estado dentro de la transacción.
3. Solo el estado `PENDIENTE` puede pasar a `APLICADO`.
4. Solo `APLICADO` puede pasar a `ANULADO`.
5. Solo `PENDIENTE` puede pasar a `RECHAZADO`.
6. Agregar referencia externa única si el abono proviene de una pasarela.

### Criterio de aceptación

- Aplicar dos veces el mismo abono genera un solo impacto en saldo.
- Rechazar un abono aplicado retorna 400.
- Anular dos veces retorna 400.

## 6.4 Corregir movimiento de abono/anulación

### Implementación

Cada movimiento debe guardar:

- Cuenta corriente.
- Tipo de movimiento.
- Monto positivo o negativo según la política oficial.
- Saldo anterior.
- Saldo nuevo.
- Disponible anterior.
- Disponible nuevo.
- Referencia.
- Tipo de referencia.
- Fecha.
- Responsable o proceso.

Cuando un abono se aplica:

- disminuye deuda;
- aumenta disponible;
- movimiento con efecto de abono.

Cuando se anula:

- aumenta deuda;
- disminuye disponible;
- si el disponible queda negativo, pasa a mora;
- movimiento positivo de ajuste/manual.

### Criterio de aceptación

- La suma de saldos puede reconstruirse desde movimientos.
- Aplicar/anular deja movimientos coherentes.
- Un abono mayor que la deuda no crea saldo a favor.

## 6.5 Definir interés mensual

### Implementación

- El proceso debe ejecutarse una vez por cuenta y período.
- Debe existir registro de período procesado para evitar que dos procesos cobren el mismo interés.
- Debe existir bloqueo sobre la cuenta.
- Si el interés excede el límite, cambiar a mora.
- Definir si se cobra sobre saldo diario, mensual o utilizado al corte.

### Criterio de aceptación

- Ejecutar el proceso dos veces en el mismo mes no duplica el interés.
- Ejecutar para cuentas sin deuda no crea movimiento.
- Los intereses generan movimientos auditables.

## 6.6 Eliminar o cerrar cuentas con seguridad

No permitir borrado físico si existen:

- saldo utilizado;
- abonos;
- movimientos;
- referencias a pedidos o facturas.

Preferir estado `CERRADA` o baja lógica.

### Criterio de aceptación

- Eliminar cuenta utilizada retorna conflicto o rechazo.
- Las cuentas cerradas no reciben cargos ni abonos.
- El historial financiero sigue disponible.

---

# Fase 7. RabbitMQ y consistencia entre servicios

## 7.1 Definir los eventos reales

Eventos existentes o a revisar:

- `usuario.creado`
- `usuario.actualizado`
- `usuario.eliminado`
- `producto.creado`
- `producto.actualizado`
- `producto.stock_cambiado`
- `producto.eliminado`
- `orden.creada`
- `orden.estado_cambiado`
- Eventos de auth.

### Reglas

- Publicar eventos después del commit.
- No incluir datos sensibles en mensajes.
- Incluir ID, versión o timestamp cuando sea útil.
- Consumidores deben ser idempotentes.
- Los reintentos no deben repetir efectos financieros o de stock.

## 7.2 Evitar pérdida de eventos

### Implementación mínima

Para operaciones críticas, usar un outbox:

1. Guardar la entidad y el evento pendiente en la misma transacción.
2. Un proceso envía los eventos pendientes a RabbitMQ.
3. Marcar enviados solo después de confirmación.
4. Reintentar eventos pendientes.
5. Evitar que cada instancia del servicio haga el mismo envío simultáneo sin bloqueo o lease.

Como alternativa más pequeña para este proyecto: confirmar que solo se usen eventos informativos no críticos y registrar la falla. Pero para pedidos y stock no debe haber pérdida silenciosa.

### Criterio de aceptación

- Si RabbitMQ cae al hacer commit, el evento se puede reenviar después.
- Reiniciar el servicio no duplica efectos en otros servicios.
- No hay reenvíos infinitos sin observabilidad.

## 7.3 Confirmar la configuración de bindings

Revisar nombres de exchange, routing key y binding entre productores y consumidores.

### Criterio de aceptación

- Un evento publicado llega al destino configurado.
- RabbitMQ no genera colas huérfanas innecesarias.
- Los mensajes rechazados no hacen busy loop ni se pierden sin registro.

---

# Fase 8. Integración HTTP entre microservicios

## 8.1 Completar comunicación order-service → product-service

Agregar un cliente HTTP real o Feign:

- Consultar producto por código.
- Validar activo.
- Validar stock.
- Obtener precio.
- Obtener nombre.

Usar `@Transactional` solo alrededor de la creación completa.

### Manejo de fallos

- Si product-service no responde: no crear el pedido.
- Implementar timeout corto y mensaje estable.
- No asumir que el stock sigue disponible después de una respuesta.
- Reintentar solo consultas; no reintentar una operación mutante sin idempotencia.

## 8.2 order-service → catalog-service

- Validar región.
- Validar comuna.
- Validar que la comuna pertenezca a la región.
- Obtener costo o reglas de envío.

No confiar en IDs enviados por el frontend sin comprobarlos.

## 8.3 cart-service → product-service

Ya existe `ProductClient`. Debe considerarse:

- Timeout.
- Manejo de producto inexistente.
- Manejo de servicio caído.
- Mensajes consistentes al frontend.

## 8.4 Autenticación entre servicios

Definir si los servicios internos usan:

- JWT compartido;
- credencial de servicio;
- red interna aislada.

No dejar endpoints internos solo protegidos por que estén en un puerto interno.

---

# Fase 9. API y contratos HTTP para frontend

## 9.1 Rutas oficiales recomendadas

Mantener las rutas realmente expuestas por el gateway y los context paths:

```text
/auth
/products
/categories
/orders
/users o /usuarios
/cuentas-corrientes
/addresses o direcciones según finalizar
/regions
/communes
/coverage
/cart
```

Antes de entregar al frontend:

- elegir una única convención lingüística por prefijo;
- no mantener alias innecesarios si no existe compatibilidad pendiente;
- documentar todas las rutas con method, role y body.

## 9.2 Errores HTTP consistentes

Usar como mínimo:

- `400`: datos inválidos o transición inválida.
- `401`: falta o invalidez del JWT.
- `403`: JWT válido sin permiso.
- `404`: recurso inexistente.
- `409`: conflicto de estado, clave duplicada o concurrencia.
- `422`: validación semántica si se quiere distinguir, pero manténlo estable.
- `500`: error interno sin datos sensibles.

Formato recomendado:

```json
{
  "timestamp": "2026-11-22T12:00:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Stock insuficiente",
  "path": "/api/cart/items",
  "fieldErrors": {}
}
```

El frontend debe consumir `message` o `errorCode`, no texto de excepción Java.

## 9.3 Paginación consistente

Cada listado paginado debe devolver:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

Limitar `size` entre 1 y 100 en todos los servicios.

Validar `sortBy` contra campos permitidos. No permitir nombres arbitrarios que puedan causar `PropertyReferenceException`.

## 9.4 CORS

La configuración actual permite solo localhost:5173, localhost:3000 y 127.0.0.1.

Antes de integrar frontend desplegado:

- agregar el dominio real;
- no usar `*` si hay credenciales;
- mantener allow-credentials solo si es necesario;
- comprobar métodos y headers.

## 9.5 Swagger

Mantener Swagger para desarrollo y deshabilitarlo o protegerlo en producción.

Antes de entregar al frontend:

- exportar el OpenAPI de cada servicio;
- comprobar contextos `/api`;
- verificar que los ejemplos coincidan con el gateway;
- no publicar endpoints internos o usuarios de prueba.

---

# Fase 10. Validación y pruebas

## 10.1 Pruebas por microservicio

### auth-service

- Login correcto e incorrecto.
- Registro duplicado.
- Rotación y reutilización de refresh.
- Logout.
- Cambio de contraseña.
- Roles en JWT.

### user-service

- Crear perfil interno una vez.
- Propietario y administrador.
- CRUD de direcciones.
- Crear cuenta corriente.
- Aplicar cargo.
- Aplicar, rechazar y anular abono.
- Interés y bloqueo de cuenta.

### product-service

- Código único.
- Cambios de stock.
- Stock negativo.
- Imagen primaria única.
- Concurrencia.
- Producto inactivo.

### catalog-service

- Región/comuna/cobertura.
- Duplicados.
- Permisos administrativos.

### cart-service

- Propietario del carrito.
- Agregar, actualizar y eliminar.
- Producto inexistente/inactivo.
- Stock insuficiente.

### order-service

- Creación real con precio real.
- Stock suficiente.
- Idempotencia concurrente.
- Estado válido.
- Pago válido.
- Cancelación y devolución de stock.
- Propietario.

### api-gateway

- Rutas públicas.
- Rutas privadas.
- Claims y cabeceras internas.
- CORS.
- Ruta interna bloqueada.

## 10.2 Pruebas de integración

Ejecutar al menos estos flujos completos:

1. Registrar usuario.
2. Crear perfil interno.
3. Login.
4. Crear categoría.
5. Crear producto.
6. Crear carrito.
7. Crear pedido con idempotencia.
8. Confirmar pedido.
9. Registrar pago.
10. Enviar pedido.
11. Entregar pedido.
12. Cancelar otro pedido y verificar devolución de stock.
13. Crear cuenta corriente.
14. Aplicar cargo.
15. Registrar y aplicar abono.
16. Anular abono y verificar movimiento.

## 10.3 Pruebas de concurrencia

Casos obligatorios:

- Dos usuarios intentan comprar la última unidad.
- Dos solicitudes crean el mismo pedido con la misma clave.
- Dos solicitudes aplican el mismo abono.
- Dos solicitudes actualizan el mismo límite.
- Dos servicios procesan interés mensual al mismo tiempo.
- Dos procesos reciben el mismo evento RabbitMQ.

---

# Fase 11. Arranque y despliegue local

## 11.1 Orden de arranque

Docker Compose ya intenta ordenar servicios, pero debe probarse el arranque completo:

1. PostgreSQL.
2. RabbitMQ.
3. auth-service.
4. Servicios base: product, catalog y user.
5. cart y order.
6. api-gateway.

## 11.2 Comandos de revisión

Desde la raíz:

```bash
./mvnw clean verify
```

Para levantar todo:

```bash
docker compose up --build
```

Para revisar health:

```bash
curl http://localhost:8080/actuator/health
```

Nota: el actuator actual puede variar según ruta o implementación.

## 11.3 Checklist previo al frontend

- [ ] No hay secretos conocidos.
- [ ] Login y refresh funcionan desde el gateway.
- [ ] Roles oficiales están definidos.
- [ ] Rutas internas no son públicas.
- [ ] Usuario A no puede ver usuario B.
- [ ] Productos reales aparecen con stock real.
- [ ] Carrito usa identidad del JWT.
- [ ] Pedido usa precio real.
- [ ] Stock no se pasa a negativo.
- [ ] Idempotencia evita duplicados.
- [ ] Estados de pedido están validados.
- [ ] Pagos tienen referencia única.
- [ ] Cuenta corriente usa bloqueo.
- [ ] Aplicar y anular abonos no duplica saldo.
- [ ] RabbitMQ reintenta eventos sin duplicar efectos.
- [ ] Paginación tiene tamaño máximo.
- [ ] Errores son consistentes.
- [ ] CORS incluye el frontend real.
- [ ] Swagger coincide con las rutas reales.
- [ ] Todos los cambios críticos tienen prueba automatizada.

---

# Resumen de prioridades

## Bloqueadores antes de conectar frontend

1. Seguridad y secretos.
2. Identidad, roles y rutas internas.
3. Autorización por propietario.
4. Pedido real con stock real.
5. Idempotencia de pedidos.
6. Estados y pagos.
7. Cuenta corriente y abonos.

## Importantes antes de pruebas completas

1. RabbitMQ confiable.
2. Comunicación real entre microservicios.
3. Contratos HTTP consistentes.
4. Paginación y errores.
5. CORS y Swagger.

## Mejoras menores

1. Outbox avanzado si la carga crece.
2. Consulta de carrito por lote.
3. Métricas y tracing.
4. Eliminación de alias y código duplicado.
5. Limpieza de dependencias no usadas.

## Riesgos conocidos si se omiten fases

- Robo de sesiones por secretos conocidos.
- Acceso financiero a cuentas ajenas.
- Venta sin stock.
- Cobro con precios ficticios.
- Duplicación de pedidos y abonos.
- Pérdida de eventos RabbitMQ.
- Frontend integrado contra contratos inestables.

## Resultado esperado

Al terminar estas fases, el frontend podrá:

- iniciar sesión sin consultar servicios internos;
- navegar productos y catálogo sin permisos especiales;
- usar un carrito asociado al JWT;
- crear pedidos una sola vez;
- consultar solo sus propios pedidos;
- pagar o cancelar según estados válidos;
- consultar cuentas corrientes solo si están autorizadas;
- recibir errores consistentes;
- operar sin dependencia de datos ficticios o reglas mock.
