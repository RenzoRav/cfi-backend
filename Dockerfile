# =========================
# 1ª etapa: build
# =========================
FROM gradle:8.10-jdk17-alpine AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon

# =========================
# 2ª etapa: runtime
# =========================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/app.jar

# porta do Spring
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
