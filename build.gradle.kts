val gradleVersion = "6.7"
val kotlinLoggingVersion = "2.0.3"
val logbackVersion = "1.2.3"
val mockOauth2ServerVersion = "0.2.2"
val junitJupiterVersion = "5.7.0"
val kotlinVersion = "1.4.20"
val kotestVersion = "4.3.1"
val mainClassKt = "fakedings.ApplicationKt"

plugins {
    application
    kotlin("jvm") version "1.4.0"
    id("org.jmailen.kotlinter") version "3.2.0"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("com.github.ben-manes.versions") version "0.36.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.15"
}

application {
    mainClassName = mainClassKt
    mainClass.set(mainClassKt)
}

java {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
    withJavadocJar()
    withSourcesJar()
}

apply(plugin = "org.jmailen.kotlinter")

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(platform("org.http4k:http4k-bom:3.274.0"))
    implementation("org.http4k:http4k-core")
    implementation( "org.http4k:http4k-server-netty")
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("no.nav.security:mock-oauth2-server:$mockOauth2ServerVersion"){
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "io.github.microutils", module = "kotlin-logging")
    }
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion") // for kotest core jvm assertions
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
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
            jvmTarget = "14"
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<Wrapper> {
        gradleVersion = gradleVersion
    }
}
