FROM gradle:8.2.1-jdk17 AS BUILD

ARG GH_USER
ENV GH_USER $GH_USER
RUN test -n "$GH_USER"

ARG GH_TOKEN
ENV GH_TOKEN $GH_TOKEN
RUN test -n "$GH_TOKEN"

# variables needed to run unit tests
ARG GOOGLE_GEOCODING_API_KEY
ENV GOOGLE_GEOCODING_API_KEY $GOOGLE_GEOCODING_API_KEY
RUN test -n "$GOOGLE_GEOCODING_API_KEY"
ENV DB_HOST=127.0.0.1
ENV DB_PORT=54322
ENV DB_NAME=postgres
ENV DB_PASS=postgres
ENV DB_USER=postgres
ENV SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0
ENV SUPABASE_URL=http://127.0.0.1:54321
ENV SITE_URL=http://127.0.0.1:3000/

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon --scan
RUN rm /home/gradle/src/build/libs/*-plain.jar

FROM amazoncorretto:17-alpine-full

RUN apk --no-cache add curl
RUN mkdir /app
COPY --from=BUILD /home/gradle/src/build/libs/*.jar /app/microservice.jar

ENTRYPOINT ["java","-jar","/app/microservice.jar"]
