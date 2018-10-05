FROM openjdk:8-jdk-alpine AS builder
WORKDIR /src/
COPY gradle /src/gradle
COPY gradlew /src/
RUN ls -al
RUN ./gradlew --version

COPY . /src/
RUN ./gradlew build


FROM openjdk:8-jre-alpine
RUN mkdir /app
COPY --from=builder /src/build/libs/*-all.jar /app/microservice.jar

# set the default port to 80
ENV PORT 80
EXPOSE 80

CMD ["java", "-jar", "/app/microservice.jar"]
