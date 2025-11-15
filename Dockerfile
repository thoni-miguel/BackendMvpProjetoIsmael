FROM gradle:8.7-jdk21 AS build
WORKDIR /workspace

COPY gradle gradle
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts
COPY gradle.properties gradle.properties
COPY src src

RUN chmod +x gradlew \
    && ./gradlew --no-daemon installDist

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV PORT=8080 \
    APP_ENV=local \
    SQLITE_JDBC_URL=jdbc:sqlite:data/mvp.sqlite \
    MEDIA_ROOT=data/media

COPY --from=build /workspace/build/install/BackendMvpProjetoIsmael /app
VOLUME ["/app/data"]

EXPOSE 8080

ENTRYPOINT ["./bin/BackendMvpProjetoIsmael"]
