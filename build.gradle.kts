import org.gradle.kotlin.dsl.annotationProcessor
import org.gradle.kotlin.dsl.compileOnly
import org.gradle.kotlin.dsl.developmentOnly
import org.gradle.kotlin.dsl.implementation
import org.gradle.kotlin.dsl.testImplementation
import org.gradle.kotlin.dsl.testRuntimeOnly

plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "AIBE3_FinalProject_Team4"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    dependencies {
        // Spring 기본
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-security")
        implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
        developmentOnly("org.springframework.boot:spring-boot-devtools")
        annotationProcessor("org.projectlombok:lombok")
        compileOnly("org.projectlombok:lombok")

        // HTTP Client
        implementation("org.apache.httpcomponents.client5:httpclient5")

        // .env 파일 지원
        implementation("me.paulschwarz:spring-dotenv:4.0.0")

        // AOP
        implementation("org.springframework.boot:spring-boot-starter-aop")

        // Swagger
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

        // JWT
        implementation("io.jsonwebtoken:jjwt-api:0.11.5")
        runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
        runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

        // MySQL / H2
        runtimeOnly("com.mysql:mysql-connector-j:8.3.0")
        runtimeOnly("com.h2database:h2")

        // Redis
        implementation("org.springframework.boot:spring-boot-starter-data-redis")
        implementation("com.fasterxml.jackson.core:jackson-databind")

        // AWS S3
        implementation("com.amazonaws:aws-java-sdk-s3:1.12.681")

        // Test
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.security:spring-security-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }


}

tasks.withType<Test> {
    useJUnitPlatform()
}
