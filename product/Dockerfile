FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon || true

COPY . .
RUN ./gradlew clean build -x test --no-daemon

FROM amazoncorretto:17-alpine
WORKDIR /app

ENV TZ=Asia/Seoul
COPY --from=builder /app/build/libs/*.jar app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8082
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
