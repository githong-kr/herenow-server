# 1. 빌드 스테이지
FROM gradle:8.5.0-jdk21 AS builder

WORKDIR /build

# 캐시 효율을 위해 의존성 관련 파일 먼저 복사
COPY build.gradle.kts settings.gradle.kts /build/

# 소스 코드 복사 및 실행 가능한 JAR 빌드 (테스트 생략)
COPY src /build/src
RUN gradle bootJar -x test --no-daemon

# 2. 실행 스테이지
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 빌더 이미지에서 생성된 JAR 파일 복사
COPY --from=builder /build/build/libs/*.jar app.jar

# 타임존 설정 (한국 시간)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

EXPOSE 8080

# Profile 환경 변수 주입을 위한 엔트리포인트 (기본은 default)
ENV SPRING_PROFILES_ACTIVE=default
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
