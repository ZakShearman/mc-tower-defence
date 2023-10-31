FROM eclipse-temurin:21.0.1_12-jre

RUN mkdir /app
WORKDIR /app

# Download packages
RUN apt-get install wget \
    libstdc++6 libstdc++ # Add libraries required for pyroscope

COPY build/libs/*-all.jar /app/tower_defence.jar
COPY run /app

CMD ["java", "-jar", "/app/tower_defence.jar"]
