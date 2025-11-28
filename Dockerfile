# 1) Build Stage
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# Gradle 캐싱을 위한 의존성 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 캐시 생성
RUN gradle dependencies --no-daemon || true

# 나머지 소스 복사
COPY . .

# Spring Boot JAR 빌드
RUN gradle bootJar --no-daemon

# 2) Run Stage
FROM amazoncorretto:21-alpine

WORKDIR /app

# JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# Timezone 설정 (Asia/Seoul)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

# 포트 오픈
EXPOSE 8080

# 반드시 prod 프로필로 실행
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
