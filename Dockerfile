
FROM node:20-alpine AS frontend-build
WORKDIR /build/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-17 AS jar-build
WORKDIR /build
COPY pom.xml .
COPY src ./src
COPY --from=frontend-build /build/frontend/dist ./frontend/dist
RUN mvn -B -ntp -Dskip.frontend=true -Pcheckstyle-skip package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget
COPY --from=jar-build /build/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENV PORT=8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=5 \
  CMD sh -c 'wget -qO- "http://127.0.0.1:${PORT:-8080}/actuator/health/liveness" | grep -q UP || exit 1'
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
