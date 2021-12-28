import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("kapt") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("edu.sc.seis.launch4j") version "2.5.1"
    id("java")

}

group = "com.github.miong"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.eclipse.org/content/repositories/paho-releases/")
}

dependencies {
    testImplementation(kotlin("test"))

    val ktxVersion = "1.9.12-b1"
    val gdxVersion = "1.9.12"
    val exposedVersion = "0.34.1"
    val pahoVersion = "1.2.0"
    val colMessageVersion = "0.0.0.13"

    api(group = "com.badlogicgames.gdx", name = "gdx-backend-lwjgl", version = gdxVersion)
    api(group = "com.badlogicgames.gdx", name = "gdx-platform", version = gdxVersion, classifier = "natives-desktop")
    api(group = "com.badlogicgames.gdx", name = "gdx", version = gdxVersion)

    api(group = "io.github.libktx", name = "ktx-app", version = ktxVersion)
    api(group = "io.github.libktx", name = "ktx-collections", version = ktxVersion)
    api(group = "io.github.libktx", name = "ktx-graphics", version = ktxVersion)
    api(group = "io.github.libktx", name = "ktx-log", version = ktxVersion)
    api(group = "io.github.libktx", name = "ktx-actors", version = ktxVersion)
    api(group = "io.github.libktx", name = "ktx-scene2d", version = ktxVersion)
    api(group = "io.github.libktx", name = "ktx-style", version = ktxVersion)


    implementation("io.github.microutils:kotlin-logging-jvm:2.0.8")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.xerial:sqlite-jdbc:3.30.1")

    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.slf4j:slf4j-simple:1.6.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:$pahoVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")

    implementation("com.github.miong:CardsOfLegendsMessages:$colMessageVersion")

    implementation("com.badlogicgames.gdx:gdx-freetype:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.jar {
    manifest {
        attributes(mapOf("Main-Class" to "com.bubul.col.launcher.MainKt"))
    }
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("CardsOfLegends-Shadow")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "com.bubul.col.launcher.MainKt"))
        }
    }
}

launch4j {
    mainClassName = "com.bubul.col.game.MainKt"
    dontWrapJar = true
    jarTask = project.tasks.jar.get()
}

tasks.register<DefaultTask>("createPackage") {
    dependsOn("createExe")
    val rootDir = project.mkdir(layout.buildDirectory.dir("package"))
    project.copy {
        from(layout.buildDirectory.dir("../resources"))
        into(rootDir)
    }
    project.copy {
        from(layout.buildDirectory.dir("launch4j"))
        include("*")
        into(rootDir)
    }
}

tasks.register<DefaultTask>("cleanPackage") {
    project.delete(layout.buildDirectory.dir("package"))
}