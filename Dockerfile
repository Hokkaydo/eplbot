FROM eclipse-temurin:21-jre AS base

LABEL authors="hokkaydo"

RUN mkdir -p /home/eplbot/persistence && apt-get update && apt-get install -y docker.io && apt-get clean
COPY build/libs/EPLBot-1.0-SNAPSHOT-all.jar /home/eplbot/eplbot.jar
COPY variables.env /home/eplbot/variables.env

FROM base AS profiler

RUN apt-get update && apt-get install -y unzip && apt-get clean && \
    wget https://www.yourkit.com/download/docker/YourKit-JavaProfiler-2024.9-docker.zip -P /tmp && \
    unzip /tmp/YourKit-JavaProfiler-2024.9-docker.zip -d /usr/local && \
    rm /tmp/YourKit-JavaProfiler-2024.9-docker.zip
EXPOSE 10001
WORKDIR /home/eplbot
ENTRYPOINT ["java", "-agentpath:/usr/local/YourKit-JavaProfiler-2024.9/bin/linux-x86-64/libyjpagent.so=port=10001,listen=all", "--enable-preview", "-jar", "eplbot.jar"]

FROM base AS production

WORKDIR /home/eplbot
ENTRYPOINT ["java", "--enable-preview", "-jar", "eplbot.jar"]