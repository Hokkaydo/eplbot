FROM eclipse-temurin:21-jre
LABEL authors="hokkaydo"

RUN mkdir -p /home/eplbot/persistence
RUN apt-get update && apt-get install -y docker.io && apt-get clean
COPY build/libs/EPLBot-1.0-SNAPSHOT-all.jar /home/eplbot/eplbot.jar
COPY variables.env /home/eplbot/variables.env
WORKDIR /home/eplbot
ENTRYPOINT ["java", "--enable-preview", "-jar", "eplbot.jar"]