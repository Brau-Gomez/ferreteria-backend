# syntax=docker/dockerfile:1
# Multi-stage para catalog-service. El contexto de build es la RAÍZ del repo (no el módulo),
# porque Maven necesita el pom.xml padre para resolver el parent y sus versiones.

# --- Etapa 1: compilación ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Primero los pom: mientras no cambien, la capa con las dependencias descargadas se reutiliza
COPY pom.xml ./
COPY catalog-service/pom.xml catalog-service/
RUN mvn -B -f catalog-service/pom.xml dependency:go-offline -DskipTests

# Después el código del módulo
COPY catalog-service/src catalog-service/src
RUN mvn -B -f catalog-service/pom.xml package spring-boot:repackage -DskipTests

# --- Etapa 2: runtime mínimo ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/catalog-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
