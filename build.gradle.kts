import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "pink.zak.minestom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.emortal.dev/snapshots")
    maven("https://repo.emortal.dev/releases")

    maven("https://jitpack.io")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("dev.emortal.minestom:core:80be174") {
        exclude(group = "net.minestom", module = "minestom-snapshots")
    }

    implementation("net.minestom:minestom-snapshots:d0754f2a15")
    implementation("dev.emortal.api:common-proto-sdk:2584fd2")

    implementation("dev.hollowcube:schem:1.2.0")
    // Polar world format
    implementation("dev.hollowcube:polar:1.11.3")

    implementation("io.github.cdimascio:dotenv-java:3.0.2")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    shadowJar {
        mergeServiceFiles()

        manifest {
            attributes(
                "Main-Class" to "pink.zak.minestom.towerdefence.TowerDefenceServer",
                "Multi-Release" to true
            )
        }
    }

    withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    build { dependsOn(shadowJar) }
}
