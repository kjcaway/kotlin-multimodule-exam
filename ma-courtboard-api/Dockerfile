FROM amazoncorretto:17-alpine

RUN apk add --no-cache vim curl procps

# argument jar path
ARG JAR_FILE=build/libs/*.jar

# copy jar path
COPY ${JAR_FILE} app.jar

# execute
ENTRYPOINT ["java", \
  "-Xms256m", \
  "-Xmx512m", \
  "-XX:MetaspaceSize=96m", \
  "-XX:MaxMetaspaceSize=192m", \
  "-Xss256k", \
  "-XX:+OptimizeStringConcat", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]