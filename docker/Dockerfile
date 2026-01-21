# 빌드 스테이지
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Gradle 실행에 필요한 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

# gradlew 실행 권한 부여
RUN chmod +x gradlew

# 애플리케이션 빌드 (테스트 제외)
RUN ./gradlew clean build -x test


# 런타임 스테이지
FROM eclipse-temurin:21-jre

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# dev 프로필로 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "exec java -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}"]