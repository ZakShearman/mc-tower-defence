plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.sonarqube") version "3.4.0.2513"
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
    implementation("net.kyori:adventure-text-minimessage:4.11.0")
    implementation("com.typesafe:config:1.4.2")

    implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")
    implementation("org.mongodb:mongodb-driver-sync:4.7.1")

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

tasks.register("depsize") {
    description = "Prints dependencies for \"default\" configuration"
    doLast {
        listConfigurationDependencies(configurations["default"])
    }
}

tasks.register("depsize-all-configurations") {
    description = "Prints dependencies for all available configurations"
    doLast {
        configurations
            .filter { it.isCanBeResolved }
            .forEach { listConfigurationDependencies(it) }
    }
}

fun listConfigurationDependencies(configuration: Configuration) {
    val formatStr = "%,10.2f"

    val size = configuration.map { it.length() / (1024.0 * 1024.0) }.sum()

    val out = StringBuffer()
    out.append("\nConfiguration name: \"${configuration.name}\"\n")
    if (size > 0) {
        out.append("Total dependencies size:".padEnd(65))
        out.append("${String.format(formatStr, size)} Mb\n\n")

        configuration.sortedBy { -it.length() }
            .forEach {
                out.append(it.name.padEnd(65))
                out.append("${String.format(formatStr, (it.length() / 1024.0))} kb\n")
            }
    } else {
        out.append("No dependencies found")
    }
    println(out)
}
