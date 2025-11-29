FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -q -e dependency:go-offline
COPY src src
RUN rm -rf ~/.m2/repository/net/bytebuddy
RUN ./mvnw -q -e -DskipTests package

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/cvtcaptcha-0.0.1-SNAPSHOT.jar cvtcaptcha.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "cvtcaptcha.jar"]