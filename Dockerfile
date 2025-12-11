# Build stage

FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
ARG APP_VERSION=dev
ENV APP_VERSION=${APP_VERSION}
LABEL app.version=${APP_VERSION}

RUN ./mvnw -q -e dependency:go-offline
COPY src src
RUN rm -rf ~/.m2/repository/net/bytebuddy
RUN ./mvnw -q -e -DskipTests package

# Final stage

FROM eclipse-temurin:17-jdk
ARG APP_VERSION=dev
ENV APP_VERSION=${APP_VERSION}
ARG SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}"

WORKDIR /app
COPY --from=build /app/target/cvtcaptcha-${APP_VERSION}.jar cvtcaptcha.jar
COPY jvm-options.txt jvm-options.txt

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "@/jvm-options.txt" "-jar", "cvtcaptcha.jar"]