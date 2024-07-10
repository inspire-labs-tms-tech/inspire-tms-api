FROM gradle:8.2.1-jdk17 AS BUILD

ARG GH_USER
ENV GH_USER $GH_USER
RUN test -n "$GH_USER"

ARG GH_TOKEN
ENV GH_TOKEN $GH_TOKEN
RUN test -n "$GH_TOKEN"

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon
RUN rm /home/gradle/src/build/libs/*-plain.jar

FROM amazoncorretto:17-alpine-full

RUN apk --no-cache add curl
RUN mkdir /app
COPY --from=BUILD /home/gradle/src/build/libs/*.jar /app/microservice.jar

ENTRYPOINT ["java","-jar","/app/microservice.jar"]
