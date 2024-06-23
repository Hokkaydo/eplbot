# using a stable version
FROM gcc:9
WORKDIR /usr/src/app
COPY src/main/java/com/github/hokkaydo/eplbot/module/code/c/run_c.sh /usr/src/app
RUN chmod +x run_c.sh
ENTRYPOINT ["bash", "./run_c.sh"]

