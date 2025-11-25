import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "kr.hahaha98757"
version = "1.1.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.java.dev.jna:jna:5.17.0")
    implementation("net.java.dev.jna:jna-platform:5.17.0")
    implementation("com.github.kwhat:jnativehook:2.2.2")
}

kotlin {
    jvmToolchain(17)
}

tasks.named<Jar>("jar") { isEnabled = false }

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("FingerprintMacro")
    archiveClassifier.set("")
    manifest { attributes["Main-Class"] = "kr.hahaha98757.fingerprintmacro.MainKt" }
}

val packageFolder = file("build/jpackage/FingerprintMacro-$version")

tasks.register<Exec>("packageExe") {
    dependsOn("shadowJar")
    if (packageFolder.exists()) packageFolder.deleteRecursively()
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