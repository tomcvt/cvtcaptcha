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

FROM eclipse-temurin:17-jdk
ARG APP_VERSION=dev
ENV APP_VERSION=${APP_VERSION}

WORKDIR /app
COPY --from=build /app/target/cvtcaptcha-${APP_VERSION}.jar cvtcaptcha.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "cvtcaptcha.jar"]