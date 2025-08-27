## 1 단계 : 빌드
## Temurin OpenJDK 17을 베이스 이미지로 사용
#FROM eclipse-temurin:17-jdk-alpine AS build
#
## 빌드된 애플리케이션 파일 저장 위치 설정
#WORKDIR /app
#
## 전체 프로젝트 복사(Gradle 빌드를 위해)
#COPY . .
#
## Gradle Wrapper 실행으로 JAR 빌드
## --no-daemon : Docker에서는 보통 일회성 작업을 많이 한다.
##              gradle 데몬은 빌드 성능을 향상시키기 위해 백그라운드에서 상시 유지되지만,
##              Docker 컨테이너에서는 빌드 후 종료되기 때문에 굳이 데몬을 켜둘 필요가 없다.
##              그래서 Dockerfile에서는 보통 --no-demon을 붙여서 명시적으로 데몬을 끄는게 안정적이다.
#RUN ./gradlew clean build --no-daemon
#
## 2 단계 : 실제 실행용 이미지
#FROM eclipse-temurin:17-jdk-alpine
#
#WORKDIR /app
#
## 1 단계에서 빌드된 JAR 파일만 복사
#COPY --from=build /app/build/libs/*.jar app.jar
#
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-jar", "app.jar"]

# 빌드 스테이지
FROM eclipse-temurin:17 AS build
WORKDIR /app

# Gradle 래퍼와 빌드 파일 복사
COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle settings.gradle ./

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 다운로드 (캐시 최적화)
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY src/ src/

# 애플리케이션 빌드 (테스트 제외)
RUN ./gradlew clean bootJar --no-daemon -x test

# 실행 스테이지
FROM openjdk:17-jdk-slim
WORKDIR /app

# ===== Locale 세팅 추가 =====
RUN apt-get update && apt-get install -y locales \
    && locale-gen ko_KR.UTF-8 \
    && rm -rf /var/lib/apt/lists/*

ENV LANG=ko_KR.UTF-8
ENV LANGUAGE=ko_KR:ko
ENV LC_ALL=ko_KR.UTF-8
# ============================

# 애플리케이션 JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# Spring Boot가 해당 경로를 읽도록 지정
ENTRYPOINT ["java", "-jar", "app.jar"]
