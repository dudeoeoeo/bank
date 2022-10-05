FROM amazoncorretto:11-alpine3.13

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x ./gradlew && ./gradlew clean build

ARG JAR_FILE=build/libs/bank*.jar
COPY ${JAR_FILE} bank*.jar

ENTRYPOINT ["java", "-jar", "bank*.jar"]

EXPOSE 8080