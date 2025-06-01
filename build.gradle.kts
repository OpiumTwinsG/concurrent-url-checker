plugins {
    kotlin("jvm") version "2.1.10"
    application
}

group = "com.example.urlchecker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("io.ktor:ktor-client-core:3.1.3")
    implementation("io.ktor:ktor-client-cio:3.1.3")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.example.urlchecker.MainKt")
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
tasks.test {
    useJUnitPlatform()
}

