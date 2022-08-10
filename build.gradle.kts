import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.2"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.allopen") version "1.7.10"
    kotlin("plugin.spring") version "1.7.10"
    kotlin("plugin.jpa") version "1.7.10"
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("org.springframework.transaction.annotation.Transactional")
}

group = "vanzay"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.7.2")
    implementation("org.springframework.boot:spring-boot-starter-mail:2.7.2")
    implementation("org.springframework.boot:spring-boot-starter-web:2.7.2")
    implementation("org.springframework.boot:spring-boot-starter-validation:2.7.2")
    implementation("org.springframework.security:spring-security-crypto:5.7.2")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("ch.qos.logback:logback-parent:1.2.11")
    implementation("com.itextpdf:itextpdf:5.5.13.3")
    implementation("com.opencsv:opencsv:5.6")
    implementation("com.positiondev.epublib:epublib-core:3.1") {
        exclude("org.slf4j", "slf4j-simple")
    }
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.apache.lucene:lucene-core:9.3.0")
    implementation("org.apache.lucene:lucene-queryparser:9.3.0")
    implementation("org.jsoup:jsoup:1.15.2")
    implementation("org.postgresql:postgresql:42.3.6")
    implementation("org.slf4j:slf4j-api:1.7.36")

    implementation(files("libs/text-analyser-1.0.0.jar"))

    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    developmentOnly("org.springframework.boot:spring-boot-devtools:2.7.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.2")
    testImplementation("org.springframework.security:spring-security-test:5.7.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
