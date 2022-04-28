FROM gradle:7.4.1-jdk17 AS gradle
COPY --chown=gradle:gradle . /gradleBuild
WORKDIR /gradleBuild

RUN gradle clean shadowJar --no-daemon


FROM azul/zulu-openjdk-alpine:18.0.1
MAINTAINER ZakShearman

RUN mkdir -p /app/extensions
WORKDIR /app

ADD https://github.com/ZakShearman/Operadora/releases/download/1.1.0/Operadora-1.1.0-29.jar server.jar
COPY --from=gradle /gradleBuild/build/libs/*.jar /app/extensions/tower-defence.jar
RUN ls -R /app

CMD ["java", "-jar", "server.jar"]
