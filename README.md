# loan-api

API **RESTful** de **préstamo de equipos TIC** con Quarkus para Desarrollo Orientado a Servicios.
Contexto: el departamento de TIC de la Universidad Santo Tomás presta **equipos** (`equipment`: portátiles, cámaras, sensores)
a **estudiantes** (`student`) para sus proyectos. Cada entrega queda registrada como un **préstamo** (`loan`),
así se controla el stock y se sabe quién tiene qué. Base de datos **MySQL (XAMPP)**.

Incluye: capas **Resource → Service → Repository**, **DTOs** inmutables con **Bean Validation**,
**manejo de errores** con `ExceptionMapper` (400/404 en JSON) y **Lombok** en los modelos.

## Modelo de datos (diagrama)

```
equipment (1) ──< loan >── (1) student
```

| Tabla | Campos |
|-------|--------|
| `equipment` | `id` PK, `code`, `name`, `stock` |
| `student` | `id` PK, `name`, `email`, `program` |
| `loan` | `id` PK, `loan_date`, `return_date`, `quantity`, `equipment_id` FK → `equipment.id`, `student_id` FK → `student.id` |

Un equipo puede estar en muchos préstamos y un estudiante puede tener muchos préstamos (**uno a muchos** en ambos lados).
`return_date` queda en `null` mientras el equipo **no** se ha devuelto: ese es el préstamo *activo*.

## Cómo crear el proyecto (extensiones en el pom)

