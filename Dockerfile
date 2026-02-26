# ─── Stage 1: Build ───────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-jammy AS builder
WORKDIR /app

# Copiar wrapper y pom primero para aprovechar cache de capas
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && sed -i 's/\r$//' mvnw
RUN ./mvnw dependency:go-offline -B

# Copiar fuentes y compilar
COPY src src
RUN ./mvnw package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
