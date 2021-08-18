plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "pink.zak.minestom.towerdefence"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    //implementation("com.github.Minestom:Minestom:e72c87f670")
    implementation("net.minestom.server:Minestom:1.0")
    //testImplementation("com.github.Minestom:Minestom:e72c87f670")
    testImplementation("net.minestom.server:Minestom:1.0")
}

//tasks.getByName<Test>("test") {
 //   useJUnitPlatform()
//}