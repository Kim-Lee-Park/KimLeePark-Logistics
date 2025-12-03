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

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.7.0/opentelemetry-javaagent.jar /otel-javaagent.jar

COPY --from=builder /app/build/libs/*.jar app.jar
ENV JAVA_OPTS="\
 -XX:+UseContainerSupport \
 -XX:MaxRAMPercentage=75 \
 -XX:InitialRAMPercentage=40 \
 -XX:+UseG1GC \
"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -javaagent:/otel-javaagent.jar -jar /app/app.jar"]
