# Plataforma de Logística y Rutas de Entrega

TP Integrador de Técnicas Avanzadas de Programación (Universidad de Palermo).
Alumno: Santino José Lopez Mañán.

API REST para gestionar las entregas de una empresa con varios depósitos: órdenes con control de stock,
planificación de rutas, ejecución del recorrido por parte del chofer y seguimiento en tiempo real por
WebSocket. El frontend está completamente desacoplado: cualquier cliente (Postman, una web, una app)
consume la misma API.

## Tecnologías

| Componente | Tecnología |
| --- | --- |
| Lenguaje y framework | Java 21, Spring Boot 3.3 |
| Persistencia | PostgreSQL 16, Spring Data JPA (Hibernate), Flyway para esquema y datos semilla |
| Seguridad | Spring Security, JWT (jjwt), contraseñas con BCrypt |
| Tiempo real | WebSocket con STOMP (Spring WebSocket) |
| Documentación | springdoc-openapi (Swagger UI) |
| Tests | JUnit 5, Mockito, AssertJ, JaCoCo |
| Despliegue | Docker y Docker Compose |

## Cómo levantarlo

Requisito: Docker Desktop instalado y abierto.

1. Crear el archivo de entorno a partir del ejemplo:

   ```bash
   cp .env.example .env
   ```

2. Completar en `.env` una contraseña para la base y la clave del JWT. La clave se genera con:

   ```bash
   openssl rand -base64 32
   ```

   El `.env` no se sube al repositorio; los valores usados para la corrección se envían por separado.

3. Levantar todo:

   ```bash
   docker compose up --build
   ```

   La primera vez tarda unos minutos porque descarga las imágenes y compila el proyecto. Al arrancar,
   Flyway crea las tablas y carga los datos de prueba.

4. Verificar que esté arriba:

   | Qué | URL |
   | --- | --- |
   | Estado de la API | http://localhost:8080/actuator/health |
   | Documentación Swagger | http://localhost:8080/swagger-ui.html |
   | Página de demo del tracking | http://localhost:8080/demo.html |

Para apagarlo: `docker compose down`. Para borrar también los datos y volver a la carga inicial:
`docker compose down -v`.

## Imágenes Docker y cómo se conectan

El sistema corre en dos contenedores definidos en `docker-compose.yml`, conectados por la red interna
que crea Compose:

| Contenedor | Imagen | Por qué |
| --- | --- | --- |
| `db` | `postgres:16-alpine` | Imagen oficial de PostgreSQL, en la variante Alpine que es más liviana. La versión está fijada para que el entorno sea siempre el mismo. Los datos quedan en el volumen `datos-postgres`. |
| `api` | Se construye con el `Dockerfile` del proyecto | Build en dos etapas: `maven:3.9-eclipse-temurin-21` compila el `.jar` y `eclipse-temurin:21-jre` lo ejecuta. La imagen final solo tiene el JRE y el `.jar` (sin Maven ni código fuente) y corre con un usuario sin privilegios. |

- La base **no publica puertos** hacia afuera: solo la API puede conectarse, usando el nombre de servicio `db` como host.
- La API espera a que la base esté lista gracias al `healthcheck` de PostgreSQL y a `depends_on: condition: service_healthy`.
- La configuración (credenciales, clave JWT) entra por variables de entorno desde el `.env`, no está escrita en el código.

## Usuarios de prueba

| Rol | Email | Contraseña |
| --- | --- | --- |
| Administrador | admin@logistica.com | admin123 |
| Operador de depósito | operador@logistica.com | operador123 |
| Chofer (vehículo AB123CD) | chofer1@logistica.com | chofer123 |
| Chofer (vehículo AC456EF) | chofer2@logistica.com | chofer123 |
| Chofer (vehículo AD789GH) | chofer3@logistica.com | chofer123 |

También se precargan 2 depósitos, 6 productos con stock, 8 destinatarios con coordenadas reales de CABA,
4 vehículos (uno en el taller) y 4 órdenes listas para planificar en el depósito Palermo.

## Circuito de la demo

