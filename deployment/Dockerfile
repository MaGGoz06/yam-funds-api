FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S yam && adduser -S yam -G yam
COPY applications/app-service/build/libs/yam-fondos.jar app.jar
USER yam
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
