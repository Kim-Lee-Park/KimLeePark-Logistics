FROM amazoncorretto:17-alpine

WORKDIR /app
ENV TZ=Asia/Seoul

COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="\
 -XX:+UseContainerSupport \
 -XX:MaxRAMPercentage=50 \
 -XX:InitialRAMPercentage=25 \
 -XX:+UseG1GC \
"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
