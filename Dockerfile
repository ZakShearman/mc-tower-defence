FROM gradle:7.2.0-jdk17 AS build

WORKDIR /home/gradle/src
COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts ./

RUN gradle build

COPY --chown=gradle:gradle . .

RUN gradle clean shadowJar

FROM eclipse-temurin:17-jre-alpine

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/minestom-tower-defence.jar
COPY run/world /app/world/
WORKDIR /app

CMD ["java", "-jar", "/app/minestom-tower-defence.jar"]