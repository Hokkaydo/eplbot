docker build -t java-runner -f src/main/java/com/github/hokkaydo/eplbot/module/code/java/Dockerfile .
docker image tag java-runner:latest hokkaydo/java-runner:latest
docker build -t python-runner -f src/main/java/com/github/hokkaydo/eplbot/module/code/python/Dockerfile .
docker image tag python-runner:latest hokkaydo/python-runner:latest
docker build -t c-runner -f src/main/java/com/github/hokkaydo/eplbot/module/code/c/Dockerfile .
docker image tag c-runner:latest hokkaydo/c-runner:latest
