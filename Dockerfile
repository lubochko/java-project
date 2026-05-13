FROM node:22-bookworm-slim AS frontend-build
WORKDIR /build
COPY frontend/package.json frontend/package-lock.json ./frontend/
RUN cd frontend && npm ci
COPY frontend ./frontend
WORKDIR /build/frontend
RUN npm run build

FROM eclipse-temurin:17-jdk-noble AS backend-build
WORKDIR /build
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY src ./src
COPY --from=frontend-build /build/frontend/dist ./frontend/dist
RUN chmod +x mvnw && ./mvnw -B -ntp package -DskipTests -Pcheckstyle-skip -Dskip.frontend=true

FROM eclipse-temurin:17-jre-noble
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app --no-create-home app
COPY --from=backend-build /build/target/*.jar app.jar
USER app
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=50.0 -XX:MaxMetaspaceSize=256m -XX:+ExitOnOutOfMemoryError"
ENV JAVA_OPTS=""
HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=5 \
  CMD curl -fsS "http://127.0.0.1:${PORT:-8080}/actuator/health/liveness" | grep -q UP || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
