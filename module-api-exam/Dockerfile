FROM amazoncorretto:17

# argument jar path
ARG JAR_FILE=build/libs/*.jar

# copy jar path
COPY ${JAR_FILE} app.jar

# execute
ENTRYPOINT ["java", "-jar", "app.jar"]