# 🚲 Prueba Técnica — Sistema de Alquiler de Bicicletas Urbanas

Este es mi proyecto para la prueba técnica de Ceiba Software. Desarrollé esta API REST usando **Spring Boot 3.5** para resolver el problema de gestión de alquileres de bicicletas que me plantearon en el enunciado.

🟢 **¡El proyecto está desplegado en vivo!** Puedes probar la API directamente desde tu navegador usando Swagger UI aquí:
👉 **[https://trabajo-ceiba.onrender.com/swagger-ui.html](https://trabajo-ceiba.onrender.com/swagger-ui.html)**

El sistema permite registrar bicicletas, iniciar y finalizar alquileres, y lo más importante: **calcula automáticamente los costos base y las multas** siguiendo estrictamente las reglas de negocio, con un sistema de redondeo de horas.

---

## ¿Qué encontrarás aquí?

- [Arquitectura que elegí](#-arquitectura)
- [Tecnologías que usé](#-tecnologías)
- [Cómo probar el proyecto](#-cómo-ejecutarlo)
- [Endpoints](#-endpoints-disponibles)
- [Pruebas Automatizadas](#-pruebas-automatizadas)
- [Decisiones y Supuestos](#-decisiones-y-supuestos)

---

## Arquitectura

Para este proyecto, decidí implementar una **Arquitectura por Capas** (Layered Architecture) tradicional. Me pareció la mejor opción porque mantiene el código ordenado y es fácil de entender. Lo dividí así:

- **Controllers:** Son los puntos de entrada (los endpoints). Su única responsabilidad es recibir las peticiones JSON y devolver las respuestas correctas.
- **Services:** ¡Aquí está el corazón del proyecto! Toda la lógica de negocio (cálculos de horas, tarifas, reglas de multas) vive aquí para no mezclarla con la base de datos o la web.
- **Repositories:** Usé Spring Data JPA para comunicarme con la base de datos sin escribir SQL a mano.
- **Modelos y DTOs:** Separé las entidades de la base de datos (Models) de los objetos que viajan por internet (DTOs) por seguridad y limpieza.

También agregué un `GlobalExceptionHandler` para atrapar cualquier error y devolver siempre un JSON amigable con los códigos HTTP correctos (404, 400, 409).

---

## Tecnologías

- **Java 17** y **Spring Boot 3.5.0**
- **H2 Database:** Elegí usar una base de datos en memoria para que no tengan que instalar nada (MySQL o Postgres) a la hora de evaluar la prueba. ¡Solo darle a Run y listo!
- **Spring Data JPA & Hibernate** para la persistencia.
- **Swagger UI (SpringDoc)** para que la API esté documentada automáticamente.
- **JUnit 5 y Mockito** para mis pruebas unitarias.

---

## Cómo ejecutarlo

Es muy sencillo. Como incluí el Maven Wrapper (`mvnw`), ni siquiera necesitan tener Maven instalado, solo Java 17.

1. **Clonar y abrir la terminal en la carpeta del proyecto**
2. **Para correr el servidor:**
   ```bash
   # En Windows:
   .\mvnw.cmd spring-boot:run

   # En Mac/Linux:
   ./mvnw spring-boot:run
   ```
3. **Para probar la API (Swagger):**
   Abran su navegador en `http://localhost:8080/swagger-ui.html`

### Datos Iniciales
Para facilitar las pruebas, hice que el sistema cargue automáticamente las 5 bicicletas de ejemplo que pedía el enunciado apenas arranca. Así que ya hay datos con qué jugar en Swagger.

---

## Pruebas Automatizadas

Me tomé muy en serio el tema de probar la lógica de negocio. En total escribí **42 pruebas automatizadas** (unitarias y de integración).

El archivo al que más cariño le puse fue `AlquilerServiceTest.java`. Ahí probé cada regla de cálculo de horas y de multas. De hecho, uno de los tests se llama `ejemploEnunciado_costoTotalCorrecto`, el cual verifica que la bicicleta de Montaña que se pasa por 1h y 20min cobra exactamente **$25.000**, tal como pedía el PDF de la prueba.

Para correr las pruebas ustedes mismos:
```bash
.\mvnw.cmd test
```

---

## Endpoints Disponibles

Si prefieren probar con `curl` o Postman en lugar de Swagger, aquí están las rutas principales:

**Bicicletas:**
- `POST /api/bicicletas` (Registrar)
- `GET /api/bicicletas/disponibles` (Consultar, acepta filtro `?tipo=MONTANA`)
- `GET /api/bicicletas/{codigo}/historial` (Ver historial)

**Alquileres:**
- `POST /api/alquileres` (Iniciar alquiler)
- `PUT /api/alquileres/{id}/finalizar` (Terminar y calcular precio)

---

## Decisiones y Supuestos

Durante el desarrollo tomé un par de decisiones que quiero aclarar:

1. **Sin tildes ni "ñ" en el código:** Usé `ELECTRICA` y `MONTANA` en los `enum` de Java para evitar cualquier problema de compilación con caracteres especiales en distintos sistemas operativos.
2. **Validaciones estrictas:** Le puse validaciones con Regex al código de la bicicleta (tiene que ser formato `BIC-XXX`) y me aseguré de que no se pueda alquilar una bicicleta si su estado no es exactamente `DISPONIBLE`.
3. **Tiempo automático:** En el endpoint de "Iniciar alquiler", no pido que me envíen la fecha de inicio en el JSON. El sistema la toma automáticamente del reloj del servidor en ese instante para evitar fraudes.

¡Espero que el código sea de su agrado y quedo atento a cualquier feedback técnico!
