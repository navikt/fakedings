import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val kotlinLoggingVersion = "3.0.5"
val logbackVersion = "1.5.12"
val mockOauth2ServerVersion = "2.1.10"
val mainClassKt = "fakedings.ApplicationKt"

plugins {
    application
    kotlin("jvm") version "2.1.0"
    id("org.jmailen.kotlinter") version "4.5.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.ben-manes.versions") version "0.51.0"
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
    implementation("no.nav.security:mock-oauth2-server:$mockOauth2ServerVersion"){
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "io.github.microutils", module = "kotlin-logging")
    }
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
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
}
