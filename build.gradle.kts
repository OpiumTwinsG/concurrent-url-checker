plugins {
    kotlin("jvm") version "2.1.10"
    // id("org.jlleitschuh.gradle.ktlint") version "11.5.0"
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

// ktlint {
//    version.set("0.50.0")
//    verbose.set(true)
//    android.set(false)
//    outputToConsole.set(true)
//    ignoreFailures.set(false)
//
//    filter {
//        exclude("**/src/test/**")
//    }
//
//    reporters {
//        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
//        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
//    }
// }

kotlin {
    jvmToolchain(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
tasks.test {
    useJUnitPlatform()
}

tasks.findByName("ktlintTestSourceSetCheck")?.apply { enabled = false }
tasks.findByName("ktlintTestSourceSetFormat")?.apply { enabled = false }
apply(from = "./ktlint.gradle")
