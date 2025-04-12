import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "kr.hahaha98757"
version = "1.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.java.dev.jna:jna:5.17.0")
    implementation("net.java.dev.jna:jna-platform:5.17.0")
    implementation("com.github.kwhat:jnativehook:2.2.2")
}

val buildFolder = file("${layout.buildDirectory.locationOnly.get()}/libs/FingerprintMacro-v$version")

tasks.named<ShadowJar>("shadowJar") {
    destinationDirectory.set(buildFolder)
    archiveBaseName.set("FingerprintMacro")
    archiveVersion.set("")
    archiveClassifier.set("")
    manifest { attributes["Main-Class"] = "kr.hahaha98757.fingerprintmacro.MainKt" }
    finalizedBy("copyResources")
}

tasks.register<Copy>("copyResources") {
    from("resources")
    into(buildFolder)
}