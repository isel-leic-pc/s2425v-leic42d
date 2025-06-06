plugins {
    kotlin("jvm") version "2.1.10"
}


group = "pt.isel.pc"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("io.github.microutils:kotlin-logging:3.0.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}