# 빌드 스테이지
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Gradle 실행에 필요한 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# gradlew 실행 권한 부여
RUN chmod +x gradlew

# Gradle 캐시를 BuildKit cache mount로 유지
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon || true

COPY src/ src/

# 빌드
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar -x test --no-daemon


# 런타임 스테이지
FROM eclipse-temurin:21-jre

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]