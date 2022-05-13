plugins {
    id("org.springframework.boot") version "2.6.6"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"

    kotlin("jvm") version "1.6.20"
    kotlin("plugin.spring") version "1.6.20"
}

repositories {
    mavenCentral()
}
dependencies {
    // Kotlin
    implementation(kotlin("reflect"))

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    // OpenApi/Swagger documentation
    implementation("io.springfox:springfox-swagger2:2.9.2") {
        exclude(group = "com.google.guava", module = "guava")
    }
    implementation("io.springfox:springfox-swagger-ui:2.9.2")

    // Metrics
    implementation("io.micrometer:micrometer-core")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // SSH
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("com.h2database:h2:1.4.200")
}

tasks {
    named("bootRun") {
        group = "local"
        description = "Starts the SpringBoot application, executes startLocalInfra first"
    }

    named<Jar>("jar") {
        enabled = true
        classifier = "thin"
    }
    named<Test>("test") {
        useJUnitPlatform()
        filter {
            isFailOnNoMatchingTests = false
            includeTestsMatching("*Test")
            includeTestsMatching("*IT")
            includeTestsMatching("*ITFull")
        }
    }
}

// injects build infos
springBoot {
    buildInfo {
        properties {
            time = null
        }
    }
}
