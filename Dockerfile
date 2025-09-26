FROM gradle:8.10.2-jdk21-alpine AS build
WORKDIR /app

COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle ./gradle

RUN gradle dependencies --no-daemon || true

COPY . .

# Evitamos validateStructure en Docker
RUN gradle clean bootJar --no-daemon -x validateStructure

FROM eclipse-temurin:21-jre

WORKDIR /app

# 👇 Aquí copiamos el JAR correcto desde el módulo app-service
COPY --from=build /app/applications/app-service/build/libs/CrediYaAutenticationService.jar app.jar

EXPOSE 8090
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java","-jar","app.jar"]
