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

    //test
    testImplementation("org.mockito:mockito-junit-jupiter")
    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // AWS S3
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.681")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Elasticsearch
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    //tsid
    implementation("com.github.f4b6a3:tsid-creator:5.2.6")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // Docker Compose
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")


    // Spring AI
    implementation(platform("org.springframework.ai:spring-ai-bom:1.0.3"))
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("org.springframework.ai:spring-ai-starter-model-huggingface")
    // Vector DB
    implementation("org.springframework.ai:spring-ai-starter-vector-store-pinecone")
    // 스트리밍 및 비동기 처리
    implementation("org.springframework.boot:spring-boot-starter-webflux")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
