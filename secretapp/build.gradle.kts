import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val archivaUser: String? by project
val archivaPassword: String? by project
//val version: String by project
val archivaHostId: String? by project
val archivaPort: String? by project

group = "edu.vanderbilt"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        isAllowInsecureProtocol = true
        url = uri("http://$archivaHostId:$archivaPort/repository/enigma-test-snapshot/")
    }
}

plugins {
    id("org.springframework.boot") version "2.6.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.8.20"
    kotlin("plugin.spring") version "1.8.20"
    id("maven-publish")
}

dependencies {
    implementation(project(":command"))
    implementation("info.picocli:picocli:4.6.2")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")

    testImplementation(kotlin("test"))

    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.2")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")

}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.bootJar {
    exclude("application.yml")
    exclude("application-clientcredential.yml")
    exclude("application-passthrough.yml")
    archiveFileName.set("leap_cli.jar")
}

configurations {
    val elements = listOf(apiElements, runtimeElements)
    elements.forEach { element ->
        element.get().outgoing.artifacts.removeIf { it ->
            it.buildDependencies.getDependencies(null).contains(tasks.jar.get())
        }
        element.get().outgoing.artifact(tasks.bootJar.get())
    }
}

publishing {
    publications.create<MavenPublication>("secretapp") {
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
//val compileKotlin: KotlinCompile by tasks
//compileKotlin.kotlinOptions {
//    jvmTarget = "1.8"
//}
//val compileTestKotlin: KotlinCompile by tasks
//compileTestKotlin.kotlinOptions {
//    jvmTarget = "1.8"
//}