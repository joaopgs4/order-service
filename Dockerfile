#order-service dockerfile (needs the auto-install)
FROM openjdk:21-slim
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
