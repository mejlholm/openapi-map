FROM maven:3.8.3-openjdk-17-slim AS build

#download dependencies for caching
COPY pom.xml pom.xml
RUN mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.1.1:go-offline

#copy the rest and build it
COPY src src
RUN mvn verify -Dmaven.test.skip=true


#base image for deployment
FROM openjdk:17-alpine3.14
EXPOSE 8080

#copy build results
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV AB_ENABLED=jmx_exporter
COPY --from=build target/quarkus-app/lib/* /deployments/lib/
COPY --from=build target/quarkus-app/app/openapi-map-1.0-SNAPSHOT.jar /deployments/app.jar

#set application start
WORKDIR /deployments
CMD ["app.jar"]
