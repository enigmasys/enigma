import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

val archivaUser: String? by project
val archivaPassword: String? by project
//val version: String by project
val archivaHostId: String? by project
val archivaPort: String? by project

plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.21"
    id("maven-publish")
}


group = "edu.vanderbilt"
version = "0.0.1-SNAPSHOT"
//# THis is specifically testing the CLI_VERSIOn and CLI_RELEASE_URL properties
val CLI_VERSION:String by project
val CLI_RELEASE_URL:String by project

tasks.named("classes") {
    dependsOn("createProperties")
}


tasks.create("createProperties") {
    dependsOn("processResources")

    doLast {
        // Create file if not present..
        val propertiesFile = File("$buildDir/resources/main/version.properties")

        propertiesFile.parentFile.mkdirs()
        propertiesFile.createNewFile()


        File("$buildDir/resources/main/version.properties").writer().use { w ->
            val p = Properties()
            p["version"] = CLI_VERSION
            p["releaseURL"] = CLI_RELEASE_URL
            p.store(w, null)
        }
    }
}


repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":command"))
    implementation(project(":common"))
    implementation("info.picocli:picocli:4.7.5")
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.1.5")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
//    kotlinOptions { jvmTarget = "1.8" }
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

kotlin{
    jvmToolchain(17)
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.bootJar{
//    exclude("application.yml")
    exclude("application-clientcredential.yml")
//    exclude("application-passthrough.yml")
    archiveFileName.set("leap_cli.jar")
}

//
//repositories {
//    mavenCentral()
//    maven {
//        isAllowInsecureProtocol = true
//        url = uri("http://$archivaHostId:$archivaPort/repository/enigma-test-snapshot/")
//    }
//}

configurations {
    val elements = listOf(apiElements, runtimeElements)
    elements.forEach { element ->
        element.get().outgoing.artifacts.removeIf { it -> it.buildDependencies.getDependencies(null).contains(tasks.jar.get())}
        element.get().outgoing.artifact(tasks.bootJar.get())
    }
}

//
//publishing {
//    publications.create<MavenPublication>("secretapp"){
//        from(components["java"])
//    }
//
//    repositories {
//        archivaHostId?.let {
//            archivaPassword?.let {
//                maven {
////                    name = "rootPublish"
//                    val internalRepoUrl = "http://$archivaHostId:$archivaPort/repository/enigma-test-release"
//                    val snapshotsRepoUrl = "http://$archivaHostId:$archivaPort/repository/enigma-test-snapshot"
//                    url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else internalRepoUrl)
//
//                    logger.info("URL = \"$url\"")
//                    isAllowInsecureProtocol = true
//                    authentication {
//                        create<BasicAuthentication>("basic")
//                    }
//                    credentials {
//                        username = archivaUser
//                        password = archivaPassword
//                    }
//                }
//            }
//        }
//
//
//    }
//}