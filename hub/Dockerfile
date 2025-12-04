FROM amazoncorretto:17-alpine

WORKDIR /app
ENV TZ=Asia/Seoul

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.7.0/opentelemetry-javaagent.jar /otel-javaagent.jar

# Gradle 빌드로 만들어진 jar 복사
COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="\
 -XX:+UseContainerSupport \
 -XX:MaxRAMPercentage=75 \
 -XX:InitialRAMPercentage=40 \
 -XX:+UseG1GC \
"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -javaagent:/otel-javaagent.jar -jar /app/app.jar"]
