# Dockerfile multi-stage para ERPTahona

# Stage 1: build
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml mvnw mvnw./cmd ./
COPY .mvn .mvn
COPY src ./src
RUN mvn -B -DskipTests package

# Stage 2: runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Crear usuario no-root
RUN addgroup -S erpgroup && adduser -S erpuser -G erpgroup
COPY --from=build /workspace/target/*.jar app.jar
# Directorio para logs y otros archivos
RUN mkdir -p /var/log/erp && chown -R erpuser:erpgroup /var/log/erp
USER erpuser
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
