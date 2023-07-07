FROM eclipse-temurin:17-jre

RUN mkdir /app
WORKDIR /app

# Download packages
RUN apk add --no-cache \
    libstdc++6 libstdc++ # Add libraries required for pyroscope

COPY build/libs/*-all.jar /app/tower_defence.jar
COPY run /app

CMD ["java", "-jar", "/app/tower_defence.jar"]
