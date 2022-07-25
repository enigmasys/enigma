import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


val archivaUser: String? by project
val archivaPassword: String? by project
//val version: String by project
val archivaHostId: String? by project
val archivaPort: String? by project


plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.springframework.boot") version "2.6.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
//    id("org.jetbrains.kotlin.jvm")
//    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
}

group = "edu.vanderbilt"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven {
        isAllowInsecureProtocol = true
        url = uri("http://$archivaHostId:$archivaPort/repository/enigma-test-snapshot/")
    }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("edu.vanderbilt:common:0.0.1-SNAPSHOT")
                implementation("org.springframework.boot:spring-boot-starter-webflux")

//                implementation(files("../common"))
//                implementation(project(":common"))
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "desktop"
            packageVersion = "1.0.0"
        }
    }
}