La colección `postman/TP-Logistica.postman_collection.json` tiene el recorrido completo, con las pruebas
de camino feliz y de errores. Se importa en Postman y se ejecuta carpeta por carpeta (o con el Runner)
sobre una base recién levantada:

1. **Login** de operador, chofer y admin. Login con contraseña incorrecta (401) y endpoint privado sin token (401).
2. **Consultas** paginadas: depósitos, stock, productos, destinatarios, vehículos.
3. **Órdenes**: alta con descuento de stock, alta sin stock (409), datos inválidos (400), cancelación con reintegro de stock.
4. **Planificación** de las órdenes pendientes entre dos vehículos. Intento con un vehículo en el taller (409).
5. **Recorrido del chofer**: ver su ruta, iniciarla, informar posición, seguimiento público por código,
   intento de cancelar una orden en tránsito (409), resultado de cada parada (con una entrega fallida) y finalización.
6. **Administración**: el operador no puede cambiar el estado de un vehículo (403); el admin sí.

Para ver el WebSocket en vivo, abrir http://localhost:8080/demo.html después de iniciar la ruta:
en un panel se sigue un pedido con su código y en otro se simula el recorrido del chofer; el vehículo se
mueve en el mapa con las posiciones que llegan por WebSocket.

## Endpoints

Todos los listados son paginados (`page`, `size`, `sort`; tamaño por defecto 10, máximo 50).

| Método y ruta | Rol | Descripción |
| --- | --- | --- |
| `POST /api/auth/login` | Público | Devuelve el JWT |
| `GET /api/depositos` | Operador, Admin | Depósitos |
| `GET /api/depositos/{id}/stock` | Operador, Admin | Stock del depósito |
| `GET /api/productos` | Operador, Admin | Catálogo de productos |
| `GET` / `POST /api/destinatarios` | Operador, Admin | Listado y alta de destinatarios |
| `GET /api/ordenes` | Operador, Admin | Listado con filtros `estado` y `depositoId` |
| `GET /api/ordenes/{id}` | Operador, Admin | Detalle con ítems e historial de estados |
| `POST /api/ordenes` | Operador, Admin | Alta: valida y descuenta stock |
| `POST /api/ordenes/{id}/cancelar` | Operador, Admin | Solo en estado `CREADA`; reintegra stock |
| `POST /api/planificaciones` | Operador, Admin | Arma las rutas de los vehículos indicados |
| `GET /api/planificaciones/{id}` | Operador, Admin | Detalle de una planificación |
| `GET /api/rutas` | Operador, Admin | Listado con filtro `estado` |
| `GET /api/rutas/actual` | Chofer | Ruta pendiente o en curso de su vehículo |
| `POST /api/rutas/{id}/iniciar` | Chofer, Operador, Admin | Sale del depósito: órdenes en tránsito |
| `POST /api/paradas/{id}/resultado` | Chofer, Admin | Entregada o no entregada (con motivo) |
| `POST /api/rutas/{id}/finalizar` | Chofer, Operador, Admin | Cierra la ruta y procesa las devoluciones |
| `POST /api/rutas/{id}/anular` | Operador, Admin | Solo antes de salir; libera las órdenes |
| `PATCH /api/vehiculos/{id}/estado` | Admin | Disponible, en taller o baja |
| `POST /api/vehiculos/{id}/posicion` | Chofer | Posición actual (se reenvía por WebSocket) |
| `GET /api/seguimiento/{codigo}` | Público | Estado del pedido sin datos personales |

**WebSocket** (STOMP en `ws://localhost:8080/ws`):

| Destino | Quién | Contenido |
| --- | --- | --- |
| `/topic/seguimiento/{codigo}` | Público | Cambios de estado y posición del vehículo que lleva el pedido |
| `/topic/vehiculos` | Operador o Admin (JWT en el header `Authorization` del `CONNECT`) | Posición de toda la flota |

Errores: todas las respuestas de error tienen el mismo formato (`status`, `error`, `mensaje`, `ruta`).
400 datos inválidos, 401 sin token o credenciales incorrectas, 403 rol sin permiso, 404 recurso
inexistente, 409 regla de negocio violada (transición de estado inválida, stock insuficiente, etc.).

