# Build stage with Maven + Java 22
FROM maven:3.9.6-eclipse-temurin-22 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# Runtime stage with Java 22 JRE
FROM eclipse-temurin:22-jre
WORKDIR /app
COPY --from=build /build/target/tsdb-producer-1.0-SNAPSHOT-jar-with-dependencies.jar ./app.jar
# copy data files (docker-compose mounts ./simulator/data -> /app/data),
# but keep them in image for convenience if not mounted
COPY data ./data

ENV DB_HOST=timescaledb
ENV DB_PORT=5432
ENV DB_NAME=docs_ops
ENV DB_USER=postgres
ENV DB_PASS=password
ENV WORD_INTERVAL_SECONDS=60
ENV FAST_MODE=false

EXPOSE 8080
ENTRYPOINT ["sh","-c","java -Xmx256m -jar /app/app.jar"]
