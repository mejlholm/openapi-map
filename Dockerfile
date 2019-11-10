FROM maven:3.6.2-jdk-11-slim AS build

COPY pom.xml pom.xml

#download dependencies for caching
RUN mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.1.1:go-offline

#copy the rest and build it
COPY src src
RUN mvn verify -Dmaven.test.skip=true


#base image for deployment
FROM gcr.io/distroless/java:11
EXPOSE 8080

#copy build results
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV AB_ENABLED=jmx_exporter
COPY --from=build target/lib/* /deployments/lib/
COPY --from=build target/*-runner.jar /deployments/app.jar

#set application start
WORKDIR /deployments
CMD ["app.jar"]
