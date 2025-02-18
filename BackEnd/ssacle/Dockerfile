# Gradle 이미지 (JDK 17 포함)를 사용하여 빌드합니다.
FROM gradle:8.1.1-jdk17 AS build

# 작업 디렉토리 설정
WORKDIR /home/gradle/project

# 프로젝트 전체 복사
# (gradlew, gradle 디렉토리, 그리고 소스 코드 모두 포함)
COPY --chown=gradle:gradle . .

# gradlew에 실행 권한을 부여합니다.
RUN chmod +x gradlew

# Gradle Wrapper를 사용하여 Spring Boot 애플리케이션 빌드 (bootJar)
RUN ./gradlew clean bootJar --no-daemon


# ────────────── Run Stage ──────────────
# OpenJDK 17 기반의 경량 런타임 이미지 사용 (예: slim 이미지)
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드 단계에서 생성된 jar 파일을 복사합니다.
# 보통 Spring Boot 프로젝트는 build/libs 디렉토리에 단일 jar 파일을 생성합니다.
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# 업로드 디렉토리를 도커 볼륨으로 지정합니다.
VOLUME ["/app/uploads"]

# 애플리케이션이 사용하는 포트를 EXPOSE (예: 8080)
EXPOSE 8080

# 컨테이너 시작 시 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]