plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.sonarqube") version "3.3"
}

group = "pink.zak.minestom"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://mvn.zak.pink/shapshots") // pre-release builds
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.ZakShearman:Minestom:e5c7a20b0b")
    implementation("net.kyori:adventure-text-minimessage:4.10.1")
    implementation("com.typesafe:config:1.4.2")
    implementation("com.google.guava:guava:31.1-jre")

    implementation("com.github.ben-manes.caffeine:caffeine:3.0.6")
    implementation("org.mongodb:mongo-java-driver:3.12.10")

    testImplementation("com.github.ZakShearman:Minestom:e5c7a20b0b")

    //testImplementation("net.minestom.server:Minestom:1.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sonarqube {
    properties {
        property("sonar.projectKey", "ZakShearman_mc-tower-defence")
        property("sonar.organization", "zakshearman")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
