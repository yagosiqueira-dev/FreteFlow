
# Etapa 1: compilação
FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw \
    && ./mvnw -B -DskipTests package

# Etapa 2: execução
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --system app \
    && useradd --system --gid app --uid 10001 app

COPY --from=build /build/target/freteflow-0.0.1-SNAPSHOT.jar /app/app.jar

USER app:app

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
