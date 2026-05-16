# Usa la imagen oficial de Java 17 (Maven incluido para compilar)
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compilar el proyecto (saltando tests para que el deploy sea más rápido)
RUN mvn clean package -DskipTests

# Usa una imagen más ligera solo con Java 17 para correr la app
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copiar el .jar compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar
# Exponer el puerto
EXPOSE 8080
# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
