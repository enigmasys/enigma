import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar


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
//java.targetCompatibility = JavaVersion.VERSION_17

group = "edu.vanderbilt"
version = "0.0.1-SNAPSHOT"

dependencies {

//    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.0.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.21")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.1")
    implementation("com.azure:azure-storage-blob:12.22.0")
    implementation("info.picocli:picocli:4.7.3")
    implementation("junit:junit:4.13.2")

    implementation("com.microsoft.azure:msal4j:1.13.8")
    implementation("com.microsoft.azure:msal4j-persistence-extension:1.2.0")
    implementation("com.networknt:json-schema-validator:1.0.81")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.0.6")
    testImplementation("io.projectreactor:reactor-test:3.5.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")


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