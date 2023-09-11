val gradleVersion = "6.7"
val kotlinLoggingVersion = "3.0.5"
val logbackVersion = "1.4.11"
val mockOauth2ServerVersion = "1.0.0"
val kotlinVersion = "1.9.10"
val mainClassKt = "fakedings.ApplicationKt"

plugins {
    application
    kotlin("jvm") version "1.9.10"
    id("org.jmailen.kotlinter") version "3.16.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.ben-manes.versions") version "0.47.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.18"
}

application {
    mainClass.set(mainClassKt)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    withType<Wrapper> {
        gradleVersion = "8.3"
    }
}