Al crear el proyecto en [code.quarkus.io](https://code.quarkus.io) agrega estas extensiones. Si ya creaste el proyecto, añade los bloques al `pom.xml`:

| Extensión en code.quarkus.io | Dependencia en el pom | Para qué |
|---|---|---|
| REST | `quarkus-rest` | Endpoints REST (Resource) |
| REST Jackson | `quarkus-rest-jackson` | Serialización JSON |
| Hibernate ORM | `quarkus-hibernate-orm` | Entidades JPA |
| Hibernate ORM with Panache | `quarkus-hibernate-orm-panache` | Repositorios |
| JDBC Driver - MySQL | `quarkus-jdbc-mysql` | Conexión a MySQL |
| Hibernate Validator | `quarkus-hibernate-validator` | Bean Validation (`@NotBlank`, `@Email`, `@Min`...) |

Además, **Lombok** va como dependencia normal (`provided`) en el pom:

```xml
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <version>1.18.42</version>
  <scope>provided</scope>
</dependency>
```

> Si compilas con **JDK 23 o superior**, el `maven-compiler-plugin` del `pom.xml` incluye `-proc:full` y
> `annotationProcessorPaths` para Lombok: desde esa versión Java ya no activa el procesamiento de anotaciones por su cuenta.

## Estructura del proyecto

```
src/main/java/usta/
├── model/                        <- MODELO
│   ├── Equipment.java                Entidad JPA (tabla equipment) + control de stock + Lombok
│   ├── EquipmentRepository.java      Repositorio con Panache
│   ├── Student.java                  Entidad JPA (tabla student) + Lombok
│   ├── StudentRepository.java        Repositorio con Panache
│   ├── Loan.java                     Entidad JPA (tabla loan) + FKs a equipment y student
│   └── LoanRepository.java           Repositorio con Panache (activos, por estudiante, por equipo)
├── dto/                          <- DTO (records con validación)
│   ├── EquipmentDTO.java             @NotBlank @Size @NotNull @Min
│   ├── StudentDTO.java               @NotBlank @Email @Size
│   └── LoanDTO.java                  @NotNull @Min
├── service/                      <- SERVICE (reglas de negocio)
│   ├── EquipmentService.java         CRUD + código único + no borrar equipo prestado
│   ├── StudentService.java           CRUD + correo único + no borrar con equipos pendientes
│   └── LoanService.java              Prestar / devolver + control de stock
├── resource/                     <- RESOURCE (endpoints REST JSON)
│   ├── EquipmentResource.java
│   ├── StudentResource.java
│   └── LoanResource.java
└── exception/                    <- MANEJO DE ERRORES
    ├── ValidationExceptionMapper.java    400 (Bean Validation)
    ├── BadRequestExceptionMapper.java    400 (reglas de negocio)
    └── NotFoundExceptionMapper.java      404 (recurso no existe)
```

| Capa | Responsabilidad |
|------|-----------------|
| **Resource** | Recibe la petición HTTP y responde JSON |
| **Service** | Lógica de negocio (stock, préstamos, devoluciones) y validaciones de reglas |
| **Repository** | Acceso a datos (Panache + MySQL) |
| **DTO** | Datos que entran/salen de la API, con validación |
| **Exception** | Convierte errores en JSON uniforme |

## Endpoints

### Equipos - `http://localhost:8080/api/equipment`

| Método | Ruta | Descripción | Cuerpo (JSON) |
|--------|------|-------------|---------------|
| GET | `/api/equipment` | Listar equipos | - |
| GET | `/api/equipment/{id}` | Obtener uno | - |
| POST | `/api/equipment` | Crear | `{"code":"PORT-001","name":"Portátil Lenovo ThinkPad","stock":5}` |
| PUT | `/api/equipment/{id}` | Actualizar | `{"code":"PORT-001","name":"Portátil Lenovo T14","stock":4}` |
| DELETE | `/api/equipment/{id}` | Eliminar | - |

### Estudiantes - `http://localhost:8080/api/students`

| Método | Ruta | Descripción | Cuerpo (JSON) |
|--------|------|-------------|---------------|
| GET | `/api/students` | Listar estudiantes | - |
| GET | `/api/students/{id}` | Obtener uno | - |
| GET | `/api/students/{id}/loans` | Préstamos del estudiante | - |
| POST | `/api/students` | Crear | `{"name":"Ana Pérez","email":"ana.perez@usantoto.edu.co","program":"Ingeniería de Sistemas"}` |
| PUT | `/api/students/{id}` | Actualizar | `{"name":"Ana P. Gómez","email":"ana.gomez@usantoto.edu.co","program":"Ingeniería de Sistemas"}` |
| DELETE | `/api/students/{id}` | Eliminar | - |

### Préstamos - `http://localhost:8080/api/loans`

| Método | Ruta | Descripción | Cuerpo (JSON) |
|--------|------|-------------|---------------|
| GET | `/api/loans` | Listar préstamos | - |
| GET | `/api/loans/active` | Listar solo los no devueltos | - |
| GET | `/api/loans/{id}` | Obtener uno | - |
| POST | `/api/loans` | Registrar préstamo | `{"studentId":1,"equipmentId":1,"quantity":2}` |
| PUT | `/api/loans/{id}` | Actualizar préstamo activo | `{"studentId":1,"equipmentId":1,"quantity":3}` |
| PUT | `/api/loans/{id}/return` | Registrar la devolución | - |
| DELETE | `/api/loans/{id}` | Eliminar préstamo | - |

`loanDate` es opcional en el cuerpo del préstamo: si no se envía, se usa la **fecha de hoy**
(`{"studentId":1,"equipmentId":1,"quantity":1,"loanDate":"2026-09-18"}`).

## Préstamo y devolución (stock)

| Acción | Ruta | Efecto en el stock | Errores posibles |
|--------|------|--------------------|------------------|
| Prestar | `POST /api/loans` | **Resta** `quantity` al stock del equipo | `400` sin stock suficiente o datos inválidos · `404` si el equipo o el estudiante no existe |
| Actualizar | `PUT /api/loans/{id}` | Devuelve la cantidad anterior y **resta** la nueva | `400` si el préstamo ya fue devuelto o no hay stock · `404` si algo no existe |
| Devolver | `PUT /api/loans/{id}/return` | **Suma** `quantity` al stock y pone `return_date` | `400` si ya estaba devuelto · `404` si el préstamo no existe |
| Eliminar | `DELETE /api/loans/{id}` | Si seguía activo, **devuelve** las unidades al stock | `404` si el préstamo no existe |

Reglas adicionales:

* El `code` del equipo y el `email` del estudiante son **únicos** (`400` si se repiten).
* No se puede eliminar un equipo con **préstamos activos**, ni un estudiante con **equipos sin devolver** (`400`).
* Al eliminar un equipo o un estudiante que ya no tiene préstamos activos, primero se borra su historial de préstamos y luego el registro.

## Validación y manejo de errores

**DTOs** (records) con anotaciones de Jakarta Validation:

| Campo | Validación |
|-------|-----------|
| `code`, `name` (Equipment) | `@NotBlank`, `@Size(max = ...)` |
| `stock` (Equipment) | `@NotNull`, `@Min(0)` |
| `name`, `program` (Student) | `@NotBlank`, `@Size(max = 100)` |
| `email` (Student) | `@NotBlank`, `@Email`, `@Size(max = 120)` |
| `studentId`, `equipmentId` (Loan) | `@NotNull` |
| `quantity` (Loan) | `@NotNull`, `@Min(1)` |

En los Resource se usa `@Valid` para activar la validación antes de entrar al método.

**Respuestas de error (JSON uniforme):**

```json
{"error":"El nombre del equipo es obligatorio"}
{"error":"Stock insuficiente para el equipo: Portátil Lenovo ThinkPad (disponibles: 1)"}
{"error":"Ya existe un equipo con el código: PORT-001"}
{"error":"No se puede eliminar el equipo: tiene préstamos activos"}
{"error":"Estudiante no encontrado"}
{"error":"Equipo no encontrado"}
{"error":"Préstamo no encontrado"}
```

Los cuatro primeros son `400 Bad Request` (datos incompletos o regla de negocio) y los tres últimos `404 Not Found`.

## Cómo registrar equipos, estudiantes y préstamos

En Postman usa **Body → raw → JSON** con estos cuerpos:

**Crear equipo** `POST /api/equipment`
```json
{"code":"PORT-001","name":"Portátil Lenovo ThinkPad","stock":5}
```

**Crear estudiante** `POST /api/students`
```json
{"name":"Ana Pérez","email":"ana.perez@usantoto.edu.co","program":"Ingeniería de Sistemas"}
```

**Registrar préstamo** `POST /api/loans`
```json
{"studentId":1,"equipmentId":1,"quantity":2}
```

Respuesta (`201 Created`):

```json
{
  "id": 1,
  "loanDate": "2026-09-18",
  "returnDate": null,
  "quantity": 2,
  "equipment": {"id": 1, "code": "PORT-001", "name": "Portátil Lenovo ThinkPad", "stock": 3},
  "student": {"id": 1, "name": "Ana Pérez", "email": "ana.perez@usantoto.edu.co", "program": "Ingeniería de Sistemas"}
}
```

**Devolver el equipo** `PUT /api/loans/1/return` (sin cuerpo JSON) -> pone `returnDate` y el stock vuelve a `5`.

> Para el `id`, primero haz `GET /api/equipment` o `GET /api/students` y toma el `id` de la lista.

## Códigos HTTP usados

| Código | Cuándo |
|--------|--------|
| `200 OK` | Consulta, actualización o devolución correcta |
| `201 Created` | Recurso creado (POST) |
| `204 No Content` | Recurso eliminado (DELETE) |
| `400 Bad Request` | Datos incompletos/inválidos o regla de negocio incumplida |
| `404 Not Found` | El recurso solicitado no existe |

## Cómo ejecutarlo

Requisitos: JDK 21 o superior, Maven (o `./mvnw`) y **XAMPP con MySQL corriendo**.

1. Iniciar Apache y MySQL desde el panel de XAMPP.
2. Crear la base de datos (phpMyAdmin o consola):

   ```sql
   CREATE DATABASE loans;
   ```

3. Ejecutar la aplicación:

   ```shell script
   ./mvnw quarkus:dev
   ```

4. Probar con **Postman** o el navegador:
   * `GET http://localhost:8080/api/equipment` -> `[]`
   * `POST http://localhost:8080/api/equipment` con Body JSON -> crea y devuelve `201`.

## Configuración de la base de datos

En `src/main/resources/application.properties`:

```properties
quarkus.datasource.db-kind=mysql
quarkus.datasource.username=root
quarkus.datasource.password=
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/loans
quarkus.hibernate-orm.database.generation=update
quarkus.hibernate-orm.database.version-check.enabled=false
```
