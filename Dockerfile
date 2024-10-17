FROM --platform=$TARGETPLATFORM azul/zulu-openjdk:21.0.5-jre

RUN mkdir /app
WORKDIR /app

COPY build/libs/*-all.jar /app/tower-defence.jar
COPY run /app

ENTRYPOINT ["java"]
CMD ["-jar", "/app/tower-defence.jar"]
