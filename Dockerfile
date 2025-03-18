#
# This dockerfile expects a compiled artifact in the target folder.
# Call "mvn clean package" first!
#
FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080
HEALTHCHECK --interval=10s CMD curl --fail http://localhost:8080/health || exit 1

COPY target/apigateway-*.jar /usr/local/lib/apigateway.jar

ENTRYPOINT ["java","-jar","/usr/local/lib/apigateway.jar"]
