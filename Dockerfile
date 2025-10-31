# 1) build
FROM gradle:8.10-jdk17-alpine AS build
WORKDIR /home/gradle/src
COPY . .
RUN gradle bootJar --no-daemon

# 2) run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
