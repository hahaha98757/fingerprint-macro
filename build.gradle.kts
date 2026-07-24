import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.3.21"
    id("com.gradleup.shadow") version "9.4.3"
}

group = "kr.hahaha98757"
version = "1.2.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.kwhat:jnativehook:2.2.2")
}

kotlin {
    jvmToolchain(25)
}

tasks.named<Jar>("jar") { isEnabled = false }

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("FingerprintMacro")
    archiveClassifier.set("")
    manifest { attributes["Main-Class"] = "kr.hahaha98757.fingerprintmacro.MainKt" }
}

val packageFolder = file("build/jpackage/FingerprintMacro-$version")

tasks.register<Exec>("packageExe") {
    group = "build"
    description = "Packages the application into an executable format using JPackage."
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
        "--win-console",
        "--java-options", "--enable-native-access=ALL-UNNAMED"
    )
    doLast {
        file("build/jpackage/FingerprintMacro-$version/FingerprintMacro-$version.ico").delete()
        copy {
            from("resource/README.txt")
            into(packageFolder)
        }
    }
}