plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "pink.zak.minestom.towerdefence"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/") // minimessage? todo plz fix
    maven("https://jitpack.io")
}

dependencies {
    //compileOnly("com.github.Minestom:Minestom:6e16fb7b13")
    compileOnly("com.github.ZakShearman:Minestom:12bc35f89")
    implementation("net.kyori:adventure-text-minimessage:4.2.0-SNAPSHOT")
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.5")
    //testImplementation("com.github.Minestom:Minestom:6e16fb7b13")
    testImplementation("com.github.ZakShearman:Minestom:12bc35f89")

    //testImplementation("net.minestom.server:Minestom:1.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}