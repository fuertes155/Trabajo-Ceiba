# 🚲 API REST — Sistema de Alquiler de Bicicletas Urbanas

API REST desarrollada con **Spring Boot 3.5** para gestionar el alquiler de bicicletas en una empresa de turismo urbano. Permite registrar bicicletas, iniciar/finalizar alquileres, consultar disponibilidad e historial, y calcular costos con multas por devolución tardía.

---

## 📋 Tabla de Contenidos

- [Arquitectura](#-arquitectura)
- [Tecnologías](#-tecnologías-utilizadas)
- [Ejecutar el Proyecto](#-ejecutar-el-proyecto)
- [Endpoints de la API](#-endpoints-de-la-api)
- [Reglas de Negocio](#-reglas-de-negocio)
- [Datos Iniciales](#-datos-iniciales)
- [Ejemplos de Peticiones](#-ejemplos-de-peticiones-curl)
- [Tests Automatizados](#-tests-automatizados)
- [Supuestos Tomados](#-supuestos-tomados)

---

## 🏗️ Arquitectura

Se implementó una **arquitectura por capas** (Layered Architecture) con separación clara de responsabilidades:

```
com.ceiba.alquiler
├── config/              → Configuración (datos iniciales, OpenAPI)
├── controller/          → Capa de presentación (REST Controllers)
├── dto/                 → Objetos de transferencia (Request/Response)
├── exception/           → Excepciones personalizadas + GlobalExceptionHandler
├── model/               → Entidades JPA (dominio)
│   └── enums/           → Enumeraciones (TipoBicicleta, EstadoBicicleta)
├── repository/          → Capa de persistencia (Spring Data JPA)
└── service/             → Capa de lógica de negocio
```

### Justificación

- **Separación de responsabilidades**: Cada capa tiene una responsabilidad única (SRP), con dependencias que fluyen de arriba hacia abajo.
- **DTOs**: Se usan DTOs de request y response para desacoplar la representación externa de las entidades internas, evitando exponer detalles de implementación.
- **Excepciones centralizadas**: Un `@RestControllerAdvice` captura todas las excepciones y retorna respuestas JSON consistentes con códigos HTTP apropiados.
- **Principios SOLID**: Inyección de dependencias via constructor, responsabilidad única por clase, y lógica de negocio concentrada en la capa de servicio.

---

## 🛠️ Tecnologías Utilizadas

| Componente | Tecnología | Versión |
|---|---|---|
| Framework | Spring Boot | 3.5.0 |
| Lenguaje | Java | 17 |
| Build Tool | Maven (wrapper incluido) | 3.9.11 |
| Base de datos | H2 (en memoria) | Runtime |
| ORM | Spring Data JPA + Hibernate | - |
| Validación | Jakarta Bean Validation | - |
| Documentación API | SpringDoc OpenAPI (Swagger UI) | 2.8.6 |
| Testing | JUnit 5 + Mockito + MockMvc | - |

---

## 🚀 Ejecutar el Proyecto

### Prerrequisitos

- **Java 17** o superior instalado
- No se requiere instalación de Maven (se incluye el Maven Wrapper)

### Pasos

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/tu-usuario/alquiler-bicicletas.git
   cd alquiler-bicicletas
   ```

2. **Ejecutar la aplicación**:
   ```bash
   # Linux/Mac
   ./mvnw spring-boot:run

   # Windows
   .\mvnw.cmd spring-boot:run
   ```

3. **Acceder a la API**:
   - API Base: `http://localhost:8080/api`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Consola H2: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:alquilerdb`, usuario: `sa`, sin contraseña)

4. **Ejecutar los tests**:
   ```bash
   # Linux/Mac
   ./mvnw test

   # Windows
   .\mvnw.cmd test
   ```

---

## 📡 Endpoints de la API

### Bicicletas

| Método | Endpoint | Descripción | RF |
|---|---|---|---|
| `POST` | `/api/bicicletas` | Registrar una nueva bicicleta | RF-01 |
| `GET` | `/api/bicicletas/disponibles` | Listar bicicletas disponibles | RF-04 |
| `GET` | `/api/bicicletas/disponibles?tipo=MONTANA` | Filtrar disponibles por tipo | RF-04 |
| `GET` | `/api/bicicletas/{codigo}/historial` | Historial de alquileres | RF-05 |

### Alquileres

| Método | Endpoint | Descripción | RF |
|---|---|---|---|
| `POST` | `/api/alquileres` | Iniciar un alquiler | RF-02 |
| `PUT` | `/api/alquileres/{id}/finalizar` | Finalizar un alquiler | RF-03 |

---

## 📏 Reglas de Negocio

### RN-01: Tarifas por tipo de bicicleta

| Tipo | Tarifa/hora |
|---|---|
| URBANA | $3.500 |
| MONTAÑA | $5.000 |
| ELÉCTRICA | $7.500 |

### RN-02: Cálculo del costo base

El costo base se calcula sobre el tiempo real de uso, redondeado al alza a la hora completa más cercana.

### RN-03: Multa por devolución tardía

Si se devuelve después de la duración estimada, se cobra 50% de la tarifa/hora por cada hora de retraso (redondeada al alza).

**Ejemplo**: Bicicleta MONTAÑA, estimada 2h, devuelta a 3h 20min:
- Costo base: 4h × $5.000 = $20.000
- Multa: 2h retraso × $2.500 = $5.000
- **Total: $25.000**

### RN-04 y RN-05: Validaciones

- Intentar alquilar una bicicleta no disponible → `409 Conflict`
- Finalizar un alquiler inexistente → `404 Not Found`
- Finalizar un alquiler ya finalizado → `409 Conflict`

---

## 📦 Datos Iniciales

La aplicación carga automáticamente las siguientes bicicletas al iniciar:

| Código | Tipo | Estado |
|---|---|---|
| BIC-001 | URBANA | DISPONIBLE |
| BIC-002 | MONTANA | DISPONIBLE |
| BIC-003 | ELECTRICA | DISPONIBLE |
| BIC-004 | MONTANA | EN_MANTENIMIENTO |
| BIC-005 | URBANA | DISPONIBLE |

---

## 💻 Ejemplos de Peticiones (curl)

### 1. Registrar una bicicleta (RF-01)

```bash
curl -X POST http://localhost:8080/api/bicicletas \
  -H "Content-Type: application/json" \
  -d '{"codigo":"BIC-006","tipo":"ELECTRICA","estado":"DISPONIBLE"}'
```

### 2. Consultar bicicletas disponibles (RF-04)

```bash
# Todas las disponibles
curl http://localhost:8080/api/bicicletas/disponibles

# Filtrar por tipo
curl "http://localhost:8080/api/bicicletas/disponibles?tipo=MONTANA"
```

### 3. Iniciar un alquiler (RF-02)

```bash
curl -X POST http://localhost:8080/api/alquileres \
  -H "Content-Type: application/json" \
  -d '{"codigoBicicleta":"BIC-001","nombreCliente":"Juan Pérez","duracionEstimadaHoras":2}'
```

**Respuesta:**
```json
{
  "id": 1,
  "codigoBicicleta": "BIC-001",
  "tipoBicicleta": "URBANA",
  "nombreCliente": "Juan Pérez",
  "horaInicio": "2026-05-15T20:00:00",
  "duracionEstimadaHoras": 2,
  "horaFin": null,
  "duracionRealMinutos": null,
  "costoBase": null,
  "multa": null,
  "costoTotal": null,
  "activo": true,
  "tuvoMulta": false
}
```

### 4. Finalizar un alquiler (RF-03)

```bash
curl -X PUT http://localhost:8080/api/alquileres/1/finalizar
```

**Respuesta:**
```json
{
  "id": 1,
  "codigoBicicleta": "BIC-001",
  "tipoBicicleta": "URBANA",
  "nombreCliente": "Juan Pérez",
  "horaInicio": "2026-05-15T20:00:00",
  "duracionEstimadaHoras": 2,
  "horaFin": "2026-05-15T22:30:00",
  "duracionRealMinutos": 150,
  "costoBase": 10500.00,
  "multa": 1750.00,
  "costoTotal": 12250.00,
  "activo": false,
  "tuvoMulta": true
}
```

### 5. Consultar historial de una bicicleta (RF-05)

```bash
curl http://localhost:8080/api/bicicletas/BIC-001/historial
```

### 6. Error: alquilar bicicleta en mantenimiento (RN-04)

```bash
curl -X POST http://localhost:8080/api/alquileres \
  -H "Content-Type: application/json" \
  -d '{"codigoBicicleta":"BIC-004","nombreCliente":"Ana García","duracionEstimadaHoras":1}'
```

**Respuesta (409 Conflict):**
```json
{
  "timestamp": "2026-05-15 20:00:00",
  "status": 409,
  "error": "Conflict",
  "mensaje": "La bicicleta BIC-004 no está disponible para alquiler. Estado actual: EN_MANTENIMIENTO"
}
```

---

## 🧪 Tests Automatizados

El proyecto incluye **42 pruebas automatizadas** divididas en:

### Tests Unitarios (Mockito)

- **AlquilerServiceTest** (23 tests):
  - Cálculo de horas redondeadas al alza (6 casos)
  - Cálculo de multas por devolución tardía (6 casos)
  - Cálculo de costo total completo (3 casos, incluye ejemplo del enunciado)
  - Iniciar alquiler + validaciones RN-04 (3 casos)
  - Finalizar alquiler + validaciones RN-05 (3 casos)
  - Consulta de historial (2 casos)

- **BicicletaServiceTest** (5 tests):
  - Registro de bicicleta y código duplicado
  - Consulta de disponibilidad con/sin filtro por tipo

### Tests de Integración (MockMvc + H2)

- **BicicletaControllerIntegrationTest** (6 tests)
- **AlquilerControllerIntegrationTest** (7 tests, incluye flujo completo end-to-end)

### Ejecutar tests
```bash
.\mvnw.cmd test
```

---

## 📝 Supuestos Tomados

1. **ELECTRICA sin tilde**: El enum Java usa `ELECTRICA` (sin tilde) y `MONTANA` (sin ñ), ya que los identificadores Java no soportan caracteres especiales. En la documentación y comunicación se usa la forma correcta.

2. **Seguridad básica**: Se implementó validación de entrada (Bean Validation en DTOs), manejo centralizado de errores y respuestas con códigos HTTP apropiados. No se implementó autenticación JWT ya que no es un requerimiento funcional explícito.

3. **Tiempo mínimo de alquiler**: Se estableció un mínimo de 1 minuto para evitar cobros de $0 en caso de finalizaciones inmediatas.

4. **Formato de código de bicicleta**: Se adoptó el patrón `BIC-XXX` (tres dígitos) validado con regex en el DTO de registro.

5. **Base de datos en memoria**: Se usa H2 en memoria para facilitar la ejecución sin instalación adicional. Los datos se pierden al reiniciar la aplicación.

6. **Hora de inicio**: La hora de inicio del alquiler se asigna automáticamente al momento de crearlo (no se permite especificar manualmente).

---

## 📄 Licencia

Proyecto desarrollado como prueba técnica para Ceiba Software.
