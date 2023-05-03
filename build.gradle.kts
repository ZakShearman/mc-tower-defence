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
    implementation("dev.emortal.minestom:core:cc4a043") {
        exclude("net.minestom.server", "Minestom")
    }
    implementation("dev.emortal.api:kurushimi-sdk:684e7c4")

//    implementation("net.minestom.server:Minestom:1.0")
    implementation("com.github.Minestom.Minestom:Minestom:8ad2c7701f")
    implementation("net.kyori:adventure-text-minimessage:4.11.0")

    // TNT world format
    implementation("com.github.EmortalMC:TNT:4ef1b53482")

    implementation("com.github.ben-manes.caffeine:caffeine:3.1.5")
}

application {
    mainClass.set("pink.zak.minestom.towerdefence.TowerDefenceServer")
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}