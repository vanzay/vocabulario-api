plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.2.21"
}

group = "vanzay"
version = "1.0.0"
description = "vocabulario-api"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.security:spring-security-crypto:7.0.3")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")

    implementation("ch.qos.logback:logback-classic:1.5.32")
    implementation("com.itextpdf:itextpdf:5.5.13.5")
    implementation("org.bouncycastle:bcprov-jdk18on:1.83")
    implementation("com.opencsv:opencsv:5.12.0")
    implementation("com.positiondev.epublib:epublib-core:3.1") {
        exclude("org.slf4j", "slf4j-simple")
    }
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("org.apache.commons:commons-text:1.15.0")
    implementation("org.apache.lucene:lucene-core:9.3.0")
    implementation("org.apache.lucene:lucene-queryparser:9.3.0")
    implementation("org.jsoup:jsoup:1.22.1")
    implementation("org.postgresql:postgresql:42.7.10")

    implementation(files("libs/text-analyser-1.1.1.jar"))

    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-mail-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
    annotation("org.springframework.transaction.annotation.Transactional")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