## Estructura del código

```
src/main/java/ar/edu/up/tap/logistica
├── dominio        Entidades JPA con sus reglas (Orden, Ruta, Vehiculo, StockDeposito...) y enums de estado
├── repositorio    Interfaces de Spring Data JPA
├── servicio       Casos de uso y transacciones
├── optimizacion   Interfaz OptimizadorRutas y la heurística que la implementa
├── tracking       Posición en memoria y notificaciones por WebSocket
├── seguridad      JWT, filtro de autenticación, reglas por rol, seguridad del WebSocket
├── config         WebSocket y Swagger
└── web            Controladores REST, DTOs y manejador global de excepciones
```

Arquitectura en capas: los controladores solo traducen HTTP, la lógica está en los servicios y en las
entidades, y el acceso a datos pasa siempre por los repositorios.

## Decisiones de diseño

- **Inyección de dependencias por constructor.** Todos los servicios reciben sus dependencias en el
  constructor y dependen de interfaces donde hay una tecnología de por medio: `OptimizadorRutas`,
  `RegistroPosiciones` y `NotificadorTiempoReal`. En los tests esas dependencias se reemplazan por mocks.
- **Máquina de estados de la orden.** Las transiciones permitidas están en el enum `EstadoOrden`. Cada
  cambio pasa por `Orden`, que lo valida y registra un `EventoOrden` (historial). Toda orden termina en
  `ENTREGADA`, `DEVUELTA` o `CANCELADA`.
- **Optimización de rutas.** `OptimizadorVecinoMasCercano` reparte las paradas entre los vehículos por
  barrido angular desde el depósito y ordena cada recorrido con la heurística del vecino más cercano.
- **Posición en vivo sin persistir.** Se guarda solo la última posición de cada vehículo en memoria; en la
  base quedan los hechos de negocio (resultado de cada parada y cambios de estado).
- **Stock consistente.** Crear una orden valida y descuenta el stock en una sola transacción; la columna
  `version` aplica bloqueo optimista ante dos descuentos simultáneos.

### Cambios respecto de la primera entrega

- La planificación se resuelve **dentro de la API**, de forma sincrónica, a través de la interfaz
  `OptimizadorRutas`. El microservicio y la cola de mensajes quedan como plan de mejora (abajo).
- El chofer informa la posición **por REST** (`POST /api/vehiculos/{id}/posicion`), protegido con el mismo
  JWT que el resto de la API. El WebSocket se usa para lo que aporta: empujar las actualizaciones a los
  que están mirando, sin que tengan que consultar.
- Los destinatarios se cargan con sus coordenadas; la geocodificación automática de direcciones queda como mejora.
- No hay frontend: la API se prueba con Postman y Swagger, más una página simple para ver el WebSocket.

## Plan de mejora

1. **Microservicio de optimización.** Llevar el cálculo de rutas a un servicio en Python con OR-Tools,
   comunicado con la API por RabbitMQ: la API publica la solicitud, responde `202 Accepted`, y al recibir el
   resultado crea las rutas y avisa al operador por WebSocket. Así, en picos de pedidos el cálculo no
   ocupa hilos ni conexiones de la API. El cambio queda acotado a una nueva implementación de
   `OptimizadorRutas` que publique en la cola.
2. **Registro de posiciones en Redis**, para poder correr varias instancias de la API detrás de un balanceador.
3. **Geocodificación** de direcciones (Nominatim/OpenStreetMap) al dar de alta un destinatario.
4. **Distancias reales por calle** (OSRM) en lugar de línea recta.
5. **Frontend** para el operador y la vista del chofer en el celular.

## Tests

Tests unitarios del dominio (máquina de estados, stock, vehículos), de los servicios con Mockito y del
optimizador de rutas. Con Maven instalado:

```bash
mvn test
```

O sin instalar nada más que Docker:

```bash
docker run --rm -v "$PWD":/app -w /app maven:3.9-eclipse-temurin-21 mvn test
```

El reporte de cobertura queda en `target/site/jacoco/index.html`.
