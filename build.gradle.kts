plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "pink.zak.minestom.towerdefence"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    compileOnly("com.github.Minestom:Minestom:6ab94bb778")
    implementation("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT")
    //implementation("net.minestom.server:Minestom:1.0")
    testImplementation("com.github.Minestom:Minestom:6ab94bb778")
    //testImplementation("net.minestom.server:Minestom:1.0")
}

//tasks.getByName<Test>("test") {
 //   useJUnitPlatform()
//}