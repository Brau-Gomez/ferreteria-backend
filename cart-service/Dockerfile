# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY cart-service/pom.xml cart-service/
RUN mvn -B -f cart-service/pom.xml dependency:go-offline -DskipTests
COPY cart-service/src cart-service/src
RUN mvn -B -f cart-service/pom.xml package spring-boot:repackage -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/cart-service/target/cart-service-*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
