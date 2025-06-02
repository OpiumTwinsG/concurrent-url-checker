plugins {
    kotlin("jvm") version "2.1.10"
    // id("org.jlleitschuh.gradle.ktlint") version "11.5.0"
    application
    id("jacoco")
}

group = "com.example.urlchecker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
sourceSets {
    create("integrationTest") {
        kotlin.srcDir("src/integrationTest/kotlin")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets["main"].output
        compileClasspath += configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath
    }
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("io.ktor:ktor-client-core:3.1.3")
    implementation("io.ktor:ktor-client-cio:3.1.3")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    val integrationTestImplementation by configurations.getting
    integrationTestImplementation(kotlin("test"))
    integrationTestImplementation("io.ktor:ktor-client-mock:3.1.3")
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

tasks {
    test { useJUnitPlatform() }

    // отдельный Task для integrationTest
    val integrationTest by registering(Test::class) {
        description = "Runs integration tests"
        group = "verification"
        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
        shouldRunAfter(test)
    }

    // «check» запускает и unit, и integration
    check { dependsOn(integrationTest) }

    // ---- Jacoco ----
    jacocoTestReport {
        dependsOn(test, integrationTest)
        reports {
            html.required.set(true)
            xml.required.set(false)
        }
    }
}

tasks.findByName("ktlintTestSourceSetCheck")?.apply { enabled = false }
tasks.findByName("ktlintTestSourceSetFormat")?.apply { enabled = false }
apply(from = "./ktlint.gradle")
