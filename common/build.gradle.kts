import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar


val archivaUser: String? by project
val archivaPassword: String? by project
//val version: String by project
val archivaHostId: String? by project
val archivaPort: String? by project



plugins {
    id("org.springframework.boot") version "2.6.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.8.20"
    kotlin("plugin.spring") version "1.8.20"
    id("maven-publish")
}

group = "edu.vanderbilt"
version = "0.0.1-SNAPSHOT"

dependencies {

//    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.6")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("com.azure:azure-storage-blob:12.14.2")
    implementation("info.picocli:picocli:4.6.2")
    implementation("junit:junit:4.13.1")

    implementation("com.microsoft.azure:msal4j:1.11.3")
    implementation("com.microsoft.azure:msal4j-persistence-extension:1.1.0")
    implementation("com.networknt:json-schema-validator:1.0.71")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    testImplementation(kotlin("test"))

}

tasks.test {
    useJUnitPlatform()
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    jvmArgs = mutableListOf("--enable-preview")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

repositories {
    mavenCentral()
    maven {
        isAllowInsecureProtocol = true
        url = uri("http://$archivaHostId:$archivaPort/repository/enigma-test-snapshot/")
    }
}

publishing {
    publications.create<MavenPublication>("common").from(components["java"])
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