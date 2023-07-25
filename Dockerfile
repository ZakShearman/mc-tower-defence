FROM eclipse-temurin:17.0.8_7-jre

RUN mkdir /app
WORKDIR /app

# Download packages
RUN apt-get install wget \
    libstdc++6 libstdc++ # Add libraries required for pyroscope

COPY build/libs/*-all.jar /app/tower_defence.jar
COPY run /app

CMD ["java", "-jar", "/app/tower_defence.jar"]
