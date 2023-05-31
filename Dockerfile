FROM eclipse-temurin:17-jre-alpine

RUN mkdir /app
WORKDIR /app

COPY build/libs/*-all.jar /app/tower_defence.jar
COPY run/maps /app/maps
COPY run/mobs /app/mobs
COPY run/precalculated /app/precalculated
COPY run/towers /app/towers

CMD ["java", "-jar", "/app/tower_defence.jar"]
