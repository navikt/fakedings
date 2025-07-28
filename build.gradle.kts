import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val mainClassKt = "fakedings.ApplicationKt"

plugins {
    application
    kotlin("jvm") version "2.2.0"
    id("org.jmailen.kotlinter") version "5.2.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.ben-manes.versions") version "0.52.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.18"
}

application {
    mainClass.set(mainClassKt)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

apply(plugin = "org.jmailen.kotlinter")

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("no.nav.security:mock-oauth2-server:2.2.1") {
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "io.github.microutils", module = "kotlin-logging")
    }
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.2.0")
}

tasks {
    withType<org.jmailen.gradle.kotlinter.tasks.LintTask> {
        dependsOn("formatKotlin")
    }

    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveBaseName.set("app")
        archiveClassifier.set("")
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to mainClassKt
                )
            )
        }
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    test {
        useJUnitPlatform()
    }

    named("dependencyUpdates", com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class).configure {
        val immaturityLevels = listOf("rc", "cr", "m", "beta", "alpha", "preview") // order is important
        val immaturityRegexes = immaturityLevels.map { ".*[.\\-]$it[.\\-\\d]*".toRegex(RegexOption.IGNORE_CASE) }
        fun immaturityLevel(version: String): Int = immaturityRegexes.indexOfLast { version.matches(it) }
        rejectVersionIf { immaturityLevel(candidate.version) > immaturityLevel(currentVersion) }
    }
}
