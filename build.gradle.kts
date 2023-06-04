import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("application")
    id("io.freefair.lombok") version "6.6"
}

group = "pink.zak.minestom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://jitpack.io")
    maven("https://repo.emortal.dev/snapshots")
    maven("https://repo.emortal.dev/releases")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("dev.emortal.minestom:core:c53fbcd") {
        exclude("net.minestom.server", "Minestom")
    }
    implementation("dev.emortal.api:kurushimi-sdk:a1dcf4d") {
        exclude("dev.emortal.api", "common-proto-sdk")
        exclude("net.minestom.server")
    }

//    implementation("net.minestom.server:Minestom:1.0")
//    implementation("com.github.Minestom.Minestom:Minestom:8ad2c7701f")
//    implementation("dev.hollowcube:minestom-ce:7f3144337d")

    implementation("com.github.Minestom:MinestomDataGenerator:e6ebd0fb9b") {
        version {
            strictly("e6ebd0fb9b")
        }
    }
    implementation("net.kyori:adventure-text-minimessage:4.13.0")

    // TNT world format
    implementation("dev.emortal.tnt:TNT:1.19.4_2")

    implementation("com.github.ben-manes.caffeine:caffeine:3.1.5")
}
application {
    mainClass.set("pink.zak.minestom.towerdefence.TowerDefenceServer")
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}