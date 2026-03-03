# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jdk
RUN apt-get update && apt-get install -y \
    libgtk-3-0 \
    libgl1 \
    libx11-6 \
    libxxf86vm1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "--module-path", "/app", "--add-modules", "javafx.controls,javafx.fxml", "-jar", "app.jar"]
