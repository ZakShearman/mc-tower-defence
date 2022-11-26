FROM eclipse-temurin:17-jre-alpine

RUN mkdir /app
WORKDIR /app

COPY build/libs/*-all.jar /app/minestom-tower-defence.jar
COPY run/world /app/world/

# parallelism is manually set to 3 as I commonly run with only 2 cores causing a parallelism value of 1
# if paralellism = 1, the behaviour of the ForkJoinPool commonPool is completely different (1 new thread per task)
# and loading is more than 10x worse
CMD ["java", "-Djava.util.concurrent.ForkJoinPool.common.parallelism=3", "-jar", "/app/minestom-tower-defence.jar"]