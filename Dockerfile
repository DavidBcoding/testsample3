FROM openjdk:8-alpine

COPY target/uberjar/testthree.jar /testthree/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/testthree/app.jar"]
