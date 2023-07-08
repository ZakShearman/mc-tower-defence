import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    implementation("dev.emortal.minestom:core:9a188df") {
        exclude("net.minestom")
        exclude("dev.hollowcube")
        exclude("com.github.hollow-cube")
    }

//    implementation("com.github.hollow-cube.common:schem:e297e8f999")
    implementation("com.github.ZakShearman.common:schem:fix~argument-block-state-SNAPSHOT") {
        exclude("net.minestom.server", "Minestom")
    }

    implementation("dev.hollowcube:minestom-ce:dev")

    // TNT world format
    implementation("dev.hollowcube:polar:1.3.1")

    implementation("com.github.ben-manes.caffeine:caffeine:3.1.6")
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