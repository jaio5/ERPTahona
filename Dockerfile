FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -q --fail-never
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
# mysql-client: necesario para los backups desde la aplicación (mysqldump)
# curl: usado por el healthcheck de docker compose
RUN apt-get update \
    && apt-get install -y --no-install-recommends mysql-client curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/target/*.jar app.jar
# Directorios persistidos mediante volúmenes en docker-compose.yml
RUN mkdir -p /app/backups /app/impresiones /app/logs /app/certs
# Usuario sin privilegios: la app no necesita root para nada
RUN groupadd --system erp && useradd --system --gid erp --no-create-home erp \
    && chown -R erp:erp /app
USER erp
# La JVM respeta el límite de memoria del contenedor
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
