FROM eclipse-temurin:21.0.2_13-jre-jammy
COPY build/libs/be-0.0.1-SNAPSHOT.jar /opt/be.jar
COPY build/resources/main/application-docker.properties /opt
ENTRYPOINT java -Dspring.profiles.active=docker -Dspring.config.location=/opt/ -jar /opt/be.jar