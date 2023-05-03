FROM eclipse-temurin:17-jre-alpine

RUN mkdir /app
WORKDIR /app

COPY build/libs/*-all.jar /app/tower_defence.jar
COPY run/maps /app/maps

CMD ["java", "-jar", "/app/tower_defence.jar"]
