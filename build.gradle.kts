plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    application
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
    implementation("ch.qos.logback:logback-classic:1.5.34")
    implementation("io.insert-koin:koin-core:3.5.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("io.ktor:ktor-client-cio:3.1.3")
    implementation("io.ktor:ktor-server-core:3.5.0")
    implementation("io.ktor:ktor-server-netty:3.5.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.1.3")
    implementation("redis.clients:jedis:5.1.2")
}

kotlin {
    jvmToolchain(24)
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("org.example.MainKt")
}
