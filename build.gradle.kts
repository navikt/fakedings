import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jmailen.gradle.kotlinter.tasks.LintTask

val mainClassKt = "fakedings.ApplicationKt"

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.dependency.updates)
    alias(libs.plugins.use.latest.versions)
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
    implementation(platform(libs.jackson.bom))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.mock.oauth2.server) {
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "io.github.microutils", module = "kotlin-logging")
    }
    implementation(libs.logback.classic)
    implementation(libs.kotlin.logging)
    implementation(libs.logstash.encoder)
    testImplementation(libs.kotlin.test.junit5)
}

tasks {
    kotlin {
        jvmToolchain(21)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    withType<LintTask> {
        dependsOn("formatKotlin")
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
