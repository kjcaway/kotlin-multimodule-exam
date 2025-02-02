FROM amazoncorretto:17-alpine

RUN apk add --no-cache vim curl procps

# argument jar path
ARG JAR_FILE=build/libs/*.jar

# copy jar path
COPY ${JAR_FILE} app.jar

# execute
ENTRYPOINT ["java", "-jar", "app.jar"]