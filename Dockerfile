# 1. JDK 17 기반 이미지 (가볍고 안정적)
FROM openjdk:17-jdk-slim

# 2. 컨테이너 내부 작업 디렉토리 생성
WORKDIR /app

# 3. Gradle 빌드 결과물(JAR) 복사
# (GitHub Actions에서 gradlew build 후 build/libs/*.jar이 생성됨)
COPY build/libs/*SNAPSHOT.jar app.jar

# 4. EC2 환경변수도 전달 가능 (옵션)
# ENV SPRING_PROFILES_ACTIVE=prod

# 5. Java 실행 옵션 추가 (메모리 최적화)
ENTRYPOINT ["java", "-jar", "app.jar"]