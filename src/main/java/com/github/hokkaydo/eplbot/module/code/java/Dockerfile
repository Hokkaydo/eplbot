FROM openjdk:8-jdk-slim
WORKDIR /usr/src/app
COPY src/main/java/com/github/hokkaydo/eplbot/module/code/java/run_java.sh /usr/src/app
RUN chmod +x run_java.sh
ENTRYPOINT ["bash", "./run_java.sh"]
