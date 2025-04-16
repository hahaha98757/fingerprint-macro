import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "kr.hahaha98757"
version = "1.0.0"

java { toolchain.languageVersion.set(JavaLanguageVersion.of(17)) }

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.java.dev.jna:jna:5.17.0")
    implementation("net.java.dev.jna:jna-platform:5.17.0")
    implementation("com.github.kwhat:jnativehook:2.2.2")
}

tasks.named<Jar>("jar") { isEnabled = false }

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("FingerprintMacro")
    archiveClassifier.set("")
    manifest { attributes["Main-Class"] = "kr.hahaha98757.fingerprintmacro.MainKt" }
}

tasks.register<Exec>("packageExe") {
    commandLine(
        "jpackage",
        "--type", "app-image",
        "--input", "build/libs",
        "--name", "FingerprintMacro-$version",
        "--main-jar", "FingerprintMacro-$version.jar",
        "--icon", "icon.ico",
        "--dest", "build/jpackage",
        "--win-console"
    )
    doLast { file("build/jpackage/FingerprintMacro-$version/FingerprintMacro-$version.ico").delete() }
}

tasks.named("build") {
    dependsOn("shadowJar")
    finalizedBy("packageExe")
}