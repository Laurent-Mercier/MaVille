FROM openjdk:17-jdk-slim

WORKDIR /MaVille

COPY maville.jar /MaVille/
COPY src /MaVille/src

EXPOSE 3000

CMD ["java", "-jar", "maville.jar"]