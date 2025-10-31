# 1) build
FROM gradle:8.10-jdk17 AS build
WORKDIR /home/gradle/src
COPY . .
# garante que o wrapper rode no Fly
RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

# 2) runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
