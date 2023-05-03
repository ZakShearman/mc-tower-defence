FROM eclipse-temurin:17-jre-alpine

RUN mkdir /app
WORKDIR /app

COPY build/libs/*-all.jar /app/tower_defence.jar
COPY run/world /app/world/
COPY run/world.tnt /app/world.tnt

CMD ["java", "-jar", "/app/tower_defence.jar"]
