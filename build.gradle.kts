plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("dev.kord:kord-core:0.17.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("io.insert-koin:koin-core:3.5.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("com.google.genai:google-genai:1.35.0")
}

kotlin {
    jvmToolchain(24)
}

tasks.test {
    useJUnitPlatform()
}