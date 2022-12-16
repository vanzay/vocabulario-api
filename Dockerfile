# this stage will be rebuilt only when build.gradle.kts is changed
FROM gradle:7.6-jdk17-alpine as deps

# /home/gradle/.gradle in parent Gradle Docker image is defined as volume and is erased after each image layer
RUN mkdir -p /gradle_cache
ENV GRADLE_USER_HOME /gradle_cache

WORKDIR /app

COPY libs ./libs
COPY build.gradle.kts settings.gradle.kts ./

# /dev/null for skipping errors
RUN gradle clean build --no-daemon > /dev/null 2>&1 || true

# --------------------------------------------------
FROM gradle:7.6-jdk17-alpine as build

COPY --from=deps /gradle_cache /home/gradle/.gradle

WORKDIR /app

COPY src ./src
COPY libs ./libs
COPY build.gradle.kts settings.gradle.kts ./

RUN gradle bootJar -i --stacktrace --no-daemon

# --------------------------------------------------
FROM openjdk:17-jdk-alpine as runner

WORKDIR /app

ADD indecies/*.tar.gz /var/lucene_indices/

RUN addgroup -g 10001 -S java \
    && adduser -S spring -u 10001 \
    && chown spring:java /var/lucene_indices -R

COPY --chown=spring:java application.properties logback.xml ./
COPY --from=build --chown=spring:java /app/build/libs/vocabulario*.jar ./vocabulario.jar

USER spring

EXPOSE 8080

CMD [ "java", "-jar", "vocabulario.jar" ]
