import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val archivaUser: String? by project
val archivaPassword: String? by project
//val version: String by project
val archivaHostId: String? by project
val archivaPort: String? by project

plugins {
    id("org.springframework.boot") version "3.0.6"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    id("maven-publish")
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17


group = "edu.vanderbilt"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":command"))
    implementation("info.picocli:picocli:4.7.3")

    implementation("org.springframework.boot:spring-boot-starter-webflux:3.0.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.21")
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

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.bootJar{
    exclude("application.yml")
    exclude("application-clientcredential.yml")
    exclude("application-passthrough.yml")
    archiveFileName.set("leap_cli.jar")

}

repositories {
    mavenCentral()
    maven {
        isAllowInsecureProtocol = true
        url = uri("http://$archivaHostId:$archivaPort/repository/enigma-test-snapshot/")
    }
}

configurations {
    val elements = listOf(apiElements, runtimeElements)
    elements.forEach { element ->
        element.get().outgoing.artifacts.removeIf { it -> it.buildDependencies.getDependencies(null).contains(tasks.jar.get())}
        element.get().outgoing.artifact(tasks.bootJar.get())
    }
}


publishing {
    publications.create<MavenPublication>("secretapp"){
        from(components["java"])
    }

    repositories {
        archivaHostId?.let {
            archivaPassword?.let {
                maven {
//                    name = "rootPublish"
                    val internalRepoUrl = "http://$archivaHostId:$archivaPort/repository/enigma-test-release"
                    val snapshotsRepoUrl = "http://$archivaHostId:$archivaPort/repository/enigma-test-snapshot"
                    url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else internalRepoUrl)

                    logger.info("URL = \"$url\"")
                    isAllowInsecureProtocol = true
                    authentication {
                        create<BasicAuthentication>("basic")
                    }
                    credentials {
                        username = archivaUser
                        password = archivaPassword
                    }
                }
            }
        }


    }
}