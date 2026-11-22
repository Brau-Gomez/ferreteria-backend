# syntax=docker/dockerfile:1
# Multi-stage para auth-service. El contexto de build es la RAÍZ del repo (no el módulo),
# porque Maven necesita el pom.xml padre para resolver el parent y sus versiones.

# --- Etapa 1: compilación ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Primero los pom: mientras no cambien, la capa con las dependencias descargadas se reutiliza
COPY pom.xml ./
COPY auth-service/pom.xml auth-service/
RUN mvn -B -f auth-service/pom.xml dependency:go-offline -DskipTests

# Después el código del módulo
COPY auth-service/src auth-service/src
RUN mvn -B -f auth-service/pom.xml package spring-boot:repackage -DskipTests

# --- Etapa 2: runtime mínimo ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
