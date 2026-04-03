# Dockerfile para el Backend (Spring Boot)
# Usa una imagen de Maven para compilar y luego una de JRE para ejecutar

# Fase de Compilación
FROM maven:3.9.12-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Fase de Ejecución
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

# Usamos el perfil de desarrollo por defecto
ENTRYPOINT ["java", "-jar", "app.jar"]
