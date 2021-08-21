FROM gradle:7.2.0-jdk16 AS BUILD
COPY --chown=gradle:gradle . /home/gradle
WORKDIR /home/gradle

RUN gradle shadowJar

FROM zakshearman/basic-minestom-impl:1.0

WORKDIR /lib/minestom/extensions

COPY --from=BUILD ./home/gradle/build/libs/*.jar ./TowerDefence.jar

RUN ls /lib/minestom/extensions

WORKDIR /lib/minestom
COPY /world ./world
COPY /extensions/TowerDefence ./extensions/TowerDefence
CMD ["java", "-jar", "/lib/minestom/server.jar"]